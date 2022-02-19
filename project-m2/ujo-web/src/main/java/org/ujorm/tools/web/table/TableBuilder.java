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
package org.ujorm.tools.web.table;

import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.web.report.ReportBuilder;
import org.ujorm.tools.web.report.ReportBuilderConfig;

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
 * @deprecated  Uset the {@link ReportBuilder} class rather.
 */
@Deprecated
public class TableBuilder<D> extends ReportBuilder {

    @Deprecated
    public TableBuilder(CharSequence title) {
        super(title);
    }

    @Deprecated
    public TableBuilder(HtmlConfig config) {
        super(config);
    }

    @Deprecated
    public TableBuilder(ReportBuilderConfig config) {
        super(config);
    }
}
