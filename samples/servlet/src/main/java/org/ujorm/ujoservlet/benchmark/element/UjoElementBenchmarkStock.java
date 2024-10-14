/*
 * Copyright 2018-2022 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.ujoservlet.benchmark.element;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.ujoservlet.benchmark.Stock;
import org.ujorm.ujoservlet.benchmark.StockService;

/**
 * A live example of the HtmlElement inside a servlet.
 *
 * @see <a href="https://dzone.com/articles/modern-type-safe-template-engines-part-2">...</a>
 * @author Pavel Ponec
 */
@WebServlet(UjoElementBenchmarkStock.URL_PATTERN)
public class UjoElementBenchmarkStock extends HttpServlet {

    /**
     * URL pattern
     */
    public static final String URL_PATTERN = "/ujoElementBenchmarkStock";

    private static final HtmlConfig CONFIG = HtmlConfig.ofDefault().setTitle("Stock Prices");

    /**
     * Inline CSS
     */
    private static final String STOCKS_CSS = getCss();

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        try {
            stock(output);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * <title>Stock Prices</title>
     * <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
     * <meta http-equiv="Content-Style-Type" content="text/css">
     * <meta http-equiv="Content-Script-Type" content="text/javascript">
     * <link rel="shortcut icon" href="/images/favicon.ico">
     * <link rel="stylesheet" type="text/css" href="/css/style.css" media="all">
     * <script type="text/javascript" src="/js/util.js"></script>
     * <style type="text/css">...
     */
    public void stock(HttpServletResponse output) throws IOException, IllegalArgumentException {

        try (HtmlElement html = HtmlElement.ofResponse(output, CONFIG)) {
            try (Element head = html.getHead()) {
                head.addElement(Html.META)
                        .setAttribute("http-equiv", "Content-Type")
                        .setAttribute(Html.A_CONTENT, "text/html; charset=UTF-8");
                head.addElement(Html.META)
                        .setAttribute("http-equiv", "Content-Style-Type")
                        .setAttribute(Html.A_CONTENT, "text/css");
                head.addElement(Html.META)
                        .setAttribute("Content-Script-Type", "text/javascript")
                        .setAttribute(Html.A_CONTENT, "text/javascript");
                head.addElement(Html.LINK)
                        .setAttribute(Html.A_REL, "shortcut icon")
                        .setAttribute(Html.A_CONTENT, "/images/favicon.ico");
                head.addElement(Html.LINK)
                        .setAttribute(Html.A_REL, "stylesheet")
                        .setAttribute(Html.A_HREF, "benchmark.css")
                        .setAttribute(Html.A_MEDIA, "all");
                head.addElement(Html.SCRIPT)
                        .setAttribute(Html.A_TYPE, "text/javascript")
                        .setAttribute(Html.A_SRC, "/js/util.js");
                head.addElement(Html.STYLE)
                        .setAttribute(Html.A_TYPE, "text/css")
                        .addRawText(STOCKS_CSS);
            }
            try (Element body = html.getBody()) {
                body.addElement(Html.H1).addText("Stock Prices");
                Element table = body.addElement(Html.TABLE);
                {
                    Element thead = table.addElement(Html.THEAD);
                    {
                        thead.addElement(Html.TH).addText("#");
                        thead.addElement(Html.TH).addText("symbol");
                        thead.addElement(Html.TH).addText("name");
                        thead.addElement(Html.TH).addText("price");
                        thead.addElement(Html.TH).addText("change");
                        thead.addElement(Html.TH).addText("ratio");
                    }

                    final List<Stock> stocks = dummyItems();
                    for (int itemIndex = 0, max = stocks.size(); itemIndex < max; itemIndex++) {
                        Stock stock = stocks.get(itemIndex);
                        Element row = table.addElement(Html.TR).setAttribute(Html.A_CLASS, itemIndex % 2 == 0 ? "odd" : "even");
                        {
                            row.addElement(Html.TD).addText(String.valueOf(itemIndex + 1));
                            row.addElement(Html.TD).addElement(Html.A).setAttribute(Html.A_HREF, "/stocks/" + stock.getSymbol())
                                    .addText(stock.getSymbol());
                            row.addElement(Html.TD).addElement(Html.A).setAttribute(Html.A_HREF, stock.getUrl())
                                    .addText(stock.getName());
                            row.addElement(Html.TD).addElement("strong").addText(String.valueOf(stock.getPrice()));
                            row.addElement(Html.TD).setAttribute(Html.A_CLASS, stock.getChange() < 0 ? "minus" : null)
                                    .addText(String.valueOf(stock.getChange()));
                            row.addElement(Html.TD).setAttribute(Html.A_CLASS, stock.getRatio() < 0 ? "minus" : null)
                                    .addText(String.valueOf(stock.getRatio()));
                        }
                    }
                }
            }
        }
    }

    public static List<Stock> dummyItems() {
        return StockService.INSTANCE.getStocks();
    }

    /**
     * Get inline CSS
     */
    private static String getCss() {
        return String.join("\n",
                "/*<![CDATA[*/",
                "body {",
                "\tcolor: #333333;",
                "\tline-height: 150%;",
                "}",
                "",
                "thead {",
                "\tfont-weight: bold;",
                "\tbackground-color: #CCCCCC;",
                "}",
                "",
                ".odd {",
                "\tbackground-color: #FFCCCC;",
                "}",
                "",
                ".even {",
                "\tbackground-color: #CCCCFF;",
                "}",
                "",
                ".minus {",
                "\tcolor: #FF0000;",
                "}",
                "",
                "/*]]>*/");
    }

    @Override
    protected void doPost(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        doGet(input, output);
    }
}
