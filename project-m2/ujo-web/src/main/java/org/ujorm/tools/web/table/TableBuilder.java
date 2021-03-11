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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.ajax.JavaScriptWriter;
import org.ujorm.tools.web.ajax.ReqestDispatcher;
import org.ujorm.tools.web.ao.Column;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.Injector;
import org.ujorm.tools.web.ao.WebUtils;
import org.ujorm.tools.web.json.JsonBuilder;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 * A HTML page builder for table based an AJAX.
 *
 * <br>Please note that this is an experimental implementation.
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
public class TableBuilder<D> {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(TableBuilder.class.getName());

    /** Columns */
    protected final List<ColumnModel<D,?>> columns = new ArrayList<>();
    /** Table builder config */
    protected final TableBuilderConfig config;
    /** AJAX request param */
    protected HttpParameter ajaxRequestParam = JavaScriptWriter.DEFAULT_AJAX_REQUEST_PARAM;
    /** Extension is empty by default */
    @Nonnull
    protected Injector htmlHeader = e -> {};
    /** Print a config title by default */
    @Nonnull
    protected Injector header = e -> e.addHeading(TableBuilder.this.config.getConfig().getTitle());
    /** Print an empty text by default */
    @Nonnull
    protected Injector footer = e -> e.addText("");
    /** Form injector */
    @Nonnull
    protected Injector formAdditions = footer;
    /** Javascript writer */
    @Nonnull
    protected Supplier<Injector> javascritWriter = () -> new JavaScriptWriter()
            .setSortable(TableBuilder.this.isSortable())
            .setAjax(TableBuilder.this.ajaxEnabled)
            .setSubtitleSelector("." + TableBuilder.this.config.getSubtitleCss());
    /** is An AJAX enabled? */
    protected boolean ajaxEnabled = true;
    /** Call an autosubmit on first load */
    protected boolean autoSubmmitOnLoad = false;
    /** Sorted column index */
    @Nullable
    private int sortedColumn = -1;

    public TableBuilder(@Nonnull CharSequence title) {
        this((HtmlConfig) HtmlConfig.ofDefault().setTitle(title).setNiceFormat());
    }

    public TableBuilder(@Nonnull HtmlConfig config) {
        this(TableBuilderConfig.of(config));
    }

    public TableBuilder(@Nonnull TableBuilderConfig config) {
        this.config = config;
    }

    @Nonnull
    public <V> TableBuilder<D> add(Function<D,V> column) {
        return addInternal(column, "Column-" + (columns.size() + 1), null);
    }

    @Nonnull
    public <V> TableBuilder<D> add(Function<D,V> column, CharSequence title) {
        return addInternal(column, title, null);
    }

    @Nonnull
    public <V> TableBuilder<D> add(Function<D,V> column, Injector title) {
        return addInternal(column, title, null);
    }

    @Nonnull
    public <V> TableBuilder<D> add(Function<D,V> column, CharSequence title, @Nullable HttpParameter param) {
        return addInternal(column, title, param);
    }

    @Nonnull
    public <V> TableBuilder<D> add(Function<D,V> column, Injector title, @Nullable HttpParameter param) {
        return addInternal(column, title, param);
    }

    @Deprecated
    @Nonnull
    public TableBuilder<D> addToElement(Column<D> column, CharSequence title) {
        return addColumn(column, title);
    }

    @Deprecated
    @Nonnull
    public TableBuilder<D> addToElement(Column<D> column, Injector title) {
        return addColumn(column, title);
    }

    @Nonnull
    public TableBuilder<D> addColumn(@Nonnull final Column<D> column, @Nonnull final CharSequence title) {
        return addInternal(column, title, null);
    }

    @Nonnull
    public TableBuilder<D> addColumn(@Nonnull final Column<D> column, @Nonnull final Injector title) {
        return addInternal(column, title, null);
    }

