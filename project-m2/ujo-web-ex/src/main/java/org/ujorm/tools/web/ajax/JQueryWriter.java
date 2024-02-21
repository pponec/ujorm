/*
 * Copyright 2020-2022 Pavel Ponec, https://github.com/pponec
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
import org.ujorm.tools.Check;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.ao.HttpParameter;

/**
 * A common jQuery Writer of the Ujorm framework
 *
 * @Depreated This class is no longer maintained
 * @author Pavel Ponec
 */
@Deprecated
public class JQueryWriter extends JavaScriptWriter {

    public JQueryWriter() {
    }

    public JQueryWriter(CharSequence... inputSelectors) {
        super(inputSelectors);
    }

    public JQueryWriter(
            Duration idleDelay,
            HttpParameter ajaxRequestParam,
            HttpParameter sortRequestParam,
            CharSequence... inputSelectors) {
        super(idleDelay, ajaxRequestParam, sortRequestParam, inputSelectors);
    }

    /**
     * Generate a Javascript
     */
    @Override
    public void write(@NotNull final Element parent) {
        try (Element js = parent.addElement(Html.SCRIPT)) {
            if (isAjax) {
                js.addRawText(newLine);
                js.addRawText("var f", fceOrder, "=function(){");
                if (Check.hasLength(inputCssSelectors)) {
                    final String inpSelectors = Stream.of(inputCssSelectors)
                            .collect(Collectors.joining(", "));
                    js.addRawTexts(newLine, ""
                            , "var timeout=null, ajaxRun=false, submitReq=false;"
                            , "$('" + inpSelectors + "').keyup(function(){"
                            , "  if(timeout){clearTimeout(timeout);}"
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
                    );
                    if (onLoadSubmit) {
                        js.addRawText(newLine, "  $('" + formSelector + "').submit();");
                    }
                }
                js.addRawText("};");
                js.addRawText("$(document).ready(f", fceOrder, ");");
            }
        }
    }
}
