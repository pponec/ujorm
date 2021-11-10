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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.Injector;

/**
 * A prototype of ES6 Vanilla Javascript Writer of the Ujorm framework.
 *
 * @author Pavel Ponec
 */
public class JavaScriptWriter implements Injector {

    /** Default AJAX request parameter name */
    public static final HttpParameter DEFAULT_AJAX_REQUEST_PARAM = HttpParameter.of("_ajax");
    /** Default AJAX request parameter name */
    public static final HttpParameter DEFAULT_SORT_REQUEST_PARAM = HttpParameter.of("_sort");
    /** Default duration */
    public static final Duration DEFAULT_DURATION = Duration.ofMillis(250);

    /** Javascript ajax request parameter */
    protected final HttpParameter ajaxRequestParam;
    /** Javascript ajax request parameter */
    protected final HttpParameter sortRequestParam;
     /** Input selectors */
    protected final CharSequence[] inputCssSelectors;
    /** Input idle delay */
    @NotNull
    protected Duration idleDelay = DEFAULT_DURATION;
    /** Form selector */
    protected String formSelector = Html.FORM;
    /** On load submit request */
    protected boolean onLoadSubmit = false;
    /** New line characters */
    protected CharSequence newLine = "\n";
    /** A subtitle selector */
    @Nullable
    protected CharSequence subtitleSelector="?";
    /** A subtitle selector */
    @NotNull
    protected CharSequence errorMessage = "AJAX fails due";
    /** Ajax Timeout */
    @NotNull
    protected Duration ajaxTimeout = Duration.ofMillis(30_000);
    /** JavaScript version */
    protected int version = 1;
    /** Javascript ajax request parameter */
    protected String ajaxRequestPath = "_ajax";
    /** Function order of name */
    protected int fceOrder = 1;
    /** Ajax support */
    protected boolean isAjax = true;

    public JavaScriptWriter() {
        this("form input:not([type=\"button\"])");
    }

    public JavaScriptWriter(@NotNull CharSequence... inputSelectors) {
        this(DEFAULT_DURATION,
                DEFAULT_AJAX_REQUEST_PARAM,
                DEFAULT_SORT_REQUEST_PARAM,
                inputSelectors);
    }

    public JavaScriptWriter(
            @NotNull Duration idleDelay,
            @NotNull HttpParameter ajaxRequestParam,
            @NotNull HttpParameter sortRequestParam,
            @NotNull CharSequence... inputSelectors) {
        this.idleDelay = Assert.notNull(idleDelay, "idleDelay");
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "ajaxRequestParam");
        this.sortRequestParam = Assert.notNull(sortRequestParam, "sortRequestParam");
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

    public JavaScriptWriter setNewLine(@NotNull CharSequence newLine) {
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
    public JavaScriptWriter setAjaxTimeout(@NotNull Duration ajaxTimeout) {
        this.ajaxTimeout = Assert.notNull(ajaxTimeout, "ajaxTimeout");
        return this;
    }

    /** Assign an AJAX timeout */
    public JavaScriptWriter setAjaxRequestPath(@NotNull String ajaxRequestPath) {
        this.ajaxRequestPath = ajaxRequestPath;
        setVersion(2);
        return this;
    }

    /** Assign an AJAX timeout */
    public JavaScriptWriter setVersion(int version) {
        this.version = version;
        return this;
    }

    /** Set a function order */
    public JavaScriptWriter setSortable(int fceOrder) {
        this.fceOrder = fceOrder;
        return this;
    }

    /** Set a function order name */
    public int getFceOrder() {
        return fceOrder;
    }

    /** Set a function order */
    public JavaScriptWriter setAjax(boolean ajax) {
        this.isAjax = ajax;
        return this;
    }

    /** Set a function order name */
    public boolean isAjax() {
        return isAjax;
    }

    /**
     * Generate a Javascript
     */
    @Override
    public void write(@NotNull final Element parent) {
        final String inpSelectors = Check.hasLength(inputCssSelectors)
        ? Stream.of(inputCssSelectors).collect(Collectors.joining(", "))
        : "#!@";
        try (Element js = parent.addElement(Html.SCRIPT)) {
            js.addRawText(newLine, "/* jshint esversion:6 */");
            if (isAjax) {
                js.addRawText(newLine, "var f", fceOrder, "=function(){");
                js.addRawTexts(newLine, ""
                    , "var timeout=null, ajaxRun=false, submitReq=false;"
                  //, "document.querySelectorAll('button.sortable').forEach(item=>{item.addEventListener('click',e=>sort(e.target.value));},false);" // Wrong way
                    , "document.querySelector('" + formSelector + "').addEventListener('submit',process,false);"
                    , "document.querySelectorAll('" + inpSelectors + "').forEach(item=>{item.addEventListener('keyup',e=>{"
                    , "  if(timeout){clearTimeout(timeout);}"
                    , "  timeout=setTimeout(()=>{"
                    , "    timeout=null;"
                    , "    if(ajaxRun) submitReq=true; "
                    , "    else process(null);"
                    , "  }," + idleDelay.toMillis() + ");},false);"
                    , "});"
                );
                if (onLoadSubmit) {
                    js.addRawText(newLine, "process(null);");
                }
                js.addRawTexts(newLine, ""
                    , "function process(e){"
                    , "  let pars=new URLSearchParams(new FormData(document.querySelector('" + formSelector + "')));"
                    , "  if(e!==null){e.preventDefault();pars.append(e.submitter.name,e.submitter.value);}"
                    , "  fetch('" + (version == 2
                            ? ajaxRequestPath
                            : ("?" + ajaxRequestPath + "=true")) + "', {"
                    , "    method:'POST',"
                    , "    body:pars,"
                    , "    headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'},"
                    , "  })"
                    , "  .then(response=>response.json())"
                    , "  .then(data=>{"
                    , "    for (var key of Object.keys(data))"
                    , "      document.querySelectorAll(key).forEach(i=>{i.innerHTML=data[key];});"
                    , "    if(submitReq){submitReq=false;process(e);}" // Next submit the form
                    , "    else{ajaxRun=false;}"
                    , "  }).catch(err=>{"
                    , "    ajaxRun=false;"
                    , "    document.querySelector('" + subtitleSelector + "').innerHTML='" + errorMessage + ": ' + err;"
                    , "  });"
                    , "}"
                );
                js.addRawTexts(newLine, "};");
                js.addRawText(newLine, "document.addEventListener('DOMContentLoaded',f", fceOrder, ");");
            }
        }
    }
}
