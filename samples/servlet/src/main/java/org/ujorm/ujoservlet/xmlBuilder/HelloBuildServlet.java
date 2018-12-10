/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
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

package org.ujorm.ujoservlet.xmlBuilder;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.dom.HtmlElement;
import org.ujorm.tools.xml.Html;
import org.ujorm.tools.xml.XmlBuilder;
import org.ujorm.tools.xml.XmlPriter;
import org.ujorm.ujoservlet.tools.ApplService;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A live example of the HtmlElement inside a servlet.
 * @author Pavel Ponec
 */
@WebServlet(HelloBuildServlet.URL_PATTERN)
public class HelloBuildServlet extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/helloBuidServlet";

    /** Show the first line of soufce code */
    public static final short SHOW_LINE = 51;

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {

        final XmlPriter writer = XmlPriter.forHtml(output);
        try (XmlBuilder html = new XmlBuilder(Html.HTML, writer)) {

            try (XmlBuilder head = html.addElement(Html.HEAD)) {
                writeHeader(head, "Demo", "userForm.css");
            }
            try (XmlBuilder body = html.addElement(Html.BODY)) {
                body.addElement(Html.H1).addText("Hello, World!");
                ApplService.addFooter(body, this, SHOW_LINE);
            }
        }
    }

    /** Print default header */
    private void writeHeader(XmlBuilder head, String title, String css) throws IOException {
        head.addElement(Html.META, Html.A_CHARSET, UTF_8);
        head.addElement(Html.TITLE).addText(title);
        head.addElement(HtmlElement.Html.LINK
                , HtmlElement.Html.A_HREF, css
                , Html.A_REL, Html.V_STYLESHEET
                , HtmlElement.Html.A_TYPE, Html.V_TEXT_CSS);
    }

    @Override
    protected void doPost(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        doGet(input, output);
    }
}
