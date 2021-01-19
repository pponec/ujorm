/*
 * Copyright 2020-2020 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.web.ajax;

import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.Injector;

/**
 * A common Javascript Writer of the Ujorm framework
 *
 * @author Pavel Ponec
 */
public class JavaScriptWriter implements Injector {

    /** Default AJAX request parameter name */
    public static final HttpParameter DEFAULT_AJAX_REQUEST_PARAM = new HttpParameter() {
            @Override
            public String toString() {
                return "_ajax";
            }
        };
    /** Default AJAX request parameter name */
    public static final HttpParameter DEFAULT_SORT_REQUEST_PARAM = new HttpParameter() {
            @Override
            public String toString() {
                return "_sort";
            }
        };
    /** Default duration */
    public static final Duration DEFAULT_DURATION = Duration.ofMillis(250);

    /** Javascript ajax request parameter */
    protected final HttpParameter ajaxRequestParam;
     /** Input selectors */
    protected final CharSequence[] inputCssSelectors;
    /** Input idle delay */
    @Nonnull
    protected Duration idleDelay = DEFAULT_DURATION;
    /** Form selector */
    protected String formSelector = "form";
    /** On load submit request */
    protected boolean onLoadSubmit = false;
    /** New line characters */
    protected CharSequence newLine = "\n";
    /** A subtitle selector */
    @Nullable
    protected CharSequence subtitleSelector;
    /** A subtitle selector */
    @Nonnull
    protected CharSequence errorMessage = "AJAX fails due";
    /** Ajax Timeout */
    @Nonnull
    protected Duration ajaxTimeout = Duration.ofMillis(30_000);
    /** JavaScript version */
    protected int version = 1;
    /** Javascript ajax request parameter */
    protected String ajaxRequestPath = "/ajax";
    /** Is the table sortable */
    protected boolean isSortable = true;

    public JavaScriptWriter() {
        this("form input");
    }

    public JavaScriptWriter(@Nonnull CharSequence... inputSelectors) {
        this(DEFAULT_DURATION, JavaScriptWriter.DEFAULT_AJAX_REQUEST_PARAM, inputSelectors);
    }

    public JavaScriptWriter(
            @Nonnull Duration idleDelay,
            @Nonnull HttpParameter ajaxRequestParam,
            @Nonnull CharSequence... inputSelectors) {
        this.idleDelay = Assert.notNull(idleDelay, "idleDelay");
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "ajaxRequestParam");
        this.inputCssSelectors = Assert.hasLength(inputSelectors, "inputSelectors");
    }

    public JavaScriptWriter setFormSelector(String formSelector) {
        this.formSelector = Assert.notNull(formSelector, "formSelector");
        return this;
    }

    public JavaScriptWriter setOnLoadSubmit(boolean onLoadSubmit) {
        this.onLoadSubmit = onLoadSubmit;
        return this;
    }

    public JavaScriptWriter setNewLine(@Nonnull CharSequence newLine) {
        this.newLine = Assert.notNull(newLine, "newLine");
        return this;
    }

    /** Assign a subtitle CSS selector */
    public JavaScriptWriter setSubtitleSelector(CharSequence subtitleSelector) {
        this.subtitleSelector = subtitleSelector;
        return this;
    }

    /** Assign an AJAX error message */
    public JavaScriptWriter setErrorMessage(@Nullable CharSequence errorMessage) {
        this.errorMessage = Assert.hasLength(errorMessage, "errorMessage");
        return this;
    }

    /** Assign an AJAX timeout */
    public JavaScriptWriter setAjaxTimeout(@Nonnull Duration ajaxTimeout) {
        this.ajaxTimeout = Assert.notNull(ajaxTimeout, "ajaxTimeout");
        return this;
    }

    /** Assign an AJAX timeout */
    public JavaScriptWriter setAjaxRequestPath(@Nonnull String ajaxRequestPath) {
        this.ajaxRequestPath = ajaxRequestPath;
        setVersion(2);
        return this;
    }

    /** Assign an AJAX timeout */
    public JavaScriptWriter setVersion(int version) {
        this.version = version;
        return this;
    }

    /** Assign a Sortable table */
    public JavaScriptWriter setSortable(boolean isSortable) {
        this.isSortable=isSortable;
        return this;
    }

    /**
     * Generate a Javascript
     */
    @Override
    public void write(@Nonnull final Element parent) {
        try (Element js = parent.addElement(Html.SCRIPT)) {
            js.addRawText(newLine);
            js.addRawText("$(document).ready(function(){");
            if (Check.hasLength(inputCssSelectors)) {
                final String inpSelectors = Stream.of(inputCssSelectors)
                        .collect(Collectors.joining(", "));
                js.addRawTexts(newLine, ""
                        , "var timeout=null, ajaxRun=false, submitReq=false;"
                        , "$('" + inpSelectors + "').keyup(function(){"
                        , "  if (timeout){"
                        , "    clearTimeout(timeout);"
                        , "  }"
                        , "  timeout=setTimeout(function(){"
                        , "    timeout=null;"
                        , "    if(ajaxRun){submitReq=true;}"
                        , "    else{$('" + formSelector + "').submit();}"
                        , "  }, " + idleDelay.toMillis() + ");"
                        , "});"
                    );
            } {
            js.addRawTexts(newLine, ""
                    , "$('form').submit(function(event){"
                    , "  event.preventDefault();"
                    , "  ajaxRun=true;"
                    , "  var data=$('" + formSelector + "').serialize();"
                    , "  $.ajax("
                        + (version == 2
                            ? ("{ url:'" + ajaxRequestPath + "'")
                            : ("{ url:'?" + ajaxRequestParam + "=true'"))
                        + ", type:'POST'"
                        + ", data:data"
                        + ", timeout:" + ajaxTimeout.toMillis()
                        + ", error:function(xhr,ajaxOptions,thrownError){", Check.hasLength(subtitleSelector)
                        ? "   ajaxRun=false;"
                        +    " $('" + subtitleSelector + "').html('" + errorMessage + ":' + thrownError);":""
                        , "  }"
                        + ", success:function(result){"
                    , "    var jsn=JSON.parse(result);"
                    , "    $.each(jsn,function(key,value){"
                    , "      $(key).html(value);"
                    , "    }); "
                    , "    if(submitReq){submitReq=false; $('" + formSelector + "').submit();} "
                    , "    else{ajaxRun=false;}"
                    , "  }});"
                    , "});"
                    , onLoadSubmit ? "  $('" + formSelector + "').submit();" : ""
                );
            }
            if (isSortable) {
                js.addRawText(newLine, "", "});");
                js.addRawText(newLine, "function sort(col){");
                js.addRawText(newLine, " document.querySelector('", "input[name=\"_sort\"]').value=col;");
                js.addRawText(newLine, " document.querySelector('", formSelector , "').submit();");
                js.addRawText(newLine, "}");
            }
        }
    }
}
