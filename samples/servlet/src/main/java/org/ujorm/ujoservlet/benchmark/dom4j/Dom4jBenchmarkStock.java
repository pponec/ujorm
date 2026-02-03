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
package org.ujorm.ujoservlet.benchmark.dom4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.tree.FlyweightText;
import org.ujorm.tools.web.Html;
import org.ujorm.ujoservlet.benchmark.Stock;
import org.ujorm.ujoservlet.benchmark.StockService;

/**
 * A live example of the HtmlElement inside a servlet.
 * @see https://dzone.com/articles/modern-type-safe-template-engines-part-2
 * @author Pavel Ponec
 */
@WebServlet(Dom4jBenchmarkStock.URL_PATTERN)
public class Dom4jBenchmarkStock extends HttpServlet {

    /* A common code page form request and response. Try the {@code  Charset.forName("windows-1250")} for example. */
    private static final String charset = StandardCharsets.UTF_8.toString();

    /** URL pattern */
    public static final String URL_PATTERN = "/dom4jBenchmarkStock";

    /** Inline CSS */
    private static final String STOCKS_CSS = getCss();

    /**
     * Handles the HTTP <code>GET</code> method.
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

        Document document = DocumentHelper.createDocument();
        Element html = document.addElement(Html.HTML);
        Element head = html.addElement(Html.HEAD);
        {
            head.addElement(Html.TITLE)
                    .addText("Stock Prices");
            head.addElement(Html.META)
                    .addAttribute("http-equiv", "Content-Type")
                    .addAttribute(Html.A_CONTENT, "text/html; charset=UTF-8");
            head.addElement(Html.META)
                    .addAttribute("http-equiv", "Content-Style-Type")
                    .addAttribute(Html.A_CONTENT, "text/css");
            head.addElement(Html.META)
                    .addAttribute("Content-Script-Type", "text/javascript")
                    .addAttribute(Html.A_CONTENT, "text/javascript");
            head.addElement(Html.LINK)
                    .addAttribute(Html.A_REL, "shortcut icon")
                    .addAttribute(Html.A_CONTENT, "/images/favicon.ico");
            head.addElement(Html.LINK)
                    .addAttribute(Html.A_REL, "stylesheet")
                    .addAttribute(Html.A_HREF, "benchmark.css")
                    .addAttribute(Html.A_MEDIA, "all");
            head.addElement(Html.SCRIPT)
                    .addAttribute(Html.A_TYPE, "text/javascript")
                    .addAttribute(Html.A_SRC, "/js/util.js");
            head.addElement(Html.STYLE)
                    .addAttribute(Html.A_TYPE, "text/css")
                    .add(new FlyweightText(STOCKS_CSS));
        }


        Element body = html.addElement(Html.BODY);
        {
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

                List<Stock> stocks = dummyItems();
                for (int itemIndex = 0, max = stocks.size(); itemIndex < max; itemIndex++) {
                    Stock stock = stocks.get(itemIndex);
                    Element row = table.addElement(Html.TR).addAttribute(Html.A_CLASS, itemIndex % 2 == 0 ? "odd" : "even");
                    {
                        row.addElement(Html.TD).addText(String.valueOf(itemIndex + 1));
                        row.addElement(Html.TD).addElement(Html.A).addAttribute(Html.A_HREF, "/stocks/" + stock.getSymbol())
                                .addText(stock.getSymbol());
                        row.addElement(Html.TD).addElement(Html.A).addAttribute(Html.A_HREF, stock.getUrl())
                                .addText(stock.getName());
                        row.addElement(Html.TD).addElement("strong").addText(String.valueOf(stock.getPrice()));
                        row.addElement(Html.TD).addAttribute(Html.A_CLASS, stock.getChange() < 0 ? "minus" : null)
                                .addText(String.valueOf(stock.getChange()));
                        row.addElement(Html.TD).addAttribute(Html.A_CLASS, stock.getRatio() < 0 ? "minus" : null)
                                .addText(String.valueOf(stock.getRatio()));
                    }
                }
            }
        }

        renderHtml(document, output, true);
    }

    /** Rendering the HTML for Dom4j */
    private void renderHtml(Document document, HttpServletResponse output, boolean noCache) throws IOException {
        output.setCharacterEncoding(charset);
        if (noCache) {
            output.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            output.setHeader("Pragma", "no-cache");
            output.setHeader("Expires", "0");
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setNewlines(true);
        format.setEncoding(charset);
        format.setXHTML(true);
        output.getWriter().write("<!DOCTYPE html>");
        HTMLWriter writer = new HTMLWriter(output.getWriter(), format);
        writer.write(document);
        writer.flush();
    }

    public static List<Stock> dummyItems() {
        return StockService.INSTANCE.getStocks();
    }

    /** Get inline CSS */
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
