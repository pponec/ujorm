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

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.ujorm.tools.web.request.RContext;
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
import org.ujorm.tools.web.json.JsonBuilder;
import org.ujorm.tools.web.table.ColumnModel;
import org.ujorm.tools.web.table.Direction;
import org.ujorm.tools.web.table.GridBuilder;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 * A HTML page builder for table based report with an AJAX support.
 *
 * <h3>Usage</h3>
 *
 * <pre class="pre">
 *  ReportBuilder.of("Hotel Report")
 *          .add(Hotel::getName, "Hotel", NAME)
 *          .add(Hotel::getCity, "City", CITY)
 *          .add(Hotel::getStreet, "Street")
 *          .build(ServletRequest, ServletResponse, resource);
 * </pre>
 *
 * @author Pavel Ponec
 */
public class ReportBuilder<D> {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(ReportBuilder.class.getName());
    /** An undefined parameter */
    private static final HttpParameter UNDEFINED_PARAM = HttpParameter.of("UNDEFINED_PARAM");

    /** Grid builder */
    @NotNull
    protected final GridBuilder<D> gridBuilder;
    /** Table builder config */
    protected final ReportBuilderConfig config;
    /** AJAX request param */
    @NotNull
    protected HttpParameter ajaxRequestParam = JavaScriptWriter.DEFAULT_AJAX_REQUEST_PARAM;
    /** Extension is empty by default */
    @NotNull
    protected Injector htmlHeader = e -> {};
    /** Print a config title by default */
    @NotNull
    protected Injector header = e -> e.addHeading(ReportBuilder.this.config.getConfig().getTitle());
    /** Print an empty text by default */
    @NotNull
    protected Injector footer = e -> e.addText("");
    /** Form injector */
    @NotNull
    protected Injector formAdditions = footer;
    /** Javascript writer */
    @NotNull
    protected Supplier<Injector> javascritWriter = () -> new JavaScriptWriter()
            .setAjax(ReportBuilder.this.ajaxEnabled)
            .setSubtitleSelector("." + ReportBuilder.this.config.getSubtitleCss());
    /** is An AJAX enabled? */
    protected boolean ajaxEnabled = true;
    /** Call an autosubmit on first load */
    protected boolean autoSubmmitOnLoad = false;
    /** Sorted column index */
    private int sortedColumn = -1;

    public ReportBuilder(@NotNull CharSequence title) {
        this((HtmlConfig) HtmlConfig.ofDefault().setTitle(title).setNiceFormat());
    }

    public ReportBuilder(@NotNull HtmlConfig config) {
        this(ReportBuilderConfig.of(config));
    }

    public ReportBuilder(@NotNull ReportBuilderConfig config) {
        this(config, new GridBuilder<>(config));
    }

    public ReportBuilder(@NotNull ReportBuilderConfig config, @NotNull GridBuilder<D> builder) {
        this.gridBuilder = builder;
        this.config = config;
    }

    @NotNull
    public <V> ReportBuilder<D> add(Function<D,V> column) {
        gridBuilder.add(column);
        return this;
    }

    @NotNull
    public <V> ReportBuilder<D> add(Function<D,V> column, CharSequence title) {
        gridBuilder.add(column, title);
        return this;
    }

    @NotNull
    public <V> ReportBuilder<D> add(Function<D,V> column, Injector title) {
        gridBuilder.add(column, title);
        return this;
    }

    @NotNull
    public <V> ReportBuilder<D> add(Function<D,V> column, CharSequence title, @Nullable HttpParameter param) {
        gridBuilder.add(column, title, param);
        return this;
    }

    @NotNull
    public <V> ReportBuilder<D> add(Function<D,V> column, Injector title, @Nullable HttpParameter param) {
        gridBuilder.add(column, title, param);
        return this;
    }

    @NotNull
    public ReportBuilder<D> addColumn(@NotNull final Column<D> column, @NotNull final CharSequence title) {
        gridBuilder.add(column, title);
        return this;
    }

