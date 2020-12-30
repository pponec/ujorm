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

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.Element;

/**
 *
 * @author Pavel Ponec
 */
public class JavaScriptProvider {
    
    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(JavaScriptProvider.class.getName());
    /** Default AJAX request parameter name */
    public static final String DEFAULT_AJAX_REQUEST_PARAM = "_ajax";
    /** Javascript ajax request parameter */
    protected final CharSequence ajaxRequest;
    /** Javascript line separator */
    protected final CharSequence newLine;
    /** Input idle delay in millisec */
    protected final int idleDelay;

    /** Default constructor */
    public JavaScriptProvider(
            @Nonnull final CharSequence ajaxRequest,
            @Nonnull final CharSequence newLine,
            final int idleDelay) {
        this.ajaxRequest = ajaxRequest;
        this.idleDelay = idleDelay;
        this.newLine = newLine;
    }
    
    
    /**
     * Generate a Javascript
     * @param element Root element, where {@code null} value disable the javascript.
     * @param initFormSubmit Submit on the first form on load request
     * @param formSelector A form selector for submit
     * @param inputCssSelectors Array of CSS selector for autosubmit.
     */
    @Nonnull
    public void writeJavascript(
            @Nullable final Element element,
            final boolean initFormSubmit,
            @Nullable final CharSequence formSelector,
            @Nonnull final CharSequence... inputCssSelectors) {
        if (element == null) {
            return;
        }
        element.addRawTexts(newLine, newLine, "<script>", "$(document).ready(function(){");
        if (Check.hasLength(inputCssSelectors)) {
                    final String inpSelectors = Stream.of(inputCssSelectors)
              //.map(t -> "." + t)
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
        element.addRawTexts(newLine, newLine, "});", "</script>");
    }
    
}
