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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.ao.HttpParameter;

import org.ujorm.ujoservlet.ajax.ao.Hotel;
import org.ujorm.ujoservlet.ajax.ao.HotelResourceService;
import org.ujorm.ujoservlet.ajax.ujorm.TableBuilder;
import static org.ujorm.ujoservlet.ajax.HotelReportServlet.Attrib.*;

/**
 * A live example of the HtmlElement inside a servlet using a Dom4j library.
 *
 * @author Pavel Ponec
 */
@WebServlet(HotelReportServlet.URL_PATTERN)
public class HotelReportServlet extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/TableHotelServletExt";
    /** A hotel service */
    private final HotelResourceService service = new HotelResourceService();
    /** Row limit */
    private final int ROW_LIMIT = 15;

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

        TableBuilder.of("Hotel Report", service.findHotels(ROW_LIMIT, NAME.of(input), CITY.of(input)))
                .add(Hotel::getName, "Name", NAME)
                .add(Hotel::getCity, "City", CITY)
                .add(Hotel::getStreet, "Street")
                .add(Hotel::getPrice, "Price")
                .add(Hotel::getCurrency, "Currency")
                .add(Hotel::getStars, "Stars")
                .add(Hotel::getPhone, "Phone")
                .addToElement(
                        (e, v) -> e.addLinkedText(v.getHomePage(), "link"), // Column
                        (e) -> e.addText("HomePage", " ").addImage(Url.HELP_IMG, "Help")) // Title
                .setFooter(e -> printFooter(e))
                .build(input, output);
    }
    
    /**  Data are from hotelsbase.org, see the original license */
    protected void printFooter(final Element body) throws IllegalStateException {
        //
        body.addText("Data are from", " ")
                .addLinkedText(Url.HOTELBASE, "hotelsbase.org");
        body.addText(", ", "see an original", " ")
                .addLinkedText(Url.DATA_LICENSE, "license");
        body.addBreak();
        body.addAnchor(Url.SOURCE_REPO).addTextTemplated("Version <{}.{}.{}>", 1, 2, 3);
    }

    /**
     * HTTP attributes
     */
    enum Attrib implements HttpParameter {
        NAME,
        CITY;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
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
                + "58c0d8a170bfa25b8fdc4d5ebbdcd27dbd19c2dd"
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