    @NotNull
    public ReportBuilder<D> addColumn(@NotNull final Column<D> column, @NotNull final Injector title) {
        gridBuilder.add(column, title);
        return this;
    }

    /** Add new column for a row counting */
    @NotNull
    public ReportBuilder<D> addOrder(@NotNull final CharSequence title) {
        gridBuilder.addOrder(title);
        return this;
    }

    /** Get column model by index */
    public ColumnModel<D,?> getColumn(int index) {
        return gridBuilder.getColumn(index);
    }

    /** Returns a count of columns */
    public int getColumnSize() {
        return gridBuilder.getColumnSize();
    }

    /**
     * Add a sortable indicator to the last column model
     * @return
     */
    @NotNull
    public <V> ReportBuilder<D> sortable() {
        gridBuilder.sortable();
        return this;
    }
    /**
     * Add a sortable indicator to the last column model
     * @param ascending Ascending or descending direction of the sort
     * @return
     */
    @NotNull
    public <V> ReportBuilder<D> sortable(@Nullable final boolean ascending) {
        gridBuilder.sortable(ascending);
        return this;
    }

    /**
     * Add a sortable indicator to the last column model
     * @param direction The {@code null} value shows an unused sorting action.
     * @return
     */
    @NotNull
    public <V> ReportBuilder<D> sortable(@NotNull final Direction direction) {
        gridBuilder.sortable(direction);
        return this;
    }

    /** Get sorted column or a stub if the sorted column not found */
    @NotNull
    public ColumnModel<D,?> getSortedColumn() {
        return gridBuilder.getSortedColumn();
    }

