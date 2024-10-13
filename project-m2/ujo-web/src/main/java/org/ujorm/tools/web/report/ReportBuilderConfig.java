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
package org.ujorm.tools.web.report;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.table.Direction;
import org.ujorm.tools.web.table.GridBuilderConfig;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 * A HTML page builder for table based an AJAX.
 *
 * <h3>Usage<h3>
 *
 * <pre class="pre">
 *  TableBuilder.of("Hotel Report")
 *          .add(Hotel::getName, "Hotel", NAME)
 *          .add(Hotel::getCity, "City", CITY)
 *          .add(Hotel::getStreet, "Street")
 *          .build(ServletRequest, ServletResponse, resource);
 * </pre>
 *
 * @author Pavel Ponec
 */
public interface ReportBuilderConfig<D> extends GridBuilderConfig<D> {

    @NotNull HtmlConfig getConfig();

    @NotNull String getCssLink();

    /** Link to an external Javascript library where a no-library returns an empty String */
    @NotNull String getJavascriptLink();

    @NotNull Duration getIdleDelay();

    @NotNull HttpParameter getAjaxRequestParam();

    @NotNull HttpParameter getSortRequestParam();

    @NotNull CharSequence getAjaxReadyMessage();

    @NotNull String getFormId();

    @NotNull String getControlCss();

    @NotNull String getSubtitleCss();

    @NotNull CharSequence getTableSelector();

    @NotNull List<CharSequence> getTableCssClass();

    @NotNull CharSequence getSortable();

    @NotNull CharSequence getSortableAsc();

    @NotNull CharSequence getSortableDesc();

    @NotNull CharSequence getSortableBoth();

    /** Use inner icons for sortable images */
    boolean isEmbeddedIcons();

    /** Inline CSS writer where the first method is an Element and the seconnd one is a sortable  */
    BiConsumer<Element, Boolean> getCssWriter();

    /** Get a CSS direction style */
    @NotNull
    default CharSequence getSortableDirection(@NotNull final Direction direction) {
        switch (direction) {
            case ASC:
                return getSortableAsc();
            case DESC:
                return getSortableDesc();
            case NONE:
                return getSortableBoth();
            default:
                throw new IllegalArgumentException("Unsupported " + direction);
        }
    }

    /** Get a CSS direction style */
    @Nullable
    default InputStream getInnerSortableImageToStream(@NotNull final Direction direction) {
        return getClass().getResourceAsStream(getInnerSortableImage(direction));
    }

    /** Get a CSS direction style */
    @NotNull
    default String getInnerSortableImage(@NotNull final Direction direction) {
        final String baseDir = "/META-INF/resources/org/ujorm/images/v1/order";
        switch (direction) {
            case ASC:
                return String.join("/", baseDir, "up.png");
            case DESC:
                return String.join("/", baseDir, "down.png");
            case NONE:
                return String.join("/", baseDir, "both.png");
            default:
                throw new IllegalArgumentException("Unsupported " + direction);
        }
    }

    /** Returns a default implementation */
    @NotNull
    static ReportBuilderConfigImpl of(@NotNull final HtmlConfig config) {
        return new ReportBuilderConfigImpl(config);
    }
}
