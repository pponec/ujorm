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

package org.ujorm.ujoservlet.xmlElement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.dom.HtmlElement;
import org.ujorm.tools.dom.XmlElement;
import org.ujorm.tools.xml.Html;
import org.ujorm.ujoservlet.tools.ApplService;

/**
 * A live example of the HtmlElement inside a servlet.
 * @author Pavel Ponec
 */
@WebServlet(TableServlet.URL_PATTERN)
public class TableServlet extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/tableServlet";

    /** Show the first line of soufce code */
    public static final short SHOW_LINE = 53;

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        final HtmlElement html = new HtmlElement(getClass().getSimpleName(), StandardCharsets.UTF_8);
        html.addCssLink("css/tableForm.css");
        html.addElementToBody(Html.H1)
                .addText("Show table");
        final XmlElement table = html.addElementToBody(Html.TABLE)
                .setAttrib(Html.A_CLASS, "numbers");
        for (Object[] rowValue : getTableData()) {
            final XmlElement rowElement = table.addElement(Html.TR);
            for (Object value : rowValue) {
                rowElement.addElement(Html.TD)
                        .addText(value);
            }
        }

        ApplService.addFooter(html.getBody(), this, SHOW_LINE);
        html.toResponse(output, true); // Render the result
    }

    /** A number array */
    private Object[][] getTableData() {
        Random random = new Random();
        int size = 20 + 1;
        Object[][] result = new Object[size][size];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                result[i][j] = i == 0
                        ? String.valueOf((char)('A' + j - 1))
                        : j == 0
                        ? i
                        : random.nextInt(500);
            }
        }
        return result;
    }

    @Override
    protected void doPost(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        doGet(input, output);
    }
}
