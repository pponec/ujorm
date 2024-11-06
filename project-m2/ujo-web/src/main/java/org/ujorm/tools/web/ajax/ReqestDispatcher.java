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

import java.io.IOException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

import org.ujorm.tools.Assert;
import org.ujorm.tools.web.request.RContext;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.json.JsonBuilder;
import org.ujorm.tools.web.ao.IOConsumer;
import org.ujorm.tools.web.ao.IOElement;
import org.ujorm.tools.web.ao.IORunnable;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 * A Reqest Dispatcher
 * @author Pavel Ponec
 */
public class ReqestDispatcher {

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(ReqestDispatcher.class.getName());

    @NotNull
    private final RContext context;

    @NotNull
    private final HtmlConfig htmlConfig;

    private boolean done = false;

    /**
     * Disable client cache
     */
    private final boolean noCache = true;

    public ReqestDispatcher(
            @NotNull RContext context) {
        this("Info", context);
    }

    public ReqestDispatcher(
            @NotNull CharSequence title,
            @NotNull RContext context) {
        this(context, HtmlConfig.ofDefault()
                .setTitle(title)
                .setNiceFormat());
    }

    public ReqestDispatcher(
            @NotNull RContext context,
            @NotNull HtmlConfig htmlConfig
    ) {
        this.context = context;
        this.htmlConfig = htmlConfig;
    }

    @NotNull
    public HtmlConfig getAjaxConfig() {
        return htmlConfig.cloneForAjax();
    }

    /**
     * Registre new processor.
     *
     * @param key A key type of HttpParameter
     * @param processor processor
     * @return
     */
    public ReqestDispatcher onParam(@NotNull final HttpParameter key, @NotNull final IOConsumer<JsonBuilder> processor) throws IOException {
        Assert.notNull(key, "Parameter {} is required", "key");
        if (!done && key.of(context, false)) {
            try (JsonBuilder builder = JsonBuilder.of(context.writer(), getAjaxConfig())) {
                done = true;
                processor.accept(builder);
            }
        }
        return this;
    }

    /**
     * The process writes to an element
     */
    public void onDefaultToElement(@NotNull final IOElement defaultProcessor) throws IOException {
        if (!done) {
            try (HtmlElement html = HtmlElement.of(context.writer(), htmlConfig)) {
                defaultProcessor.run(html);
            }
        }
    }

    /**
     * Process the request
     */
    public void onDefault(@NotNull final IORunnable defaultProcessor) throws IOException {
        if (!done) {
            defaultProcessor.run();
        }
    }
}
