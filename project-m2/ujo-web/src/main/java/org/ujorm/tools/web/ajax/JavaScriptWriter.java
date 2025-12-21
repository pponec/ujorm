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

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.msg.MessageService;
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
    /** All input elements except buttons */
    private static final CharSequence[] DEFAULT_INPUT_SELECTORS = {"input:not([type='button'])", "textarea", "select"};

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
    protected String formCssSelector = Html.FORM;
    /** On load submit request */
    protected boolean onLoadSubmit = false;
    /** New line characters */
    protected CharSequence newLine = "\n";
    /** An error message selector */
    @Nullable
    protected CharSequence errorSelector ="?";
    /** A subtitle selector */
    @NotNull
    protected CharSequence errorMessage = "AJAX fails due";
    /** JavaScript version number */
    protected int version = 3;
    /** Javascript ajax request parameter */
    protected String ajaxRequestPath = "_ajax";
    /** Function order of name */
    protected int fceOrder = 1;
    /** Ajax support */
    protected boolean isAjax = true;

    public JavaScriptWriter() {
        this(DEFAULT_INPUT_SELECTORS);
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
        this.formCssSelector = Assert.notNull(formSelector, "formSelector");
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

    /** Assign error message CSS selector */
    public JavaScriptWriter setSubtitleSelector(CharSequence errorSelector) {
        this.errorSelector = errorSelector;
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

    @Override
    public void write(@NotNull final Element parent) {
        write(parent, Map.of());
    }

    protected String scriptTemplate() {
        return """
                /* Script of ujorm.org *//* jshint esversion:6 */
                const ${jsVar}={
                ajaxRun:false, submitReq:false, delayMs:${delayMs}, timeout:null, ${fceSpace}fceMap:{${fceMap}},
                init(){
                  document.querySelectorAll("${formSelector}").forEach(form=>{
                    form.addEventListener("submit",e=>this.process(e,form));
                    form.querySelectorAll("${inputSelector}").forEach(input=>{
                      input.addEventListener("keyup",()=>this.timeEvent(form));
                      input.addEventListener("change",()=>this.timeEvent(form));
                    });});},
                timeEvent(form) {
                  clearTimeout(this.timeout);
                  this.timeout=setTimeout(()=>{
                    this.timeout=null;
                    this.ajaxRun?(this.submitReq=true):this.process(null,form);
                  },this.delayMs);},
                async process(e,form) {
                  if(!form) return;
                  if(e) e.preventDefault();
                  [this.ajaxRun, this.submitReq]=[true, false];
                  const fd=new FormData(form);
                  if(e?.submitter?.name) fd.append(e.submitter.name,e.submitter.value);
                  try{const res=await fetch("?_ajax=true",{
                      method:"POST",
                      body:new URLSearchParams(fd),
                      headers:{"X-Requested-With":"XMLHttpRequest"}
                    });
                    if(!res.ok) throw new Error(res.status);
                    const data=await res.json();
                    Object.entries(data).forEach(([sel,val])=>{
                      if(sel==="") return this.fceMap[val]();
                      document.querySelectorAll(sel).forEach(el=>el.innerHTML=val);
                    });
                  }catch(err){console.error(err)}
                  finally{
                    this.ajaxRun=false;
                    if(this.submitReq) this.process(null, form);
                  }}};
                document.addEventListener("DOMContentLoaded",()=>${jsVar}.init());${onLoadSubmit}
                """;
    }

    private String onLoadSubmit(Map<String, Object> params) {
        if (!onLoadSubmit) return "";
        return "%sdocument.querySelectorAll('%s').forEach(form=>{%s.process(null,form)});".formatted(
                newLine,
                params.get("formSelector"),
                params.get("jsVar"));
    }

    /** Write Javascript body for the AJAX support. */
    public void write(@NotNull final Element parent, Map<String, String> functionMap) {
        var params = new HashMap<String, Object>();
        {   params.put("jsVar", "ujorm" + fceOrder);
            params.put("delayMs", idleDelay.toMillis());
            params.put("fceSpace", functionMap.isEmpty() ? "" : (newLine + "  "));
            params.put("fceMap", bulidFunctionMap(functionMap));
            params.put("formSelector", formCssSelector);
            params.put("inputSelector", inputCssSelector());
            params.put("onLoadSubmit", onLoadSubmit(params));
        }
        try (Element js = parent.addElement(Html.SCRIPT)) {
            MessageService.formatMsg(scriptTemplate(), params, appendable(js));
        }
    }

    private @NotNull String inputCssSelector() {
        return Check.hasLength(inputCssSelectors)
                ? String.join(",", inputCssSelectors)
                : "#!@_"; // No selection
    }

    /** Generate a map of JS functions */
    private String bulidFunctionMap(Map<String, String> functionMap) {
        if (functionMap.isEmpty()) return "";
        final var result = new StringBuilder(64);
        final var i = new AtomicInteger();
        functionMap.forEach((key, value) -> {
            result.append(i.getAndIncrement() == 0 ? " " : ", ");
            result.append(key).append("(){").append(value).append("}");
        });
        return result.toString();
    }

    private Appendable appendable(@NotNull final Element parent) {
        return new Appendable() {
            @Override
            public Appendable append(final CharSequence csq) throws IOException {
                parent.addRawText(csq);
                return this;
            }
            @Override
            public Appendable append(final CharSequence csq, final int start, final int end) throws IOException {
                return append(csq.subSequence(start, end));
            }
            @Override
            public Appendable append(final char c) throws IOException {
                return append(String.valueOf(c));
            }
        };
    }
}
