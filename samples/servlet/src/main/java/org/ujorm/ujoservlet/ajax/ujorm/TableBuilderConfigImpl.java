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

import java.time.Duration;
import java.util.List;
import java.util.Arrays;
import java.util.function.Function;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.ujorm.tools.Assert;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.xml.config.HtmlConfig;
import static org.ujorm.ujoservlet.ajax.ujorm.TableBuilderConfigImpl.Constants.*;

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
public class TableBuilderConfigImpl<D> implements TableBuilderConfig<D> {
    
    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(TableBuilderConfigImpl.class.getName());
    
    /** HTML config */
    @Nonnull
    protected final HtmlConfig config;
    /** Link to CSS file */
    @Nonnull
    private String cssLink;
    /** Link to jQuery */
    @Nonnull
    private String jqueryLink;
    /** Iddle delay in millis */
    @Nonnull
    private Duration idleDelay;
    /** AJAX request param */
    @Nonnull
    private HttpParameter ajaxRequestParam;
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

    public TableBuilderConfigImpl(@Nonnull final HtmlConfig config) {
        this(
           config, // config
           BOOTSTRAP_CSS, // cssLink
           JQUERY_JS, // jqueryLink
           IDLE_DELAY, // idleDelay
           JavaScriptWriter.DEFAULT_AJAX_REQUEST_PARAM, // ajaxRequestParam
           FORM_ID, // formId
           CONTROL_CSS, // controlCss
           SUBTITLE_CSS, // subtitleCss
           TABLE_CSS_CLASS // tableCssClass
        );
    }

    public TableBuilderConfigImpl(
            @Nonnull final HtmlConfig config, 
            @Nonnull final String cssLink, 
            @Nonnull final String jqueryLink, 
            @Nonnull final Duration idleDelay, 
            @Nonnull final HttpParameter ajaxRequestParam,
            @Nonnull final String formId, 
            @Nonnull final String controlCss, 
            @Nonnull final String subtitleCss,
            @Nonnull final List<CharSequence> tableCssClass
    ) {
        this.config = config;
        this.cssLink = cssLink;
        this.jqueryLink = jqueryLink;
        this.idleDelay = idleDelay;
        this.formId = formId;
        this.controlCss = controlCss;
        this.subtitleCss = subtitleCss;
        this.tableCssClass = tableCssClass;
        this.ajaxRequestParam = ajaxRequestParam;
    }
    
    /** Returns a fist class of table element by defult */
    @Nonnull
    protected CharSequence getTableClassSelector() {
        return tableCssClass.get(0);
    }
  
    public TableBuilderConfigImpl<D> setCssLink(@Nonnull final String cssLink) {
        this.cssLink = Assert.notNull(cssLink, "cssLink");
        return this;
    }

    public TableBuilderConfigImpl<D> setJqueryLink(@Nonnull final String jqueryLink) {
        this.jqueryLink = Assert.notNull(jqueryLink, "jqueryLink");
        return this;
    }

    public TableBuilderConfigImpl<D> setIdleDelay(@Nonnull final Duration idleDelay) {
        this.idleDelay = Assert.notNull(idleDelay, "idleDelay");
        return this;
    }

    public TableBuilderConfigImpl<D> setAjaxRequestParam(@Nonnull final HttpParameter ajaxRequestParam) {
        this.ajaxRequestParam = Assert.notNull(ajaxRequestParam, "ajaxRequestParam");
        return this;
    }
    
    public TableBuilderConfigImpl<D> setAjaxRequestParam(@Nonnull final CharSequence ajaxReadyMessage) {
        this.ajaxReadyMessage = Assert.hasLength(ajaxReadyMessage, "ajaxReadyMessage");
        return this;
    }

    public TableBuilderConfigImpl<D> setFormId(@Nonnull final String formId) {
        this.formId = Assert.hasLength(formId, "formId");
        return this;
    }

    public TableBuilderConfigImpl<D> setControlCss(@Nonnull final String controlCss) {
        this.controlCss = Assert.hasLength(controlCss, "controlCss");
        return this;
    }

    public TableBuilderConfigImpl<D> setSubtitleCss(@Nonnull final String subtitleCss) {
        this.subtitleCss = Assert.hasLength(subtitleCss, "subtitleCss");
        return this;
    }

    public TableBuilderConfigImpl<D> setTableSelector(@Nonnull final CharSequence tableSelector) {
        this.tableSelector = Assert.hasLength(tableSelector, "tableSelector");
        return this;
    }

    public TableBuilderConfigImpl<D> setTableCssClass(@Nonnull final List<CharSequence> tableCssClass) {
        this.tableCssClass = Assert.notNull(tableCssClass, "tableCssClass");
        return this;
    }
    
    // --- GETTERS ---

    @Nonnull
    public HtmlConfig getConfig() {
        return config;
    }

    @Nonnull
    public String getCssLink() {
        return cssLink;
    }

    @Nonnull
    public String getJqueryLink() {
        return jqueryLink;
    }

    @Nonnull
    public Duration getIdleDelay() {
        return idleDelay;
    }

    @Nonnull
    public HttpParameter getAjaxRequestParam() {
        return ajaxRequestParam;
    }
    
    @Override
    public CharSequence getAjaxRedyMessage() {
        return ajaxReadyMessage;
    }

    @Override
    public String getFormId() {
        return formId;
    }

    @Override
    public String getControlCss() {
        return controlCss;
    }

    @Override
    public String getSubtitleCss() {
        return subtitleCss;
    }

    @Override
    public CharSequence getTableSelector() {
        return tableSelector != null ? tableSelector : getTableCssClass().get(0);
    }

    @Override
    public List<CharSequence> getTableCssClass() {
        return tableCssClass;
    }
    
    /** Config constants */
    public static class Constants {
        /** Link to a Bootstrap URL of CDN */
        protected static final String BOOTSTRAP_CSS = "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";
        /** Link to jQuery of CDN */
        protected static final String JQUERY_JS = "https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js";
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
