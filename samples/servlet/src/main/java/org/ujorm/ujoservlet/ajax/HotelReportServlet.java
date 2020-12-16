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
package org.ujorm.ujoservlet.ajax;

import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.JsonWriter;
import org.ujorm.tools.web.ao.Renderer;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import org.ujorm.ujoservlet.ajax.ao.Hotel;
import org.ujorm.ujoservlet.ajax.ao.ResourceService;
import static org.ujorm.ujoservlet.ajax.HotelReportServlet.Attrib.*;

/**
 * A live example of the HtmlElement inside a servlet using a Dom4j library.
 * @author Pavel Ponec
 */
@WebServlet(HotelReportServlet.URL_PATTERN)
public class HotelReportServlet extends AbstractAjaxServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/TableHotelServlet";
    /** Enable AJAX feature */
    private static final boolean AJAX_ENABLED = true;
    /** Data license */
    private static final String HOTELBASE_URL = "http://hotelbase.org/";
    /** Data license */
    private static final String DATA_LICENSE_URL = "https://web.archive.org/web/20150407085757/http://api.hotelsbase.org/documentation.php";
    /** Link to a Bootstrap URL of CDN */
    private static final String BOOTSTRAP_CSS = "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";
    /** Link to jQuery of CDN */
    private static final String JQUERY_JS = "https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js";
    /** Source of the class */
    private static final String SOURCE_URL = "https://github.com/pponec/ujorm/"
            + "blob/e1b4fde571761c4cdbfd8877a7fe4dd054256c03/samples/servlet/src/main/java/org/ujorm/ujoservlet/ajax/RegexpServlet.java";
    /** Form identifier */
    private static final String FORM_ID = "form";
    /** Bootstrap form control CSS class name */
    private static final String CSS_CONTROL = "form-control";
    /** CSS class name for the output box */
    private static final String CSS_OUTPUT = "out";
    /** CSS class name for the output box */
    private static final String CSS_SUBTITLE = "subtitle";
    /** A common service */
    private final ResourceService service = new ResourceService();

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doProcess(
            final HttpServletRequest input,
            final HttpServletResponse output,
            final boolean post) throws ServletException, IOException {
        try (HtmlElement html = HtmlElement.of(input, output, getConfig("Hotel report"))) {
            html.addJavascriptLink(false, JQUERY_JS);
            html.addCssLink(BOOTSTRAP_CSS);
            html.addCssBody("", getCss());
            writeJavascript((AJAX_ENABLED ? html.getHead() : null), true, "#" + FORM_ID, NAME, STREET);
            try (Element body = html.getBody()) {
                body.addHeading(html.getTitle());
                body.addDiv(CSS_SUBTITLE).addText("");
                try (Element form =  body.addForm()
                        .setId(FORM_ID)
                        .setMethod(Html.V_POST).setAction("?")) {
                    form.addInput(CSS_CONTROL, NAME)
                            .setName(NAME)
                            .setValue(NAME.of(input))
                            .setAttribute(Html.A_PLACEHOLDER, "Name of hotel");
                    form.addInput(CSS_CONTROL, STREET)
                            .setName(STREET)
                            .setValue(STREET.of(input))
                            .setAttribute(Html.A_PLACEHOLDER, "Street");
                }

                CharSequence[] tableCss = {"table", "table-striped", "table-bordered"};
                Object[] tableTitle = {"Name", "City ID", "Street", "Price", "Currency", "Stars", "Phone", "HomePage"};
                try (Stream<Hotel> hotels = service.loadHotelStream()) {
                    body.addDiv(CSS_OUTPUT)
                         .addTable(hotels, tableCss, tableTitle
                            , Hotel::getName
                            , Hotel::getCity
                            , Hotel::getStreet
                            , Hotel::getPrice
                            , Hotel::getCurrency
                            , Hotel::getStars
                            , Hotel::getPhone
                            , (Renderer<Hotel>)(e, v) -> e.addLinkedText(v.getHomePage(), "link")
                    );
                }

                body.addElement(Html.HR);

                // Data are from hotelsbase.org, see the original license.
                body.addText("Data are from", " ")
                    .addLinkedText(HOTELBASE_URL, "hotelsbase.org");
                body.addText(", ", "see an original", " ")
                    .addLinkedText(DATA_LICENSE_URL, "license");
                body.addBreak();
                body.addAnchor(SOURCE_URL).addTextTemplated("Version <{}.{}.{}>", 1, 2, 3);
            }
        }
    }

    /**
     * Return lighlited text in HTML format according a regular expression
     * @param input servlet request
     * @param output A JSON writer
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doAjax(HttpServletRequest input, JsonWriter output) throws ServletException, IOException {

        String name = NAME.of(input, "").toUpperCase();
        String street = STREET.of(input, "").toUpperCase();

        try (Stream<Hotel> hotels = service.loadHotelStream()
                                .filter(t -> t.getName().toUpperCase().startsWith(name))
                                .filter(t -> t.getStreet().toUpperCase().startsWith(street))
                                .limit(15)) {

            StringBuilder out = new StringBuilder(256);
            try (HtmlElement html = HtmlElement.of(HtmlConfig.ofElementName("div"), out)) {
                    CharSequence[] tableCss = {"table", "table-striped", "table-bordered"};
                    Object[] tableTitle = {"Name", "City ID", "Street", "Price", "Currency", "Stars", "Phone", "HomePage"};
                    html.addElement(CSS_OUTPUT)
                         .addTable(hotels, tableCss, tableTitle
                            , Hotel::getName
                            , Hotel::getCity
                            , Hotel::getStreet
                            , Hotel::getPrice
                            , Hotel::getCurrency
                            , Hotel::getStars
                            , Hotel::getPhone
                            , (Renderer<Hotel>)(e, v) -> e.addLinkedText(v.getHomePage(), "link")
                    );
            }
            // Write a selector with a value:
            output.writeClass(CSS_OUTPUT, out);
            output.writeClass(CSS_SUBTITLE, "AJAX ready");
        }
    }

    /** Create a configuration of HTML model */
    private DefaultHtmlConfig getConfig(String title) {
        DefaultHtmlConfig config;
        config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        return config;
    }

    private String getCss() {
        return "#form input { width: 200px;}";
    }

    /** Servlet attributes */
    enum Attrib implements HttpParameter {
        NAME,
        CITY,
        STREET,
        _AJAX {@Override public String toString() {
            return AbstractAjaxServlet.DEFAULT_AJAX_REQUEST_PARAM;
        }};
        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }
}
