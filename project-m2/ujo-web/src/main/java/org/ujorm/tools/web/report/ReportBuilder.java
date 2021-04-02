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
package org.ujorm.tools.web.report;

import java.io.IOException;
import java.util.List;
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
import org.ujorm.tools.web.json.JsonBuilder;
import org.ujorm.tools.web.table.ColumnModel;
import org.ujorm.tools.web.table.Direction;
import org.ujorm.tools.web.table.GridBuilder;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 * A HTML page builder for table based report with an AJAX support.
 *
 * <br>Please note that this is an experimental implementation.
 *
 * <h3>Usage</h3>
 *
 * <pre class="pre">
 *  ReportBuilder.of("Hotel Report")
 *          .add(Hotel::getName, "Hotel", NAME)
 *          .add(Hotel::getCity, "City", CITY)
 *          .add(Hotel::getStreet, "Street")
 *          .build(httpServletRequest, httpServletResponse, resource);
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
    @Nonnull
    protected final GridBuilder<D> gridBuilder;
    /** Table builder config */
    protected final ReportBuilderConfig config;
    /** AJAX request param */
    @Nonnull
    protected HttpParameter ajaxRequestParam = JavaScriptWriter.DEFAULT_AJAX_REQUEST_PARAM;
    /** Extension is empty by default */
    @Nonnull
    protected Injector htmlHeader = e -> {};
    /** Print a config title by default */
    @Nonnull
    protected Injector header = e -> e.addHeading(ReportBuilder.this.config.getConfig().getTitle());
    /** Print an empty text by default */
    @Nonnull
    protected Injector footer = e -> e.addText("");
    /** Form injector */
    @Nonnull
    protected Injector formAdditions = footer;
    /** Javascript writer */
    @Nonnull
    protected Supplier<Injector> javascritWriter = () -> new JavaScriptWriter()
            .setSortable(ReportBuilder.this.gridBuilder.isSortable())
            .setAjax(ReportBuilder.this.ajaxEnabled)
            .setSubtitleSelector("." + ReportBuilder.this.config.getSubtitleCss());
    /** is An AJAX enabled? */
    protected boolean ajaxEnabled = true;
    /** Call an autosubmit on first load */
    protected boolean autoSubmmitOnLoad = false;
    /** Sorted column index */
    @Nullable
    private int sortedColumn = -1;

    public ReportBuilder(@Nonnull CharSequence title) {
        this((HtmlConfig) HtmlConfig.ofDefault().setTitle(title).setNiceFormat());
    }

    public ReportBuilder(@Nonnull HtmlConfig config) {
        this(ReportBuilderConfig.of(config));
    }

    public ReportBuilder(@Nonnull ReportBuilderConfig config) {
        this(config, new GridBuilder<>(config));
    }

    public ReportBuilder(@Nonnull ReportBuilderConfig config, @Nonnull GridBuilder<D> builder) {
        this.gridBuilder = builder;
        this.config = config;
    }

    @Nonnull
    public <V> ReportBuilder<D> add(Function<D,V> column) {
        gridBuilder.add(column);
        return this;
    }

    @Nonnull
    public <V> ReportBuilder<D> add(Function<D,V> column, CharSequence title) {
        gridBuilder.add(column, title);
        return this;
    }

    @Nonnull
    public <V> ReportBuilder<D> add(Function<D,V> column, Injector title) {
        gridBuilder.add(column, title);
        return this;
    }

    @Nonnull
    public <V> ReportBuilder<D> add(Function<D,V> column, CharSequence title, @Nullable HttpParameter param) {
        gridBuilder.add(column, title, param);
        return this;
    }

    @Nonnull
    public <V> ReportBuilder<D> add(Function<D,V> column, Injector title, @Nullable HttpParameter param) {
        gridBuilder.add(column, title, param);
        return this;
    }

    @Nonnull
    public ReportBuilder<D> addColumn(@Nonnull final Column<D> column, @Nonnull final CharSequence title) {
        gridBuilder.add(column, title);
        return this;
    }

    @Nonnull
    public ReportBuilder<D> addColumn(@Nonnull final Column<D> column, @Nonnull final Injector title) {
        gridBuilder.add(column, title);
        return this;
    }

    /** Add new column for a row counting */
    @Nonnull
    public ReportBuilder<D> addOrder(@Nonnull final CharSequence title) {
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
    @Nonnull
    public <V> ReportBuilder<D> sortable() {
        gridBuilder.sortable();
        return this;
    }
    /**
     * Add a sortable indicator to the last column model
     * @param ascending Ascending or descending direction of the sort
     * @return
     */
    @Nonnull
    public <V> ReportBuilder<D> sortable(@Nullable final boolean ascending) {
        gridBuilder.sortable(ascending);
        return this;
    }

    /**
     * Add a sortable indicator to the last column model
     * @param direction The {@code null} value shows an unused sorting action.
     * @return
     */
    @Nonnull
    public <V> ReportBuilder<D> sortable(@Nonnull final Direction direction) {
        gridBuilder.sortable(direction);
        return this;
    }

    /** Get sorted column or a stub of the sorted column was not found */
    @Nonnull
    public ColumnModel<D,?> getSortedColumn() {
        return gridBuilder.getSortedColumn();
    }

    @Nonnull
    public ReportBuilder<D> setAjaxRequestParam(@Nonnull HttpParameter ajaxRequestParam) {
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "ajaxRequestParam");
        return this;
    }

    @Nonnull
    public ReportBuilder<D> setHtmlHeader(@Nonnull Injector htmlHeader) {
        this.htmlHeader = Assert.notNull(htmlHeader, "htmlHeader");
        return this;
    }

    @Nonnull
    public ReportBuilder<D> setHeader(@Nonnull Injector header) {
        this.header = Assert.notNull(header, "header");
        return this;
    }

    @Nonnull
    public ReportBuilder<D> setFooter(@Nonnull Injector footer) {
        this.footer = Assert.notNull(footer, "footer");
        return this;
    }

    @Nonnull
    public ReportBuilder<D> setFormItem(@Nonnull Injector formItem) {
        this.formAdditions = Assert.notNull(formItem, "formAdditions");
        return this;
    }

    /** Use the method {@link #setFormItem(org.ujorm.tools.web.ao.Injector) } rather. */
    @Deprecated
    @Nonnull
    public ReportBuilder<D> setFormAdditions(@Nonnull Injector formItem) {
        return setFormItem(formItem);
    }

    /** Enable of disable an AJAX feature, default value si {@code true} */
    public ReportBuilder<D> setAjaxEnabled(boolean ajaxEnabled) {
        this.ajaxEnabled = ajaxEnabled;
        return this;
    }

    public ReportBuilder<D> setJavascritWriter(@Nonnull Supplier<Injector> javascritWriter) {
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
            @Nonnull final HttpServletRequest input,
            @Nonnull final HttpServletResponse output,
            @Nonnull final Stream<D> resource) {
        build(input, output, tableBuilder -> resource);
    }

    /** Build the HTML page including a table */
    public void build(
            @Nonnull final HttpServletRequest input,
            @Nonnull final HttpServletResponse output,
            @Nonnull final Function<GridBuilder<D>, Stream<D>> resource) {
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

    /** Mark a column as sortable */
    protected void setSort(@Nonnull final ColumnModel sort) {
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
            @Nonnull final HttpServletRequest input,
            @Nonnull final HtmlElement html,
            @Nonnull final Function<GridBuilder<D>, Stream<D>> resource
    ) {
        Assert.notNull(input, "input");
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
                                param.of(input),
                                column.getTitle(),
                                config.getControlCss(),
                                column.getParam(UNDEFINED_PARAM));
                    }
                });
                if (gridBuilder.isSortable()) {
                        form.addInput()
                                .setAttribute(Html.A_TYPE, Html.V_HIDDEN)
                                .setName(config.getSortRequestParam())
                                .setValue(config.getSortRequestParam().of(input));
                }
                form.addInput().setType(Html.V_SUBMIT).setAttribute(Html.V_HIDDEN);
                formAdditions.write(form);
                // Add the table:
                final List<CharSequence> tableCss = config.getTableCssClass();
                printTableBody(form.addTable(tableCss.toArray(new CharSequence[tableCss.size()])), input, resource);
            }
            footer.write(body);
        }
    }

    protected void printTableBody(
            @Nonnull final Element table,
            @Nonnull final HttpServletRequest input,
            @Nonnull final Function<GridBuilder<D>, Stream<D>> resource
    ) {
        final ColumnModel sortedColumn = ColumnModel.ofCode(config.getSortRequestParam().of(input));
        this.gridBuilder.build(table, sortedColumn, resource);
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
            @Nonnull final Function<GridBuilder<D>, Stream<D>> resource
    ) throws ServletException, IOException {
        output.writeClass(config.getTableSelector(), e -> printTableBody(e, input, resource));
        output.writeClass(config.getSubtitleCss(), config.getAjaxReadyMessage());
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
