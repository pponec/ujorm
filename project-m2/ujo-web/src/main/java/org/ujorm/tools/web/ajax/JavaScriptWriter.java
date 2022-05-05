/*
 * Copyright 2021-2022 Pavel Ponec, https://github.com/pponec
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
    public static final Duration DEFAULT_DELAY = Duration.ofMillis(250);
    /** Default timeou */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /** Javascript ajax request parameter */
    protected final HttpParameter ajaxRequestParam;
    /** Javascript ajax request parameter */
    protected final HttpParameter sortRequestParam;
     /** Input selectors */
    protected final CharSequence[] inputCssSelectors;
    /** An AJAX delay to the input request */
    @NotNull
    protected Duration idleDelay = DEFAULT_DELAY;
    /** An AJAX timeout of the input response */
    @NotNull
    protected Duration ajaxTimeout = DEFAULT_TIMEOUT;
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
        this(DEFAULT_DELAY,
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

    /** An AJAX timeout to get a response  */
    public JavaScriptWriter setAjaxTimeout(@NotNull Duration ajaxTimeout) {
        this.ajaxTimeout = Assert.notNull(ajaxTimeout, "ajaxTimeout");
        return this;
    }
    
    /** An AJAX delay to the input request */
    public JavaScriptWriter setIdleDelay(@NotNull Duration idleDelay) {
        this.idleDelay = Assert.notNull(idleDelay, "idleDelay");
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
            js.addRawText(newLine, "/* Script of ujorm.org *//* jshint esversion:6 */");
            if (isAjax) {
                js.addRawText(newLine, "const f", fceOrder, "={");
                js.addRawTexts(newLine, ""
                    , "ajaxRun:false, submitReq:false, delayMs:" + idleDelay.toMillis() + ", timeout:null,"
                    , "init(e){"
                    , " document.querySelector('" + formSelector + "').addEventListener('submit',this.process,false);"
                    , " document.querySelectorAll('" + inpSelectors + "').forEach(i=>{"
                    , "  i.addEventListener('keyup',e=>this.timeEvent(e),false);"
                    , " });},"    
                );
                js.addRawTexts(newLine, ""
                    , "timeEvent(e){"
                    , " if(this.timeout)clearTimeout(this.timeout);"
                    , " this.timeout=setTimeout(()=>{"
                    , "  this.timeout=null;"
                    , "  if(this.ajaxRun)this.submitReq=true;"
                    , "  else this.process(null);"
                    , " },this.delayMs);},"
                );
                js.addRawTexts(newLine, ""
                    , "process(e){"
                    , " let pars=new URLSearchParams(new FormData(document.querySelector('" + formSelector + "')));"
                    , " if(e!==null){e.preventDefault();pars.append(e.submitter.name,e.submitter.value);}"
                    , " fetch('" + (version == 2
                            ? ajaxRequestPath
                            : ("?" + ajaxRequestPath + "=true")) + "', {"
                    , "   method:'POST',"
                    , "   body:pars,"
                    , "   headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'},"
                    , " })"
                    , " .then(response=>response.json())"
                    , " .then(data=>{"
                    , "   for(const key of Object.keys(data))"
                    , "    if(key=='')eval(data[key]);"
                    , "    else document.querySelectorAll(key).forEach(i=>{i.innerHTML=data[key];});"
                    , "   if(this.submitReq){this.submitReq=false;this.process(e);}" // Next submit the form
                    , "   else{this.ajaxRun=false;}"
                    , " }).catch(err=>{"
                    , "   this.ajaxRun=false;"
                    , "    document.querySelector('" + subtitleSelector + "').innerHTML='" + errorMessage + ": ' + err;"
                    , " });"
                    , "}"
                );
                js.addRawTexts(newLine, "};");
                js.addRawText(newLine, "document.addEventListener('DOMContentLoaded',e=>f", fceOrder, ".init(e));");
                if (onLoadSubmit) {
                    js.addRawText(newLine, "f", fceOrder, ".process(null);");
                }
            }
        }
    }
}
