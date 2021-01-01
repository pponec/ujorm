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
package org.ujorm.ujoservlet.ajax.ujorm;

import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
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
    /** Default duration */
    public static final Duration DEFAULT_DURATION = Duration.ofMillis(250);

    /** Javascript ajax request parameter */
    protected final HttpParameter ajaxRequestParam;   
     /** Input selectors */
    protected final CharSequence[] inputCssSelectors;
    /** Input idle delay */
    protected Duration idleDelay = DEFAULT_DURATION;
    
    /** Form selector */
    protected String formSelector = "form";
    /** On load submit request */
    protected boolean onLoadSubmit = false;
    /** New line characters */
    protected CharSequence newLine = "\n";
    
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

    public JavaScriptWriter setIdleDelay(Duration idleDelay) {
        this.idleDelay = Assert.notNull(idleDelay, "idleDelay");
        return this;
    }

    public JavaScriptWriter setFormSelector(String formSelector) {
        this.formSelector = Assert.notNull(formSelector, "formSelector");
        return this;
    }

    public JavaScriptWriter setOnLoadSubmit(boolean onLoadSubmit) {
        this.onLoadSubmit = Assert.notNull(onLoadSubmit, "onLoadSubmit");
        return this;
    }

    public JavaScriptWriter setNewLine(CharSequence newLine) {
        this.newLine = Assert.notNull(newLine, "newLine");
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
                js.addRawTexts(newLine, newLine
                        , "var globalTimeout = null;"
                        , "$('" + inpSelectors + "').keyup(function() {"
                        , "  if (globalTimeout != null) {"
                        , "    clearTimeout(globalTimeout);"
                        , "  }"
                        , "  globalTimeout = setTimeout(function() {"
                        , "    globalTimeout = null;"
                        , "    $('" + formSelector + "').submit();"
                        , "  }, " + idleDelay.toMillis() + ");"
                        , "});"
                    );
            } {
            js.addRawTexts(newLine, newLine
                    , "$('form').submit(function(event){"
                    , "  var data = $('" + formSelector + "').serialize();"
                    , "  $.ajax("
                          + "{ url: '?" + ajaxRequestParam + "=true'"
                        + ", type: 'POST'"
                        + ", data: data"
                        + ", timeout: 3000"
                          + ", error: function (xhr, ajaxOptions, thrownError) {"
                          ,  "   $('.subtitle').html('AJAX fails due: ' + thrownError);"
                          +  " }"
                    ,       ", success: function(result){"
                    , "    var jsn = JSON.parse(result);"
                    , "    $.each(jsn, function(key, value){"
                    , "      $(key).html(value);"
                    , "    })"
                    , "  }});"
                    , "  event.preventDefault();"
                    , "});"
                    , onLoadSubmit ? "  $('" + formSelector + "').submit();" : ""
                );
            }
            js.addRawTexts(newLine, "", "});");
        }
    }
}
