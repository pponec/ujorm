/*
 * Copyright 2020-2021 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.web.table;

import org.ujorm.tools.web.report.*;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.ao.HttpParameter;
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
 *          .build(httpServletRequest, httpServletResponse, resource);
 * </pre>
 *
 * @author Pavel Ponec
 */
public interface GridBuilderConfig<D> {

    @NotNull
    public HtmlConfig getConfig();

    @NotNull
    public String getCssLink();

    /** Link to an external Javascript library where a no-library returns an empty String */
    @NotNull
    public String getJavascriptLink();

    @NotNull
    public Duration getIdleDelay();

    @NotNull
    public HttpParameter getAjaxRequestParam();

    @NotNull
    public HttpParameter getSortRequestParam();

    @NotNull
    public CharSequence getAjaxReadyMessage();

    @NotNull
    public String getFormId();

    @NotNull
    public String getControlCss();

    @NotNull
    public String getSubtitleCss();

    @NotNull
    public CharSequence getTableSelector();

    @NotNull
    public List<CharSequence> getTableCssClass();

    @NotNull
    public CharSequence getSortable();

    @NotNull
    public CharSequence getSortableAsc();

    @NotNull
    public CharSequence getSortableDesc();

    @NotNull
    public CharSequence getSortableBoth();

    /** Use inner icons for sortable images */
    public boolean isEmbeddedIcons();

    /** Inline CSS writer where the first method is an Element and the seconnd one is a sortable  */
    public BiConsumer<Element, Boolean> getCssWriter();

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
    public static GridBuilderConfig of(@NotNull final HtmlConfig config) {
        return new ReportBuilderConfigImpl(config);
    }
}