    /** Add new column for a row counting */
    @Nonnull
    public TableBuilder<D> addOrder(@Nonnull final CharSequence title) {
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
    protected <V> TableBuilder<D> addInternal(
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
    public <V> TableBuilder<D> sortable() {
        return sortable(Direction.NONE);
    }
    /**
     * Add a sortable indicator to the last column model
     * @param ascending Ascending or descending direction of the sort
     * @return
     */
    @Nonnull
    public <V> TableBuilder<D> sortable(@Nullable final boolean ascending) {
        return sortable(ascending ? Direction.ASC : Direction.DESC);
    }

    /**
     * Add a sortable indicator to the last column model
     * @param direction The {@code null} value shows an unused sorting action.
     * @return
     */
    @Nonnull
    public <V> TableBuilder<D> sortable(@Nonnull final Direction direction) {
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

    @Nonnull
    public TableBuilder<D> setAjaxRequestParam(@Nonnull HttpParameter ajaxRequestParam) {
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "ajaxRequestParam");
        return this;
    }

    @Nonnull
    public TableBuilder<D> setHtmlHeader(@Nonnull Injector htmlHeader) {
        this.htmlHeader = Assert.notNull(htmlHeader, "htmlHeader");
        return this;
    }

    @Nonnull
    public TableBuilder<D> setHeader(@Nonnull Injector header) {
        this.header = Assert.notNull(header, "header");
        return this;
    }

    @Nonnull
    public TableBuilder<D> setFooter(@Nonnull Injector footer) {
        this.footer = Assert.notNull(footer, "footer");
        return this;
    }

    @Nonnull
    public TableBuilder<D> setFormItem(@Nonnull Injector formItem) {
        this.formAdditions = Assert.notNull(formItem, "formAdditions");
        return this;
    }

    /** Use the method {@link #setFormItem(org.ujorm.tools.web.ao.Injector) } rather. */
    @Deprecated
    @Nonnull
    public TableBuilder<D> setFormAdditions(@Nonnull Injector formItem) {
        return setFormItem(formItem);
    }

    /** Enable of disable an AJAX feature, default value si {@code true} */
    public TableBuilder<D> setAjaxEnabled(boolean ajaxEnabled) {
        this.ajaxEnabled = ajaxEnabled;
        return this;
    }

    public TableBuilder<D> setJavascritWriter(@Nonnull Supplier<Injector> javascritWriter) {
        this.javascritWriter =  Assert.notNull(javascritWriter, "javascritWriter");;
        return this;
    }

    public TableBuilder<D> setEmbeddedIcons(boolean embeddedIcons) throws IllegalStateException {
        if (config instanceof TableBuilderConfigImpl) {
            ((TableBuilderConfigImpl)config).setEmbeddedIcons(embeddedIcons);
        } else {
            throw new IllegalStateException("Configuration must be type of: " + TableBuilderConfigImpl.class);
        }
        return this;
    }

    /** Build the HTML page including a table */
    public void build(
            @Nonnull final HttpServletRequest input,
            @Nonnull final HttpServletResponse output,
            @Nonnull final Stream<D> resource) {
        build(input, output, tableBuilder -> resource);
    }

    /** Build the HTML page including a table */
    public void build(
            @Nonnull final HttpServletRequest input,
            @Nonnull final HttpServletResponse output,
            @Nonnull final Function<TableBuilder<D>, Stream<D>> resource) {
        try {
            setSort(ColumnModel.ofCode(config.getSortRequestParam().of(input)));
            new ReqestDispatcher(input, output, config.getConfig())
                    .onParam(config.getAjaxRequestParam(), jsonBuilder -> doAjax(input, jsonBuilder, resource))
                    .onDefaultToElement(element -> printHtmlBody(input, element, resource));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Internal server error", e);
            output.setStatus(500);
        }
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

    protected void printHtmlBody(
            @Nonnull final HttpServletRequest input,
            @Nonnull final HtmlElement html,
            @Nonnull final Function<TableBuilder<D>, Stream<D>> resource
    ) {
        Assert.notNull(input, "input");
        Assert.notNull(html, "html");
        Assert.notNull(resource, "resource");

        if (Check.hasLength(config.getJavascriptLink())) {
            html.addJavascriptLink(false, config.getJavascriptLink());
        }
        html.addCssLink(config.getCssLink());
        config.getCssWriter().accept(html.getHead(), isSortable());
        javascritWriter.get().write(html.getHead());
        htmlHeader.write(html.getHead());
        try (Element body = html.getBody()) {
            header.write(body);
            body.addDiv(config.getSubtitleCss()).addText(ajaxEnabled ? config.getAjaxReadyMessage() : "");
            try (Element form =  body.addForm()
                    .setId(config.getFormId())
                    .setMethod(Html.V_POST).setAction("?")) {

                for (ColumnModel<D, ?> column : columns) {
                    if (column.isFiltered()) {
                        form.addTextInput(input,
                                column.getParam(),
                                column.getTitle(),
                                config.getControlCss(),
                                column.getParam());
                    }
                }
                if (isSortable()) {
                        form.addInput()
                                .setAttribute(Html.A_TYPE, Html.V_HIDDEN)
                                .setName(config.getSortRequestParam())
                                .setValue(config.getSortRequestParam().of(input));
                }
                form.addInput().setType(Html.V_SUBMIT).setAttribute(Html.V_HIDDEN);
                formAdditions.write(form);
            }
            final List<CharSequence> tableCss = config.getTableCssClass();
            printTableBody(body.addTable(tableCss.toArray(new CharSequence[tableCss.size()])), input, resource);
            footer.write(body);
        }
    }

    protected void printTableBody(
            @Nonnull final Element table,
            @Nonnull final HttpServletRequest input,
            @Nonnull final Function<TableBuilder<D>, Stream<D>> resource
    ) {
        final Element headerElement = table.addElement(Html.THEAD).addElement(Html.TR);
        for (ColumnModel<D,?> col : columns) {
            final boolean columnSortable = col.isSortable();
            final Object value = col.getTitle();
            final Element th = headerElement.addElement(Html.TH);
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
        try (Element tBody = table.addElement(Html.TBODY)) {
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

    /**
     * Return lighlited text in HTML format according a regular expression
     * @param input servlet request
     * @param output A JSON writer
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doAjax(
            @Nonnull final HttpServletRequest input,
            @Nonnull final JsonBuilder output,
            @Nonnull final Function<TableBuilder<D>, Stream<D>> resource
    ) throws ServletException, IOException {
        output.writeClass(config.getTableSelector(), e -> printTableBody(e, input, resource));
        output.writeClass(config.getSubtitleCss(), config.getAjaxReadyMessage());
    }

    /** If the table is sortable */
    protected boolean isSortable() {
        for (ColumnModel<D, ?> column : columns) {
            if (column.isSortable()) {
                return true;
            }
        }
        return false;
    }

    /** URL constants */
    public static class Url {
        /** Link to a Bootstrap URL of CDN */
        protected static final String BOOTSTRAP_CSS = "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";
        /** Link to jQuery of CDN */
        protected static final String JQUERY_JS = "";

        final String bootstrapCss;

        public Url() {
            this(BOOTSTRAP_CSS, JQUERY_JS);
        }

        public Url(@Nonnull final String bootstrapCss, @Nonnull final String jQueryJs) {
            this.bootstrapCss = Assert.hasLength(bootstrapCss, "bootstrapCss");
        }
    }
}
