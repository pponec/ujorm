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
package org.ujorm.ujoservlet.ajax;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.Assert;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.JsonBuilder;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 *
 * @author Pavel Ponec
 */
public class ReqestDispatcher {

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(ReqestDispatcher.class.getName());

    @Nonnull
    private final HttpServletRequest input;

    @Nonnull
    private final HttpServletResponse output;

    @Nonnull
    private final HtmlConfig htmlConfig;

    @Nonnull
    private final HtmlConfig ajaxConfig;

    private boolean done = false;

    /**
     * Disable client cache
     */
    private final boolean noCache = true;

    public ReqestDispatcher(
            @Nonnull HttpServletRequest input,
            @Nonnull HttpServletResponse output) {
        this(input, output, HtmlConfig.ofDefault());
    }

    public ReqestDispatcher(
            @Nonnull HttpServletRequest input,
            @Nonnull HttpServletResponse output,
            @Nonnull HtmlConfig htmlConfig
    ) {
        this(input, output, htmlConfig, htmlConfig.cloneForAjax());
    }

    public ReqestDispatcher(
            @Nonnull HttpServletRequest input,
            @Nonnull HttpServletResponse output,
            @Nonnull HtmlConfig htmlConfig,
            @Nonnull HtmlConfig ajaxConfig
    ) {
        this.input = input;
        this.output = output;
        this.htmlConfig = htmlConfig;
        this.ajaxConfig = ajaxConfig;

        try {
            final String charset = htmlConfig.getCharset().toString();
            input.setCharacterEncoding(charset);
            output.setCharacterEncoding(charset);

            if (noCache) {
                output.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                output.setHeader("Pragma", "no-cache");
                output.setHeader("Expires", "0");
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Registre new processor.
     *
     * @param key An enumarator type of HttpParameter
     * @param processor processor
     * @return
     */
    public ReqestDispatcher run(@Nonnull final HttpParameter key, @Nonnull final IOConsumer<JsonBuilder> processor) throws IOException, ServletException {
        Assert.isTrue(key instanceof Enum, "Parameter {} is no type of Enum", key);
        if (!done && key.isTrue(input)) {
            try (JsonBuilder builder = JsonBuilder.of(input, output)) {
                done = true;
                processor.accept(builder);
            }
        }
        return this;
    }

    /**
     * Process the request
     */
    public void runDefault(@Nonnull final IORunnable defaultProcessor) throws ServletException, IOException {
        if (!done) {
            try (HtmlElement html = HtmlElement.of(htmlConfig)) {
                defaultProcessor.run(html);
            }
        }
    }
}
