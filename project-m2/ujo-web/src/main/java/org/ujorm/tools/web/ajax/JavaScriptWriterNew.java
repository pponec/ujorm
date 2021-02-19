/*
 * Copyright 2021-2021 Pavel Ponec, https://github.com/pponec
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
import java.time.LocalDateTime;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.Injector;

/**
 * A prototype of ES6 Vanilla Javascript Writer of the Ujorm framework
 *
 * @author Pavel Ponec
 */
@Deprecated
public class JavaScriptWriterNew implements Injector {

    /** Default AJAX request parameter name */
    public static final HttpParameter DEFAULT_AJAX_REQUEST_PARAM = HttpParameter.of("_ajax");
    /** Default AJAX request parameter name */
    public static final HttpParameter DEFAULT_SORT_REQUEST_PARAM = HttpParameter.of("_sort");
    /** Default duration */
    public static final Duration DEFAULT_DURATION = Duration.ofMillis(250);

    /** New line characters */
    protected CharSequence NLINE = "\n";
    /** Double quote */
    protected static CharSequence Y = "\"";


    /** Javascript ajax request parameter */
    protected final HttpParameter ajaxRequestParam;
    /** Javascript ajax request parameter */
    protected final HttpParameter sortRequestParam;
     /** Input selectors */
    protected final CharSequence[] inputCssSelectors;
    /** Input idle delay */
    @Nonnull
    protected Duration idleDelay = DEFAULT_DURATION;
    /** Form selector */
    protected String formSelector = "form";
    /** On load submit request */
    protected boolean onLoadSubmit = false;
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
    protected boolean isSortable = false;
    /** Function order of name */
    protected int fceOrder = 1;

    public JavaScriptWriterNew() {
        this("form input");
    }

    public JavaScriptWriterNew(@Nonnull CharSequence... inputSelectors) {
        this(DEFAULT_DURATION,
                DEFAULT_AJAX_REQUEST_PARAM,
                DEFAULT_SORT_REQUEST_PARAM,
                inputSelectors);
    }

    public JavaScriptWriterNew(
            @Nonnull Duration idleDelay,
            @Nonnull HttpParameter ajaxRequestParam,
            @Nonnull HttpParameter sortRequestParam,
            @Nonnull CharSequence... inputSelectors) {
        this.idleDelay = Assert.notNull(idleDelay, "idleDelay");
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "ajaxRequestParam");
        this.sortRequestParam = Assert.notNull(sortRequestParam, "sortRequestParam");
        this.inputCssSelectors = Assert.hasLength(inputSelectors, "inputSelectors");
    }

    public JavaScriptWriterNew setFormSelector(String formSelector) {
        this.formSelector = Assert.notNull(formSelector, "formSelector");
        return this;
    }

    public JavaScriptWriterNew setOnLoadSubmit(boolean onLoadSubmit) {
        this.onLoadSubmit = onLoadSubmit;
        return this;
    }

    public JavaScriptWriterNew setNewLine(@Nonnull CharSequence newLine) {
        this.NLINE = Assert.notNull(newLine, "newLine");
        return this;
    }

    /** Assign a subtitle CSS selector */
    public JavaScriptWriterNew setSubtitleSelector(CharSequence subtitleSelector) {
        this.subtitleSelector = subtitleSelector;
        return this;
    }

    /** Assign an AJAX error message */
    public JavaScriptWriterNew setErrorMessage(@Nullable CharSequence errorMessage) {
        this.errorMessage = Assert.hasLength(errorMessage, "errorMessage");
        return this;
    }

    /** Assign an AJAX timeout */
    public JavaScriptWriterNew setAjaxTimeout(@Nonnull Duration ajaxTimeout) {
        this.ajaxTimeout = Assert.notNull(ajaxTimeout, "ajaxTimeout");
        return this;
    }

    /** Assign an AJAX timeout */
    public JavaScriptWriterNew setAjaxRequestPath(@Nonnull String ajaxRequestPath) {
        this.ajaxRequestPath = ajaxRequestPath;
        setVersion(2);
        return this;
    }

    /** Assign an AJAX timeout */
    public JavaScriptWriterNew setVersion(int version) {
        this.version = version;
        return this;
    }

    /** Assign a Sortable table */
    public JavaScriptWriterNew setSortable(boolean isSortable) {
        this.isSortable = isSortable;
        return this;
    }

    /** Set a function order */
    public JavaScriptWriterNew setSortable(int fceOrder) {
        this.fceOrder = fceOrder;
        return this;
    }

    /** Set a function order name */
    public int getFceOrder() {
        return fceOrder;
    }

    /**
     * Generate a Javascript
     */
    @Override
    public void write(@Nonnull final Element parent) {
        try ( Element js = parent.addElement(Html.SCRIPT)) {
            String row = String.join(NLINE, "/* -------- x4: " + LocalDateTime.now() + " -------- */"
                , "var f1 = function() {"
                , "    var timeout = null, ajaxRun = false, submitReq = false;"
                , "    document.querySelectorAll('#regexp, #text').forEach(item=>{item.addEventListener('keyup',e=>{"
                , "        if(timeout) {clearTimeout(timeout);}"
                , "        timeout = setTimeout(function() {"
                , "            timeout=null;"
                , "            if(ajaxRun) submitReq=true; "
                , "            else process(null);"
                , "        }, 250);"
                , "      }, false);"
                , "    });"
                , "    function process(e) {"
                , "        if(e!=null) e.preventDefault();"
                , "        fetch('?_ajax=true', {"
                , "                method: 'POST',"
                , "                body: new URLSearchParams(new FormData(document.querySelector('#form'))),"
                , "                headers: {'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'},"
                , "           })"
                , "          .then(response=>response.json())"
                , "          .then(data=>{"
                , "                for (var key of Object.keys(data)) {"
                , "                    document.querySelector(key).innerHTML = data[key];"
                , "                }"
                , "                if(submitReq) {"
                , "                    submitReq = false;"
                , "                    document.querySelector('#form').submit();" // Submit all form
                , "                } else {"
                , "                    ajaxRun = false;"
                , "                }"
                , "        }).catch(function (err) {"
                , "	       alert('Something went wrong:' + err);"
                , "        });"
                , "    }"
                , "    document.querySelector('#form').addEventListener('submit', process, false);"
                , "};"
            );
            js.addRawTexts(NLINE, row);

            if (onLoadSubmit) {
                // js.addRawTexts(NLINE, "  $('", formSelector , "').submit();");
            }
            //js.addRawText("};");
            if (isSortable) {
                js.addRawTexts(NLINE, "f1.sort=(col)=>{");
                js.addRawTexts(NLINE, " document.querySelector('", "input[name=", Y, sortRequestParam, Y, "]').value=col;");
                js.addRawTexts(NLINE, " if(this.ajaxRun){this.submitReq=true;}");
                js.addRawTexts(NLINE, " else{document.querySelector('", formSelector, "').submit();}");
                js.addRawTexts(NLINE, "};");
            }
            js.addRawTexts(NLINE, "var ready=(callback)=>{",
                     "  if (document.readyState!='loading') callback();",
                     "  else document.addEventListener('DOMContentLoaded',callback);",
                     "};",
                     "ready(f1);");
        }
    }
}
