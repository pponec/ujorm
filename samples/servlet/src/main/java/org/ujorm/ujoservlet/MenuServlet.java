/*
 * Copyright 2018-2019 Pavel Ponec, https://github.com/pponec
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

package org.ujorm.ujoservlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.xml.model.XmlModel;
import org.ujorm.ujoservlet.tools.HtmlElementOrig;
import org.ujorm.ujoservlet.ajax.RegexpServlet;
import org.ujorm.ujoservlet.ajax.HotelReportServlet;
import org.ujorm.ujoservlet.tools.ApplService;
import org.ujorm.ujoservlet.xmlElement.FormServlet;
import org.ujorm.ujoservlet.xmlElement.HelloServlet;
import org.ujorm.ujoservlet.xmlElement.TableServlet;

/**
 * A live example of the HtmlElement inside a servlet.
 * @author Pavel Ponec
 */
@WebServlet({MenuServlet.URL_PATTER, ""})
public class MenuServlet extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTER = "/menu";

    /** Show the first line of soufce code */
    public static final short SHOW_LINE = 52;

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        final String title = "List of samples (dom)";
        final HtmlElementOrig html = new HtmlElementOrig(title, StandardCharsets.UTF_8, "css/userForm.css");
        html.addElementToBody(Html.H1)
                .addText(title);
        XmlModel list = html.addElementToBody(Html.OL);
        for (Item item : getItems()) {
            list.addElement(Html.LI)
                    .addElement(Html.A)
                    .setAttribute(Html.A_HREF, item.getLink())
                    .addText(item.label);
        }

        ApplService.addFooter(html.getBody(), this, SHOW_LINE);
        html.toResponse(output, true); // Render the result
    }

    /** Form field description data */
    private Item[] getItems() {
        Item[] result = { new Item(HelloServlet.URL_PATTERN, "Hello, World!")
                        , new Item(TableServlet.URL_PATTERN, "Show table")
                        , new Item(FormServlet.URL_PATTERN + "?firstname=It's+Me!", "Simple user form")
                        , new Item(RegexpServlet.URL_PATTERN + "?regexp=%5Bbe%5D&text=abc+def", "Regular expression tester")
                        , new Item(HotelReportServlet.URL_PATTERN + "?name=Hotel", "Hotels report")
                        };
        return result;
    }

   /** Item description */
    static class Item {
        private final String link;
        private final String label;

        public Item(String link, String label) {
            this.link = link;
            this.label = label;
        }
        public String getLink() {
            return link;
        }
        public String getLabel() {
            return label;
        }
    }

    @Override
    protected void doPost(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        doGet(input, output);
    }
}
