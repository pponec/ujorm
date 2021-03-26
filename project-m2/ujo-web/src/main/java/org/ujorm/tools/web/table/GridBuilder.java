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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.ao.Column;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.Injector;
import org.ujorm.tools.web.ao.WebUtils;
import org.ujorm.tools.xml.ApiElement;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 * Build a content of a HTML page for a sortable data grid.
 *
 * <br>Please note that this is an experimental implementation.
 *
 * @author Pavel Ponec
 */
public class GridBuilder<D> {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(GridBuilder.class.getName());

    /** Columns */
    protected final List<ColumnModel<D,?>> columns = new ArrayList<>();
    /** Table builder config */
    protected final GridBuilderConfig config;
    /** An order of sorted column whete a negavive value means a descending direction */
    private int sortedColumn = -1;
    /** Is the table sortable */
    private Boolean isSortable;

    public GridBuilder(@Nonnull CharSequence title) {
        this((HtmlConfig) HtmlConfig.ofDefault().setTitle(title).setNiceFormat());
    }

    public GridBuilder(@Nonnull HtmlConfig config) {
        this(GridBuilderConfig.of(config));
    }

    public GridBuilder(@Nonnull GridBuilderConfig config) {
        this.config = config;
    }

    @Nonnull
    public <V> GridBuilder<D> add(Function<D,V> column) {
        return addInternal(column, "Column-" + (columns.size() + 1), null);
    }

    @Nonnull
    public <V> GridBuilder<D> add(Function<D,V> column, CharSequence title) {
        return addInternal(column, title, null);
    }

    @Nonnull
    public <V> GridBuilder<D> add(Function<D,V> column, Injector title) {
        return addInternal(column, title, null);
    }

    @Nonnull
    public <V> GridBuilder<D> add(Function<D,V> column, CharSequence title, @Nullable HttpParameter param) {
        return addInternal(column, title, param);
    }

    @Nonnull
    public <V> GridBuilder<D> add(Function<D,V> column, Injector title, @Nullable HttpParameter param) {
        return addInternal(column, title, param);
    }

    @Nonnull
    public GridBuilder<D> addColumn(@Nonnull final Column<D> column, @Nonnull final CharSequence title) {
        return addInternal(column, title, null);
    }

    @Nonnull
    public GridBuilder<D> addColumn(@Nonnull final Column<D> column, @Nonnull final Injector title) {
        return addInternal(column, title, null);
    }

    /** Add new column for a row counting */
    @Nonnull
    public GridBuilder<D> addOrder(@Nonnull final CharSequence title) {
        final String textRight = "text-right";
        return addColumn(new Column<D>() {
            final AtomicLong order = new AtomicLong();
            @Override
            public void write(final Element e, final D row) {
                e.setClass(Html.A_CLASS, textRight).addText(apply(row), '.');
            }
            @Override
            public Object apply(D t) {
                return order.incrementAndGet();
            }
        }, e -> e.setClass(Html.A_CLASS, textRight).addText(title));
    }

    @Nonnull
    protected <V> GridBuilder<D> addInternal(
            @Nonnull final Function<D,V> column,
            @Nonnull final CharSequence title,
            @Nullable final HttpParameter param) {
        columns.add(new ColumnModel(columns.size(), column, title, param));
        return this;
    }

    /** Get column model by index */
    public ColumnModel<D,?> getColumn(int index) {
        return columns.get(index);
    }

    /** Returns a count of columns */
    public int getColumnSize() {
        return columns.size();
    }

    /**
     * Add a sortable indicator to the last column model
     * @return
     */
    @Nonnull
    public <V> GridBuilder<D> sortable() {
        return sortable(Direction.NONE);
    }
    /**
     * Add a sortable indicator to the last column model
     * @param ascending Ascending or descending direction of the sort
     * @return
     */
    @Nonnull
    public <V> GridBuilder<D> sortable(@Nullable final boolean ascending) {
        return sortable(ascending ? Direction.ASC : Direction.DESC);
    }

    /**
     * Add a sortable indicator to the last column model
     * @param direction The {@code null} value shows an unused sorting action.
     * @return
     */
    @Nonnull
    public <V> GridBuilder<D> sortable(@Nonnull final Direction direction) {
        Assert.notNull(direction, "direction");
        Assert.hasLength(columns, "No column is available");
        columns.get(columns.size() - 1).setSortable(direction);
        return this;
    }

    /** Get sorted column or a stub of the sorted column was not found */
    @Nonnull
    public ColumnModel<D,?> getSortedColumn() {
        return (sortedColumn >= 0 && sortedColumn < getColumnSize())
                ? getColumn(sortedColumn)
                : ColumnModel.ofStub();
    }

//    public GridBuilder<D> setEmbeddedIcons(boolean embeddedIcons) throws IllegalStateException {
//        if (config instanceof ReportBuilderConfigImpl) {
//            ((ReportBuilderConfigImpl)config).setEmbeddedIcons(embeddedIcons);
//        } else {
//            throw new IllegalStateException("Configuration must be type of: " + ReportBuilderConfigImpl.class);
//        }
//        return this;
//    }

    /** Build the HTML page including a table */
    public void build(
            @Nonnull final ApiElement parent,
            @Nonnull final Function<GridBuilder<D>, Stream<D>> resource) {
        printTable(Element.of(parent), resource);
    }

    /** Build the HTML page including a table */
    public void build(
            @Nonnull final ApiElement parent,
            @Nonnull final ColumnModel sortedColumn,
            @Nonnull final Function<GridBuilder<D>, Stream<D>> resource) {

        // An original code: setSort(ColumnModel.ofCode(config.getSortRequestParam().of(input)));
        setSort(Assert.notNull(sortedColumn, "sortedColumn"));
        printTable(Element.of(parent), resource);
    }

    /** Mark a column as sorted */
    protected void setSort(@Nonnull final ColumnModel sort) {
        this.sortedColumn = sort.getIndex();
        if (sortedColumn >= 0) {
            for (int i = 0, max = columns.size(); i < max; i++) {
                final ColumnModel cm = columns.get(i);
                if (cm.isSortable()) {
                    cm.setDirection(sort.getIndex() == i ? sort.getDirection() : Direction.NONE);
                }
            }
        }
    }

    /**
     * Print table
     * @param table If the element name is a {@code "table"} value of an empty text then do not create it.
     * @param resource Data source
     */
    protected void printTable(
            @Nonnull final Element table,
            @Nonnull final Function<GridBuilder<D>, Stream<D>> resource
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

    /** Returns the true in case the table is sortable.
     *
     * NOTE: Calculated result is cached, call the method on a final model only!
     */
    public final boolean isSortable() {
        if (isSortable == null) {
            isSortable = isSortableCalculated();
        }
        return isSortable;
    }

    /** Calculate if the table has an sortable column */
    public boolean isSortableCalculated() {
        for (ColumnModel<D, ?> column : columns) {
            if (column.isSortable()) {
                return true;
            }
        }
        return false;
    }

    /** Returns all table columns in a stream */
    public Stream<ColumnModel<D,?>> getColumns() {
        return columns.stream();
    }
}
