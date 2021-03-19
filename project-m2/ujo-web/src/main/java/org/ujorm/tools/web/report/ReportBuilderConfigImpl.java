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

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.ajax.JavaScriptWriter;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.xml.config.HtmlConfig;
import static org.ujorm.tools.web.report.ReportBuilderConfigImpl.Constants.*;

/**
 * A HTML page builder for table based an AJAX.
 *
 * <h3>Usage</h3>
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
public class ReportBuilderConfigImpl<D> implements ReportBuilderConfig<D> {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(ReportBuilderConfigImpl.class.getName());

    /** HTML config */
    @Nonnull
    protected final HtmlConfig config;
    /** Link to CSS file */
    @Nonnull
    private String cssLink;
    /** Link to an external JavaScript library where no-library returns an empty String */
    @Nonnull
    private String jqueryLink;
    /** Iddle delay in millis */
    @Nonnull
    private Duration idleDelay;
    /** AJAX request param */
    @Nonnull
    private HttpParameter ajaxRequestParam;
    /** AJAX request param */
    @Nonnull
    private HttpParameter sortRequestParam;
    /** AJA ready param */
    @Nonnull
    private CharSequence ajaxReadyMessage = "AJAX ready";
    /** Form identifier */
    @Nonnull
    private String formId;
    /** Bootstrap form control CSS class name */
    @Nonnull
    private String controlCss;
    /** CSS class name for the output box */
    @Nonnull
    private String subtitleCss;
    /** Table selector */
    @Nonnull
    private CharSequence tableSelector;
    /** Table CSS class */
    @Nonnull
    private List<CharSequence> tableCssClass;
    /** Sortable column CSS style */
    @Nonnull
    private CharSequence sortableColumn;
    /** Sortable column ascending CSS style */
    @Nonnull
    private CharSequence sortableAsc;
    /** Sortable column descending CSS style */
    @Nonnull
    private CharSequence sortableDesc;
    /** Sortable column undefined CSS style */
    @Nonnull
    private CharSequence sortableBoth;
    /** Use an external images for sortable icons */
    private boolean embeddedIcons;
    /** Inline CSS writer */
    @Nullable
    private BiConsumer<Element,Boolean> cssWriter;

    public ReportBuilderConfigImpl(@Nonnull final HtmlConfig config) {
        this(config, // config
           BOOTSTRAP_CSS, // cssLink
           "", // jQueryLink
           IDLE_DELAY, // idleDelay
           JavaScriptWriter.DEFAULT_AJAX_REQUEST_PARAM, // ajaxRequestParam
           JavaScriptWriter.DEFAULT_SORT_REQUEST_PARAM, // sortRequestParam
           FORM_ID, // formId
           CONTROL_CSS, // controlCss
           SUBTITLE_CSS, // subtitleCss
           TABLE_CSS_CLASS, // tableCssClass
           "sortable", // sortableColumn
           "asc",  // sortableAsc
           "desc", // sortableDesc
           "both",  // sortableBoth
           false, // embeddedIcons
           null // cssWriter
        );
    }

    protected ReportBuilderConfigImpl(
            @Nonnull final HtmlConfig config,
            @Nonnull final String cssLink,
            @Nonnull final String jqueryLink,
            @Nonnull final Duration idleDelay,
            @Nonnull final HttpParameter ajaxRequestParam,
            @Nonnull final HttpParameter sortRequestParam,
            @Nonnull final String formId,
            @Nonnull final String controlCss,
            @Nonnull final String subtitleCss,
            @Nonnull final List<CharSequence> tableCssClass,
            @Nonnull final String sortableColumn,
            @Nonnull final String sortableAsc,
            @Nonnull final String sortableDesc,
            @Nonnull final String sortableBoth,
            final boolean embeddedIcons,
            @Nonnull final BiConsumer<Element,Boolean> cssWriter
    ) {
        this.config = config;
        this.cssLink = cssLink;
        this.jqueryLink = jqueryLink;
        this.idleDelay = idleDelay;
        this.ajaxRequestParam = ajaxRequestParam;
        this.sortRequestParam = sortRequestParam;
        this.formId = formId;
        this.controlCss = controlCss;
        this.subtitleCss = subtitleCss;
        this.tableCssClass = tableCssClass;
        this.sortableColumn = sortableColumn;
        this.sortableAsc = sortableAsc;
        this.sortableDesc = sortableDesc;
        this.sortableBoth = sortableBoth;
        this.embeddedIcons = embeddedIcons;
        this.cssWriter = cssWriter;
    }

    /** Returns a fist class of table element by defult */
    @Nonnull
    protected CharSequence getTableClassSelector() {
        return tableCssClass.isEmpty()
                ? Html.TABLE
                : String.join(" .", Html.TABLE, tableCssClass.get(0));
    }

    public ReportBuilderConfigImpl<D> setCssLink(@Nonnull final String cssLink) {
        this.cssLink = Assert.notNull(cssLink, "cssLink");
        return this;
    }

    public ReportBuilderConfigImpl<D> setJqueryLink(@Nonnull final String jqueryLink) {
        this.jqueryLink = Assert.notNull(jqueryLink, "jqueryLink");
        return this;
    }

    public ReportBuilderConfigImpl<D> setIdleDelay(@Nonnull final Duration idleDelay) {
        this.idleDelay = Assert.notNull(idleDelay, "idleDelay");
        return this;
    }

    public ReportBuilderConfigImpl<D> setAjaxRequestParam(@Nonnull final HttpParameter ajaxRequestParam) {
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "ajaxRequestParam");
        return this;
    }

    public ReportBuilderConfigImpl<D> setSortRequestParam(@Nonnull final HttpParameter sortRequestParam) {
        this.sortRequestParam = Assert.notNull(sortRequestParam, "sortRequestParam");
        return this;
    }

    public ReportBuilderConfigImpl<D> setAjaxReadyMessage(@Nonnull final CharSequence ajaxReadyMessage) {
        this.ajaxReadyMessage = Assert.hasLength(ajaxReadyMessage, "ajaxReadyMessage");
        return this;
    }

    public ReportBuilderConfigImpl<D> setFormId(@Nonnull final String formId) {
        this.formId = Assert.hasLength(formId, "formId");
        return this;
    }

    public ReportBuilderConfigImpl<D> setControlCss(@Nonnull final String controlCss) {
        this.controlCss = Assert.hasLength(controlCss, "controlCss");
        return this;
    }

    public ReportBuilderConfigImpl<D> setSubtitleCss(@Nonnull final String subtitleCss) {
        this.subtitleCss = Assert.hasLength(subtitleCss, "subtitleCss");
        return this;
    }

    public ReportBuilderConfigImpl<D> setTableSelector(@Nonnull final CharSequence tableSelector) {
        this.tableSelector = Assert.notNull(tableSelector, "tableSelector");
        return this;
    }

    public ReportBuilderConfigImpl<D> setTableCssClass(@Nonnull final List<CharSequence> tableCssClass) {
        this.tableCssClass = Assert.notNull(tableCssClass, "tableCssClass");
        return this;
    }

    /** Inline CSS writer */
    public ReportBuilderConfigImpl<D> setCssWriter(@Nullable final BiConsumer<Element,Boolean> cssWriter) {
        this.cssWriter = cssWriter;
        return this;
    }

    /** Use an external images for sortable icons */
    public boolean setEmbeddedIcons(boolean embeddedIcons) {
        return this.embeddedIcons = embeddedIcons;
    }

    // --- GETTERS ---

    @Override
    @Nonnull
    public HtmlConfig getConfig() {
        return config;
    }

    @Override
    @Nonnull
    public String getCssLink() {
        return cssLink;
    }

    /** Link to an external Javascript library */
    @Override
    @Nonnull
    public String getJavascriptLink() {
        return jqueryLink;
    }

    @Override
    @Nonnull
    public Duration getIdleDelay() {
        return idleDelay;
    }

    @Override
    @Nonnull
    public HttpParameter getAjaxRequestParam() {
        return ajaxRequestParam;
    }

    @Override
    @Nonnull
    public HttpParameter getSortRequestParam() {
        return sortRequestParam;
    }

    @Override
    @Nonnull
    public CharSequence getAjaxReadyMessage() {
        return ajaxReadyMessage;
    }

    @Override
    @Nonnull
    public String getFormId() {
        return formId;
    }

    @Override
    @Nonnull
    public String getControlCss() {
        return controlCss;
    }

    @Override
    @Nonnull
    public String getSubtitleCss() {
        return subtitleCss;
    }

    @Override
    @Nonnull
    public CharSequence getTableSelector() {
        return tableSelector != null ? tableSelector : getTableCssClass().get(0);
    }

    @Override
    @Nonnull
    public List<CharSequence> getTableCssClass() {
        return tableCssClass;
    }

    /** Sortable CSS class */
    @Override
    @Nonnull
    public CharSequence getSortable() {
        return sortableColumn;
    }

    /** Sortable ascending CSS class */
    @Override
    @Nonnull
    public CharSequence getSortableAsc() {
        return sortableAsc;
    }

    /** Sortable descending CSS class */
    @Override
    @Nonnull
    public CharSequence getSortableDesc() {
        return sortableDesc;
    }

    /** Sortable both CSS class */
    @Override
    @Nonnull
    public CharSequence getSortableBoth() {
        return sortableBoth;
    }

    /** Inline CSS writer where a default value is generated from the {@link #inlineCssWriter() } method.
     * } */
    @Override
    @Nonnull
    public BiConsumer<Element, Boolean> getCssWriter() {
        return cssWriter != null ? cssWriter : inlineCssWriter();
    }

    /** Use an external images for sortable icons */
    public boolean isEmbeddedIcons() {
        return this.embeddedIcons;
    }

    /** Default header CSS style printer */
    @Nonnull
    protected BiConsumer<Element,Boolean> inlineCssWriter() {
        return (Element element, Boolean sortable) -> {
            final ReportBuilderConfig conf = this;
            final CharSequence newLine = conf.getConfig().getNewLine();
            try (Element css = element.addElement(Html.STYLE)) {
                css.addRawText(newLine, "body { margin: 10px;}");
                css.addRawText(newLine, ".", conf.getSubtitleCss(), " {font-size: 10px; color: silver;}");
                css.addRawText(newLine, "#", conf.getFormId(), " {margin-bottom: 2px;}");
                css.addRawText(newLine, "#", conf.getFormId(), " input {width: 200px;}");
                css.addRawText(newLine, ".", conf.getControlCss(), " {display: inline;}");
                css.addRawText(newLine, ".table th {background-color: #e8e8e8;}");
                if (Boolean.TRUE.equals(sortable)) {
                    if (isEmbeddedIcons()) {
                        css.addRawText(newLine, ".sortable img {margin-left: 6px;} ");
                    } else {
                        final String img = "/org/ujorm/images/v1/order/";
                        css.addRawText(newLine, ".", conf.getSortable()
                                , " {background-repeat: no-repeat;"
                                + " background-position: right;"
                                + " padding-right: 14px; color: #212529;}");
                        css.addRawText(newLine, ".", conf.getSortable(), ".", conf.getSortableAsc(),  " {background-image: url('", img, "up"  , ".png')}");
                        css.addRawText(newLine, ".", conf.getSortable(), ".", conf.getSortableDesc(), " {background-image: url('", img, "down", ".png')}");
                        css.addRawText(newLine, ".", conf.getSortable(), ".", conf.getSortableBoth(), " {background-image: url('", img, "both", ".png')}");
                    }
                }
            }
        };
    }

    /** Config constants */
    public static class Constants {
        /** Link to a Bootstrap URL of CDN */
        public static final String BOOTSTRAP_CSS = "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";
        /** Form identifier */
        public static final String FORM_ID = "form";
        /** Bootstrap form control CSS class name */
        public static final String CONTROL_CSS = "form-control";
        /** CSS class name for the output box */
        public static final String SUBTITLE_CSS = "subtitle";
        /** Table CSS classes */
        public static List<CharSequence> TABLE_CSS_CLASS = Arrays.asList("table", "table-striped", "table-bordered");
        /** Key delay */
        public static final Duration IDLE_DELAY = Duration.ofMillis(250);
    }
}