    @NotNull
    public ReportBuilder<D> setAjaxRequestParam(@NotNull HttpParameter ajaxRequestParam) {
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "ajaxRequestParam");
        return this;
    }

    @NotNull
    public ReportBuilder<D> setHtmlHeader(@NotNull Injector htmlHeader) {
        this.htmlHeader = Assert.notNull(htmlHeader, "htmlHeader");
        return this;
    }

    @NotNull
    public ReportBuilder<D> setHeader(@NotNull Injector header) {
        this.header = Assert.notNull(header, "header");
        return this;
    }

    @NotNull
    public ReportBuilder<D> setFooter(@NotNull Injector footer) {
        this.footer = Assert.notNull(footer, "footer");
        return this;
    }

    @NotNull
    public ReportBuilder<D> setFormItem(@NotNull Injector formItem) {
        this.formAdditions = Assert.notNull(formItem, "formAdditions");
        return this;
    }

    /** Enable of disable an AJAX feature, default value si {@code true} */
    public ReportBuilder<D> setAjaxEnabled(boolean ajaxEnabled) {
        this.ajaxEnabled = ajaxEnabled;
        return this;
    }

    public ReportBuilder<D> setJavascritWriter(@NotNull Supplier<Injector> javascritWriter) {
        this.javascritWriter = Assert.notNull(javascritWriter, "javascritWriter");
        return this;
    }

    public ReportBuilder<D> setEmbeddedIcons(boolean embeddedIcons) throws IllegalStateException {
        if (config instanceof ReportBuilderConfigImpl) {
            ((ReportBuilderConfigImpl)config).setEmbeddedIcons(embeddedIcons);
        } else {
            throw new IllegalStateException("Configuration must be type of: " + ReportBuilderConfigImpl.class);
        }
        return this;
    }

    /** Build the HTML page including a table */
    public void build(
            @NotNull final RContext context,
            @NotNull final Stream<D> resource) {
        build(context, tableBuilder -> resource);
    }

    /** Build the HTML page including a table */
    public void build(
            @NotNull final RContext context,
            @NotNull final Function<GridBuilder<D>, Stream<D>> resource) {
        try {
            setSort(ColumnModel.ofCode(config.getSortRequestParam().of(context)));
            new ReqestDispatcher(context, config.getConfig())
                    .onParam(config.getAjaxRequestParam(), jsonBuilder -> doAjax(context, jsonBuilder, resource))
                    .onDefaultToElement(element -> printHtmlBody(context, element, resource));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Internal server error", e);
            throw new IllegalStateException("500"); // TODO.pop
        }
    }

    /** Mark a column as sortable */
    protected void setSort(@NotNull final ColumnModel sort) {
        this.sortedColumn = sort.getIndex();
        if (sortedColumn >= 0) {
            final int[] i = {-1};
            gridBuilder.getColumns().forEach(cm -> {
                ++i[0];
                if (cm.isSortable()) {
                    cm.setDirection(sort.getIndex() == i[0]
                            ? sort.getDirection()
                            : Direction.NONE);
                }
            });
        }
    }

    protected void printHtmlBody(
            @NotNull final RContext context,
            @NotNull final HtmlElement html,
            @NotNull final Function<GridBuilder<D>, Stream<D>> resource
    ) {
        Assert.notNull(context, "context");
        Assert.notNull(html, "html");
        Assert.notNull(resource, "resource");

        if (Check.hasLength(config.getJavascriptLink())) {
            html.addJavascriptLink(false, config.getJavascriptLink());
        }
        html.addCssLink(config.getCssLink());
        config.getCssWriter().accept(html.getHead(), gridBuilder.isSortable());
        javascritWriter.get().write(html.getHead());
        htmlHeader.write(html.getHead());
        try (Element body = html.getBody()) {
            header.write(body);
            body.addDiv(config.getSubtitleCss()).addText(ajaxEnabled ? config.getAjaxReadyMessage() : "");
            try (Element form =  body.addForm()
                    .setId(config.getFormId())
                    .setMethod(Html.V_POST).setAction("?")) {

                gridBuilder.getColumns().forEach(column -> {
                    if (column.isFiltered()) {
                        final HttpParameter param = column.getParam(UNDEFINED_PARAM);
                        form.addTextInp(
                                param,
                                param.of(context),
                                column.getTitle(),
                                config.getControlCss(),
                                column.getParam(UNDEFINED_PARAM));
                    }
                });
                // Hidden submit button is important if a javascript is disabled:
                form.addInput().setType(Html.V_SUBMIT).setAttribute(Html.V_HIDDEN);
                if (gridBuilder.isSortable()) {
                    printSortedField(form.addSpan().setId(config.getSortRequestParam()), context);
                }
                formAdditions.write(form);
                // Add the table:
                final List<CharSequence> tableCss = config.getTableCssClass();
                printTableBody(form.addTable(tableCss.toArray(new CharSequence[tableCss.size()])), context, resource);
            }
            footer.write(body);
        }
    }

    /** The hidden field contains an index of the last sorted column */
    protected void printSortedField(Element parent, final RContext context) {
        final int index = config.getSortRequestParam().of(context, -1);
        parent.addInput().setAttribute(Html.A_TYPE, Html.V_HIDDEN)
                .setNameValue(config.getSortRequestParam(), index);
    }

    protected void printTableBody(
            @NotNull final Element table,
            @NotNull final RContext context,
            @NotNull final Function<GridBuilder<D>, Stream<D>> resource
    ) {
        final ColumnModel sortedColumn = ColumnModel.ofCode(config.getSortRequestParam().of(context));
        this.gridBuilder.build(table, sortedColumn, resource);
    }

    /**
     * Return lighlited text in HTML format according a regular expression
     * @param context servlet context
     * @param output A JSON writer
     * @throws IOException if an I/O error occurs
     */
    protected void doAjax(
            @NotNull final RContext context,
            @NotNull final JsonBuilder output,
            @NotNull final Function<GridBuilder<D>, Stream<D>> resource
    ) throws IOException {
        output.writeClass(config.getTableSelector(), e -> printTableBody(e, context, resource));
        output.writeClass(config.getSubtitleCss(), config.getAjaxReadyMessage());
        if (gridBuilder.isSortable()) {
           output.writeId(config.getSortRequestParam(), e -> printSortedField(e, context));
        }
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

        public Url(@NotNull final String bootstrapCss, @NotNull final String jQueryJs) {
            this.bootstrapCss = Assert.hasLength(bootstrapCss, "bootstrapCss");
        }
    }
}
