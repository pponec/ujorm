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

import java.io.InputStream;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.ao.Column;
import org.ujorm.tools.web.ao.Injector;
import org.ujorm.tools.web.ao.WebUtils;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 * Build a content of a HTML page for a sortable data grid.
 *
 * @author Pavel Ponec
 */
@Deprecated
public class GridBuilderOld<D> extends GridBuilder<D>{

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(GridBuilderOld.class.getName());

    public GridBuilderOld(CharSequence title) {
        super(title);
    }

    public GridBuilderOld(HtmlConfig config) {
        super(config);
    }

    public GridBuilderOld(GridBuilderConfig config) {
        super(config);
    }

    /** Print table body */
    protected void printTable(
            @NotNull final Element table,
            @NotNull final Function<GridBuilder<D>, Stream<D>> resource
    ) {
        final String elementName = table.getName();
        final Element myTable = (Check.isEmpty(elementName) || Html.TABLE.equals(elementName))
                ? table
                : table.addTable();
        final Element headerRow = myTable.addElement(Html.THEAD).addElement(Html.TR);
        for (ColumnModel<D,?> col : columns) {
            final boolean columnSortable = col.isSortable();
            final Object value = col.getTitle();
            final Element th = headerRow.addElement(Html.TH);
            final Element thLink = columnSortable ? th.addAnchor("javascript:f1.sort(" + col.toCode(true) + ")") : th;
            if (columnSortable) {
                thLink.setClass(
                        config.getSortable(),
                        config.getSortableDirection(col.getDirection())
                );
            }
            if (value instanceof Injector) {
                ((Injector)value).write(thLink);
            } else {
                thLink.addText(value);
            }
            if (columnSortable && config.isEmbeddedIcons()) {
                InputStream img = config.getInnerSortableImageToStream(col.getDirection());
                if (img != null) {
                    thLink.addImage(img, col.getDirection().toString());
                }
            }
        }
        try (Element tBody = myTable.addElement(Html.TBODY)) {
            final boolean hasRenderer = WebUtils.isType(Column.class, columns.stream().map(t -> t.getColumn()));
            resource.apply(this).forEach(value -> {
                final Element rowElement = tBody.addElement(Html.TR);
                for (ColumnModel<D, ?> col : columns) {
                    final Function<D, ?> attribute = col.getColumn();
                    final Element td = rowElement.addElement(Html.TD);
                    if (hasRenderer && attribute instanceof Column) {
                        ((Column)attribute).write(td, value);
                    } else {
                        td.addText(attribute.apply(value));
                    }
                }
            });
        }
    }

}
