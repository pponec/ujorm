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
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.xml.config.XmlConfig;

/**
 * A common Javascript Writer of the Ujorm framework
 * 
 * @author Pavel Ponec
 */
public class JavaScriptWriter {

    /**
     * Parent element
     */
    private final Element parent;
    /**
     * Parent element
     */
    private final CharSequence newLine;
    /**
     * Javascript ajax request parameter
     */
    protected final HttpParameter ajaxRequestParam;
    /**
     * Input idle delay
     */
    protected final Duration idleDelay;

    public JavaScriptWriter(@Nonnull Element parent, @Nonnull XmlConfig config, @Nonnull Duration idleDelay, @Nonnull HttpParameter ajaxRequestParam) {
        this(parent, config.getNewLine(), idleDelay, ajaxRequestParam);
    }
    
    public JavaScriptWriter(@Nonnull Element parent, @Nonnull CharSequence newLine, @Nonnull Duration idleDelay, @Nonnull HttpParameter ajaxRequestParam) {
        this.parent = Assert.notNull(parent, "parent");
        this.newLine = Assert.notNull(newLine, "newLine");
        this.idleDelay = Assert.notNull(idleDelay, "idleDelay");
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "idleDelay");
    }

    /**
     * Generate a Javascript
     *
     * @param initFormSubmit Submit on the first form on load request
     * @param formSelector A form selector for submit
     * @param inputCssSelectors Array of CSS selector for autosubmit.
     */
    @Nonnull
    public void writeJavascript(
            final boolean initFormSubmit,
            @Nullable final CharSequence formSelector,
            @Nonnull final CharSequence... inputCssSelectors) {
        if (parent != null) try ( Element js = parent.addElement(Html.SCRIPT)) {

            js.addRawTexts(newLine, "", "<script>", "$(document).ready(function(){");
            if (Check.hasLength(inputCssSelectors)) {
                final String inpSelectors = Stream.of(inputCssSelectors)
                        .collect(Collectors.joining(", "));

            element.addRawTexts(newLine, newLine
                    , "var globalTimeout = null;"
                    , "$('" + inpSelectors + "').keyup(function() {"
                    , "  if (globalTimeout != null) {"
                    , "    clearTimeout(globalTimeout);"
                    , "  }"
                    , "  globalTimeout = setTimeout(function() {"
                    , "    globalTimeout = null;"
                    , "    $('" + formSelector + "').submit();"
                    , "  }, " + idleDelay + ");"
                    , "});"
                );
        }{
            element.addRawTexts(newLine, newLine
                    , "$('form').submit(function(event){"
                    , "  var data = $('" + formSelector + "').serialize();"
                    , "  $.ajax("
                          + "{ url: '?" + ajaxRequest + "=true'"
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
                    , initFormSubmit ? "  $('" + formSelector + "').submit();" : ""
                );
            }
            js.addRawTexts(newLine, "", "});");
        }
    }
}
