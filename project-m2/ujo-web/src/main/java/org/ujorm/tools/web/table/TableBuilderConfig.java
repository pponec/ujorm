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
package org.ujorm.tools.web.table;

import java.time.Duration;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 * A HTML page builder for table based an AJAX.
 * 
 * <h3>Usage<h3>
 * 
 * <pre class="pre">
 *  TableBuilder.of("Hotel Report", service.findHotels(ROW_LIMIT, NAME.of(input), CITY.of(input)))
 *          .add(Hotel::getName, "Hotel", NAME)
 *          .add(Hotel::getCity, "City", CITY)
 *          .add(Hotel::getStreet, "Street")
 *          .build(httpServletRequest, HtpServletResponse);
 * </pre>
 * 
 * @author Pavel Ponec
 */
public interface TableBuilderConfig<D> {

    @Nonnull
    public HtmlConfig getConfig();

    @Nonnull
    public String getCssLink();

    @Nonnull
    public String getJqueryLink();

    @Nonnull
    public Duration getIdleDelay();

    @Nonnull
    public HttpParameter getAjaxRequestParam();
    
    @Nonnull
    public CharSequence getAjaxReadyMessage();

    @Nonnull
    public String getFormId();

    @Nonnull
    public String getControlCss();

    @Nonnull
    public String getSubtitleCss();

    @Nonnull
    public CharSequence getTableSelector();

    @Nonnull
    public List<CharSequence> getTableCssClass();

    @Nonnull
    public CharSequence getSortable();

    @Nonnull
    public CharSequence getSortableAsc();

    @Nonnull
    public CharSequence getSortableDesc();

    @Nonnull
    public CharSequence getSortableBoth();
    
    /** Get a CSS direction style */
    default CharSequence getSortableDirection(@Nullable final Boolean ascending) {
        if (ascending == null) {
            return getSortableBoth();
        } else {
            return ascending ? getSortableAsc() : getSortableDesc();
        }
    } 
    
    /** Returns a default implementation */
    @Nonnull
    public static TableBuilderConfigImpl of(@Nonnull final HtmlConfig config) {
        return new TableBuilderConfigImpl(config);
    }
}
