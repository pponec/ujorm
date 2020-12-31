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
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.ao.Column;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.Title;
import org.ujorm.tools.web.ao.WebUtils;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 *
 * @author Pavel Ponec
 */
public class TableBuilder<D> {
    
    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(TableBuilder.class.getName());
    
    /** Form identifier */
    private static final String FORM_ID = "form";
    /** Bootstrap form control CSS class name */
    private static final String CONTROL_CSS = "form-control";
    /** CSS class name for the output box */
    private static final String OUTPUT_CSS = "out";
    /** CSS class name for the output box */
    private static final String SUBTITLE_CSS = "subtitle";
    
    /** Columns */
    private final List<ColumnModel<D,?>> columns = new ArrayList<>();
    
    /** Data resource */
    private final Stream<D> resource;
    private final HtmlConfig config;
    /** Iddle delay in millis */
    private int idleDelay = 250;
    /** Ajax request param */
    private String ajaxRequest = "_ajax";

    private TableBuilder(@Nonnull Stream<D> resource, @Nonnull HtmlConfig config) {
        this.resource = resource;
        this.config = config;
    }

    public static <D> TableBuilder<D> of(@Nonnull Stream<D> resource) {
        return of(resource, "Info");
    }

    public static <D> TableBuilder<D> of(@Nonnull Stream<D> resource, @Nonnull String title) {
        return of(resource, HtmlConfig.ofDefault().setTitle(title).setNiceFormat());
    }
    
    public static <D> TableBuilder<D> of(@Nonnull Stream<D> resource, @Nonnull HtmlConfig config) {
        return new TableBuilder(resource, config);
    }

    public <V> TableBuilder<D> add(Function<D,?> column) {
        return add(column, "col-" + columns.size());
    }

    public <V> TableBuilder<D> add(Function<D,?> column, CharSequence title) {
        columns.add(new ColumnModel(column, title));
        return this;
    }
    
    public <V> TableBuilder<D> add(Function<D,?> column, CharSequence title, @Nullable HttpParameter param) {
        columns.add(new ColumnModel(column, title, param));
        return this;
    }
    
    public <V> TableBuilder<D> addColumn(Column<D> column, CharSequence name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void build(HttpServletRequest input, HttpServletResponse output) {

        try (HtmlElement html = HtmlElement.of(input, output, config)) {
            html.addJavascriptLink(false, Url.JQUERY_JS);
            html.addCssLink(Url.BOOTSTRAP_CSS);
            html.addCssBodies("\n", getCss());
            writeJavascript(html.getHead(), true,
                    "#" + FORM_ID,
                    "#" + FORM_ID + " input");
            try (Element body = html.getBody()) {
                body.addHeading(html.getTitle());
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
                }
                printTable(body.addDiv(OUTPUT_CSS), input);
                printFooter(body);
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Internal server error");
            output.setStatus(500);
        }
    }
    
    protected void printTable(Element parent, HttpServletRequest input) {
        final Element table = parent.addTable();
        final Element headerElement = table.addElement(Html.TR);
        for (ColumnModel<D,?> col : columns) {
            final Object value = col.title;
            final Element th = headerElement.addElement(Html.TH);
            if (value instanceof Title) {
                ((Title)value).accept(th);
            } else {
                th.addText(value);
            }
        }

        final boolean hasRenderer = WebUtils.isType(Column.class, columns.stream().map(t -> t.column));
        resource.forEach(value -> {
            final Element rowElement = table.addElement(Html.TR);
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
    
    protected void printFooter(final Element body) throws IllegalStateException {
    }

    /** Default CSS styles */
    @Nonnull
    protected CharSequence[] getCss() {
        return new CharSequence[] { "body { margin: 10px;}"
                , "." + SUBTITLE_CSS + " { font-size: 10px; color: silver;}"
                , "#" + FORM_ID + " { margin-bottom: 2px;}"
                , "#" + FORM_ID + " input { width: 200px;}"
                , "." + CONTROL_CSS + " { display: inline;}"};
    }

    class ColumnModel<D,V> {
        @Nonnull
        final Function<D,V> column;
        @Nonnull
        final CharSequence title;
        @Nullable
        final HttpParameter param;

        public ColumnModel(@Nonnull Function<D, V> column, @Nonnull CharSequence title) {
            this(column, title, null);
        }

        public ColumnModel(@Nonnull Function<D, V> column, @Nonnull CharSequence title, @Nonnull HttpParameter param) {
            this.column = column;
            this.title = title;
            this.param = param;
        }
        
        public boolean isFiltered() {
            return param != null;
        }
    }
    
    /**
     * Generate a Javascript
     * @param element Root element, where {@code null} value disable the javascript.
     * @param initFormSubmit Submit on the first form on load request
     * @param formSelector A form selector for submit
     * @param inputCssSelectors Array of CSS selector for autosubmit.
     */
    @Nonnull
    protected void writeJavascript(
            @Nullable final Element element,
            final boolean initFormSubmit,
            @Nullable final CharSequence formSelector,
            @Nonnull final CharSequence... inputCssSelectors) {
        if (element == null) {
            return;
        }
        CharSequence newLine = config.getNewLine();
        element.addRawTexts(newLine, newLine, "<script>", "$(document).ready(function(){");
        if (Check.hasLength(inputCssSelectors)) {
                    final String inpSelectors = Stream.of(inputCssSelectors)
              //.map(t -> "." + t)
                .collect(Collectors.joining(", "));

            element.addRawTexts(newLine, newLine
                    , "var globalTimeout = null;"
                    , "$('" + inpSelectors + "').keyup(function() {"
                    , "  if (globalTimeout != null) {"
                    , "    clearTimeout(globalTimeout);"
                    , "  }"
                    , "  globalTimeout = setTimeout(function() {"
                    , "    globalTimeout = null;"
                    , "    $('" + formSelector + "').submit();"
                    , "  }, " + idleDelay + ");"
                    , "});"
            );
        }{
            element.addRawTexts(newLine, newLine
                    , "$('form').submit(function(event){"
                    , "  var data = $('" + formSelector + "').serialize();"
                    , "  $.ajax("
                          + "{ url: '?" + ajaxRequest + "=true'"
                          + ", type: 'POST'"
                          + ", data: data"
                          + ", timeout: 3000"
                          + ", error: function (xhr, ajaxOptions, thrownError) {"
                          ,  "   $('.subtitle').html('AJAX fails due: ' + thrownError);"
                          +  " }"
                    ,       ", success: function(result){"
                    , "    var jsn = JSON.parse(result);"
                    , "    $.each(jsn, function(key, value){"
                    , "      $(key).html(value);"
                    , "    })"
                    , "  }});"
                    , "  event.preventDefault();"
                    , "});"
                    , initFormSubmit ? "  $('" + formSelector + "').submit();" : ""
                    );
        }
        element.addRawTexts(newLine, newLine, "});", "</script>");
    }
    
    /** URL constants */
    static class Url {
        /** Link to a Bootstrap URL of CDN */
        static final String BOOTSTRAP_CSS = "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";
        /** Link to jQuery of CDN */
        static final String JQUERY_JS = "https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js";
    }
}
