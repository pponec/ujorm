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
import java.util.stream.Stream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.ao.Column;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.table.TableBuilder;
import org.ujorm.ujoservlet.ajax.ao.Hotel;
import org.ujorm.ujoservlet.ajax.ao.HotelResourceService;
import static org.ujorm.ujoservlet.ajax.HotelReportServlet.Attrib.*;
import static org.ujorm.tools.xml.AbstractWriter.NBSP;

/**
 * A live example of the HtmlElement inside a servlet using a Dom4j library.
 *
 * @author Pavel Ponec
 */
@WebServlet(HotelReportServlet.URL_PATTERN)
public class HotelReportServlet extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/TableHotelServlet";
    /** Row limit */
    private static final int DEFAULT_ROW_LIMIT = 15;
    /** CSS for inputs */
    private static final String CSS_INPUT = "form-control";
    /** A hotel service */
    private final HotelResourceService service = new HotelResourceService();

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(
            final HttpServletRequest input,
            final HttpServletResponse output) throws ServletException, IOException {

        new TableBuilder<Hotel>("Hotel Report")
                .addOrder("Ord")
                .add(Hotel::getName, "Hotel", NAME).sortable(true)
                .add(hotel -> hotel.getCity().getName(), "City", CITY).sortable(false)
                .add(Hotel::getStreet, "Street").sortable()
                .add(Hotel::getPrice, "Price").sortable()
                .add(Hotel::getCurrency, "Currency")
                .add(Hotel::getPhone, "Phone")
                .add(starsColumn(), "Stars").sortable()
                .addColumn(
                        (e, v) -> e.addLinkedText(v.getHomePage(), "link"), // Column
                        (e) -> e.addText("Home page", " ").addImage(Url.HELP_IMG, "Help")) // Title
                .setFormItem(e -> e.addTextInp(LIMIT, LIMIT.of(input), "Limit", CSS_INPUT, LIMIT))
                .setFooter(e -> printFooter(e))
                .setAjaxEnabled(true)
                .build(input, output, builder -> service.findHotels(builder,
                                LIMIT.of(input, DEFAULT_ROW_LIMIT),
                                NAME.of(input),
                                CITY.of(input)));
    }

    /** Create a stars Column */
    protected Column<Hotel> starsColumn() {
        return new Column<Hotel>() {
            @Override
            public void write(Element e, Hotel hotel) {
                e.setAttribute(Html.A_TITLE, hotel.getStars()).setAttribute(Html.STYLE, "color: Gold");
                Stream.generate(() -> "🟊" + NBSP).limit(Math.round(hotel.getStars()))
                        .forEach(s -> e.addText(s));
            }
            /** Implement it for a sortable column only */
            @Override
            public Float apply(Hotel hotel) {
                return hotel.getStars();
            }
        };
    }

    /**  Data are from hotelsbase.org, see the original license */
    protected void printFooter(final Element body) throws IllegalStateException {
        body.addText("Data are from", " ")
                .addLinkedText(Url.HOTELBASE, "hotelsbase.org");
        body.addText(", ", "see an original", " ")
                .addLinkedText(Url.DATA_LICENSE, "license");
        body.addBreak();
        body.addAnchor(Url.SOURCE_REPO).addTextTemplated("Source code <{}.{}.{}>", 1, 2, 3);
    }

    /**
     * HTTP attributes
     */
    enum Attrib implements HttpParameter {
        NAME,
        CITY,
        LIMIT { @Override public String defaultValue() {
                return "" + DEFAULT_ROW_LIMIT;
            }
        };

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    /** URL constants */
    static class Url {
        /** Help image */
        static final String HELP_IMG = "images/help.png";
        /** Data license */
        static final String HOTELBASE = "http://hotelbase.org/";
        /** Data license */
        static final String DATA_LICENSE = "https://web.archive.org/web/20150407085757/"
                + "http://api.hotelsbase.org/documentation.php";
        /** Source of the class */
        static final String SOURCE_REPO = "https://github.com/pponec/ujorm/blob/"
                + "master"
                + "/samples/servlet/src/main/java"
                + "/org/ujorm/ujoservlet/ajax/HotelReportServlet.java";
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(
            final HttpServletRequest input,
            final HttpServletResponse output) throws ServletException, IOException {
        doGet(input, output);
    }
}
