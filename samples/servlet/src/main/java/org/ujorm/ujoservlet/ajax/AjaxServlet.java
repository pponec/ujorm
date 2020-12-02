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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.HtmlElement;

/**
 * A live example of the HtmlElement inside a servlet using a Dom4j library.
 * @author Pavel Ponec
 */
@WebServlet(AjaxServlet.URL_PATTERN)
public class AjaxServlet extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/AjaxServlet";

    /** Link to a Bootstrap URL */
    private static final String BOOTSTRAP_CSS = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(AjaxServlet.class.toString());

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {

        try (HtmlElement html = HtmlElement.niceOf("Ajax Servlet", output, BOOTSTRAP_CSS)) {
            try (Element body = html.getBody()) {
                body.addHeading(html.getTitle());
                body.addTextTemplated("Data: {}.{}.{} ", 1, 2, 3);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Servlet failed", e);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Servlet failed", e);
        }

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        doGet(input, output);
    }
}
