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
package org.ujorm.ujoservlet.ajax.ujorm;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
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
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.ao.Column;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.JsonBuilder;
import org.ujorm.tools.web.ao.WebUtils;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.web.ao.Injector;

/**
 *
 * @author Pavel Ponec
 */
public class TableBuilder<D> {
    
    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(TableBuilder.class.getName());
    
    /** Form identifier */
    protected static final String FORM_ID = "form";
    /** Bootstrap form control CSS class name */
    protected static final String CONTROL_CSS = "form-control";
    /** CSS class name for the output box */
    protected static final String SUBTITLE_CSS = "subtitle";
    /** Default AJAX request parameter name */
    protected static final HttpParameter DEFAULT_AJAX_REQUEST_PARAM = new HttpParameter() {
            @Override
            public String toString() {
                return "_ajax";
            }
        };
    
    /** Columns */
    protected final List<ColumnModel<D,?>> columns = new ArrayList<>(); 
    /** Data resource */
    protected final Stream<D> resource;
    protected final HtmlConfig config;
    /** Iddle delay in millis */
    protected int idleDelay = 250;
    /** Ajax request param */
    protected HttpParameter ajaxRequestParam = DEFAULT_AJAX_REQUEST_PARAM;
    /** Useful URLs */
    protected Url url = new Url();
    /** Print a config title by default */
    @Nonnull
    protected Injector header = e -> e.addHeading(TableBuilder.this.config.getTitle());
    /** Print an empty text by default */
    @Nonnull
    protected Injector footer = e -> e.addText("");
    /** Form injector */
    @Nonnull
    protected Injector formAdditions = footer;
    /** Javascript writer */
    @Nonnull
    protected Supplier<Injector> javascritWriter = () -> new JavaScriptWriter();
    /** is An AJAX enabled? */
    protected boolean ajaxEnabled = true;
    /** Call an autosubmit on first load */
    protected boolean autoSubmmitOnLoad = false;

    private TableBuilder(@Nonnull Stream<D> resource, @Nonnull HtmlConfig config) {
        this.resource = resource;
        this.config = config;
    }

    @Nonnull
    public static <D> TableBuilder<D> of(@Nonnull Stream<D> resource) {
        return of("Info", resource);
    }

    @Nonnull
    public static <D> TableBuilder<D> of(@Nonnull String title, @Nonnull Stream<D> resource) {
        return of(resource, HtmlConfig.ofDefault().setTitle(title).setNiceFormat());
    }
    
    @Nonnull
    public static <D> TableBuilder<D> of(@Nonnull Stream<D> resource, @Nonnull HtmlConfig config) {
        return new TableBuilder(resource, config);
    }

    @Nonnull
    public <V> TableBuilder<D> add(Function<D,?> column) {
        return addInternal(column, "Column-" + (columns.size() + 1), null);
    }

    @Nonnull
    public <V> TableBuilder<D> add(Function<D,?> column, CharSequence title) {
        return addInternal(column, title, null);
    }
    
    @Nonnull
    public <V> TableBuilder<D> add(Function<D,?> column, Injector title) {
        return addInternal(column, title, null);
    }
    
    @Nonnull
    public <V> TableBuilder<D> add(Function<D,?> column, CharSequence title, @Nullable HttpParameter param) {
        return addInternal(column, title, param);
    }
    
    @Nonnull
    public <V> TableBuilder<D> add(Function<D,?> column, Injector title, @Nullable HttpParameter param) {
        return addInternal(column, title, param);
    }
    
    @Nonnull
    public <V> TableBuilder<D> addToElement(Column<D> column, CharSequence title) {
        return addInternal(column, title, null);
    }

    @Nonnull
    public <V> TableBuilder<D> addToElement(Column<D> column, Injector title) {
        return addInternal(column, title, null);
    }
    
    @Nonnull
    protected <V> TableBuilder<D> addInternal(@Nonnull final Function<D,?> column, @Nonnull final CharSequence title, @Nullable final HttpParameter param) {
        columns.add(new ColumnModel(column, title, param));
        return this;
    }

    @Nonnull
    public TableBuilder<D> setIdleDelay(@Nonnull Duration idleDelay) {
        this.idleDelay = (int) Assert.notNull(idleDelay, "idleDelay").toMillis();
        return this;
    }

