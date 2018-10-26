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

package org.ujorm.ujoservlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.HtmlElement;
import org.ujorm.tools.XmlElement;
import org.ujorm.ujoservlet.tools.Html;
import org.ujorm.ujoservlet.tools.HtmlTools;

/**
 * A live example of the HtmlElement inside a servlet.
 * @author Pavel Ponec
 */
public class MenuServlet extends HttpServlet {

    /** Show the first line of soufce code */
    public static final short SHOW_LINE = 46;

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        input.setCharacterEncoding(HtmlTools.CODE_PAGE.toString());

        final String title = "List of samples";
        final HtmlElement html = new HtmlElement(title, HtmlTools.CODE_PAGE);
        html.addCssLink("welcomeForm.css");
        html.addElementToBody(Html.H1)
                .addText(title);
        XmlElement list = html.addElementToBody(Html.OL);
        for (Item item : getItems(title)) {
            list.addElement(Html.LI)
                    .addElement(Html.A)
                    .addAttrib(Html.A_HREF, item.getLink())
                    .addText(item.label);
        }

        HtmlTools.addFooter(html.getBody(), this, SHOW_LINE);
        html.toResponse(output, true); // Render the result
    }

    /** Form field description data */
    private Item[] getItems(String title) {
        Item[] result = { new Item("helloServlet", "Hello, World!")
                       , new Item("tableServlet", "Show table")
                       , new Item("formServlet?firstname=It's+Me!", "Simple user form")};
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

    /** No implementation */
    @Override
    protected void doPost(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
    }
}
