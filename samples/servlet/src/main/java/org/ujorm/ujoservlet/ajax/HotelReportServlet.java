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
import org.ujorm.tools.web.ao.Column;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.JsonBuilder;
import org.ujorm.tools.web.ao.Title;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import org.ujorm.ujoservlet.ajax.ao.Hotel;
import org.ujorm.ujoservlet.ajax.ao.HotelResourceService;
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
    /** Form identifier */
    private static final String FORM_ID = "form";
    /** Bootstrap form control CSS class name */
    private static final String CSS_CONTROL = "form-control";
    /** CSS class name for the output box */
    private static final String CSS_OUTPUT = "out";
    /** CSS class name for the output box */
    private static final String CSS_SUBTITLE = "subtitle";
    /** A hotel service */
    private final HotelResourceService service = new HotelResourceService();

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
            html.addJavascriptLink(false, Url.JQUERY_JS);
            html.addCssLink(Url.BOOTSTRAP_CSS);
            html.addCssBody("", getCss());
            writeJavascript((AJAX_ENABLED ? html.getHead() : null), true,
                    "#" + FORM_ID,
                    "." + NAME,
                    "." + CITY);
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
                    form.addInput(CSS_CONTROL, CITY)
                            .setName(CITY)
                            .setValue(CITY.of(input))
                            .setAttribute(Html.A_PLACEHOLDER, "Name of city");
                }
                printTable(body.addDiv(CSS_OUTPUT), input);
                // Data are from hotelsbase.org, see the original license.
                body.addText("Data are from", " ")
                    .addLinkedText(Url.HOTELBASE, "hotelsbase.org");
                body.addText(", ", "see an original", " ")
                    .addLinkedText(Url.DATA_LICENSE, "license");
                body.addBreak();
                body.addAnchor(Url.SOURCE_REPO).addTextTemplated("Version <{}.{}.{}>", 1, 2, 3);
            }
        }
    }

    /** Print table */
    private void printTable(Element root, HttpServletRequest input)
            throws IllegalStateException, IOException {
        CharSequence[] tableCss = {"table", "table-striped", "table-bordered"};
        Object[] tableTitle =
                    { "Name"
                    , "City"
                    , "Street"
                    , "Price"
                    , "Currency"
                    , "Stars"
                    , "Phone"
                    , (Title) e -> e.addText("HomePage", " ").addImage(Url.HELP_IMG, "Help")};
        try (Stream<Hotel> hotels = service.findHotels(15
                    , NAME.of(input, "")
                    , CITY.of(input, ""))) {
            root.addTable(hotels, tableCss, tableTitle
                    , Hotel::getName
                    , Hotel::getCity
                    , Hotel::getStreet
                    , Hotel::getPrice
                    , Hotel::getCurrency
                    , Hotel::getStars
                    , Hotel::getPhone
                    , (Column<Hotel>)(e, v) -> e.addLinkedText(v.getHomePage(), "link")
            );
        }
    }

    /**
     * Return lighlited text in HTML format according a regular expression
     * @param input servlet request
     * @param output A JSON writer
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doAjax(HttpServletRequest input, JsonBuilder output)
            throws ServletException, IOException {
        output.writeClass(CSS_OUTPUT, e -> printTable(e, input));
        output.writeClass(CSS_SUBTITLE, "AJAX ready");
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
        return "body { margin: 10px; }"
                + "#form input { width: 200px;}"
                + ".subtitle{ font-size: 10px; color: silver;}";
    }

    /** URL constants */
    static class Url {

        /** Data license */
        static final String HOTELBASE = "http://hotelbase.org/";
        /** Data license */
        static final String DATA_LICENSE = "https://web.archive.org/web/20150407085757/"
                + "http://api.hotelsbase.org/documentation.php";
        /** Link to a Bootstrap URL of CDN */
        static final String BOOTSTRAP_CSS = "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";
        /** Link to jQuery of CDN */
        static final String JQUERY_JS = "https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js";
        /** Help image */
        static final String HELP_IMG = "images/help.png";
        /** Source of the class */
        static final String SOURCE_REPO = "https://github.com/pponec/ujorm/blob/"
                + "6d6a8e1539f4c3724027687882f689e7ad45f9f5"
                + "/samples/servlet/src/main/java"
                + "/org/ujorm/ujoservlet/ajax/HotelReportServlet.java";
    }

    /** HTTP attributes */
    enum Attrib implements HttpParameter {
        NAME,
        CITY,
        _AJAX {@Override public String toString() {
            return AbstractAjaxServlet.DEFAULT_AJAX_REQUEST_PARAM;
        }};
        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }
}