    @Nonnull
    public TableBuilder<D> setAjaxRequestParam(@Nonnull HttpParameter ajaxRequestParam) {
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "ajaxRequestParam");
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
    public TableBuilder<D> setFormAdditions(@Nonnull Injector formAdditions) {
        this.formAdditions = Assert.notNull(formAdditions, "formAdditions");
        return this;
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
    
    /** Build the HTML page including a table */
    public void build(HttpServletRequest input, HttpServletResponse output) {    
        try {
            new ReqestDispatcher(input, output, config)
                    .onParam(ajaxRequestParam, jsonBuilder -> doAjax(input, jsonBuilder))
                    .onDefaultToElement(element -> printHtmlBody(input, element));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Internal server error", e);
            output.setStatus(500);
        }
    }
    
    protected void printHtmlBody(HttpServletRequest input, HtmlElement html) {
        html.addJavascriptLink(false, url.jQueryJs);
        html.addCssLink(url.bootstrapCss);
        html.addCssBodies(config.getNewLine(), getHeaderCss());
        if (ajaxEnabled) {
            javascritWriter.get().write(html.getHead());
    //        writeJavascript(html.getHead(), autoSubmmitOnLoad,
    //                "#" + FORM_ID,
    //                "#" + FORM_ID + " input");            
        }
        try (Element body = html.getBody()) {
            header.write(body);
            body.addDiv(SUBTITLE_CSS).addText("");
            try (Element form =  body.addForm()
                    .setId(FORM_ID)
                    .setMethod(Html.V_POST).setAction("?")) {

                for (ColumnModel<D, ?> column : columns) {
                    if (column.isFiltered()) {
                        form.addInput(CONTROL_CSS, column.param)
                                .setName(column.param)
                                .setValue(column.param.of(input, ""))
                                .setAttribute(Html.A_PLACEHOLDER, column.title);                            
                    }

                }
                form.addInput().setType(Html.V_SUBMIT).setAttrib(Html.V_HIDDEN, true);    
                formAdditions.write(form);
            }
            printTableBody(body.addTable(getTableCss()), input);
            footer.write(body);
        }  
    }
    
    protected void printTableBody(Element table, HttpServletRequest input) {
        final Element headerElement = table.addElement(Html.THEAD).addElement(Html.TR);
        for (ColumnModel<D,?> col : columns) {
            final Object value = col.title;
            final Element th = headerElement.addElement(Html.TH);
            if (value instanceof Injector) {
                ((Injector)value).write(th);
            } else {
                th.addText(value);
            }
        }
        try (Element tBody = table.addElement(Html.TBODY)) {
            final boolean hasRenderer = WebUtils.isType(Column.class, columns.stream().map(t -> t.column));
            resource.forEach(value -> {
                final Element rowElement = tBody.addElement(Html.TR);
                for (ColumnModel<D, ?> col : columns) {
                    final Function<D, ?> attribute = col.column;
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
    protected void doAjax(HttpServletRequest input, JsonBuilder output)
            throws ServletException, IOException {
        output.writeClass(getTableClassSelector(), e -> printTableBody(e, input));
        output.writeClass(SUBTITLE_CSS, "AJAX ready");
    }

    /** Returns a fist class of table element by defult */
    @Nonnull
    protected CharSequence getTableClassSelector() {
        return getTableCss()[0];
    }

    /** Default header CSS style definitions */
    @Nonnull
    protected CharSequence[] getHeaderCss() {
        return new CharSequence[] { ""
                , "body { margin: 10px;}"
                , "." + SUBTITLE_CSS + " { font-size: 10px; color: silver;}"
                , "#" + FORM_ID + " { margin-bottom: 2px;}"
                , "#" + FORM_ID + " input { width: 200px;}"
                , "." + CONTROL_CSS + " { display: inline;}"
                , ".table th { background-color: #e8e8e8;}"
        };
    }
    
    /** Table CSS classes */
    protected CharSequence[] getTableCss() {
        return new CharSequence[] 
              { "table" 
              , "table-striped"
              , "table-bordered"};
    }

    class ColumnModel<D,V> {
        @Nonnull
        final Function<D,V> column;
        @Nonnull
        final CharSequence title;
        @Nullable
        final HttpParameter param;

        public ColumnModel(@Nonnull final Function<D, V> column, @Nonnull final CharSequence title, @Nonnull final HttpParameter param) {
            this.column = column;
            this.title = title;
            this.param = param;
        }
        
        public boolean isFiltered() {
            return param != null;
        }
    }
    
    /** URL constants */
    public static class Url {        
        /** Link to a Bootstrap URL of CDN */
        protected static final String BOOTSTRAP_CSS = "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";
        /** Link to jQuery of CDN */
        protected static final String JQUERY_JS = "https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js";
        
        final String bootstrapCss;
        final String jQueryJs;

        public Url() {
            this(BOOTSTRAP_CSS, JQUERY_JS);
        }
        
        public Url(@Nonnull final String bootstrapCss, @Nonnull final String jQueryJs) {
            this.bootstrapCss = Assert.hasLength(bootstrapCss, "bootstrapCss");
            this.jQueryJs = Assert.hasLength(jQueryJs, "jQueryJs");
        }
    }
}
