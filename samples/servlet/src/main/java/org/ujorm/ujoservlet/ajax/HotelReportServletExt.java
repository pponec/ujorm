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
@WebServlet(HotelReportServletExt.URL_PATTERN)
public class HotelReportServletExt extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/TableHotelServletExt";
    /** Help image */
    static final String HELP_IMG = "images/help.png";
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

        TableBuilder.of("Report", service.findHotels(20, NAME.of(input), CITY.of(input)))
                .add(Hotel::getName, "Name", NAME)
                .add(Hotel::getCity, "City", CITY)
                .add(Hotel::getStreet, "Street")
                .add(Hotel::getPrice, "Price")
                .add(Hotel::getCurrency, "Currency")
                .add(Hotel::getStars, "Stars")
                .add(Hotel::getPhone, "Phone")
                .addToElement(
                        (e, v) -> e.addLinkedText(v.getHomePage(), "link"), 
                        (e) -> e.addText("HomePage", " ").addImage(HELP_IMG, "Help"))
                .build(input, output);
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
