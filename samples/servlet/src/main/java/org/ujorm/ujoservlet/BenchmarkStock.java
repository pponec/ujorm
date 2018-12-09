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
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.dom.HtmlElement;
import org.ujorm.tools.dom.XmlElement;
import org.ujorm.tools.dom.XmlWriter;
import org.ujorm.ujoservlet.tools.Html;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A live example of the HtmlElement inside a servlet.
 * @see https://dzone.com/articles/modern-type-safe-template-engines-part-2
 * @author Pavel Ponec
 */
@WebServlet(BenchmarkStock.URL_PATTERN)
public class BenchmarkStock extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/benchmarkStock";

    /** Show the first line of soufce code */
    public static final short SHOW_LINE = 53;

    /** Inline CSS */
    private final String STOCKS_CSS = getCss();

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        stock(output);
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
     * @return
     */
    public void stock(HttpServletResponse output) throws IOException, IllegalArgumentException {
        final int[] index = {0};

        HtmlElement html = new HtmlElement(UTF_8);
        try (XmlElement head = html.getHead()) {
            head.addElement(Html.TITLE)
                    .addText("Stock Prices");
            head.addElement(Html.META
                    , "http-equiv", "Content-Type"
                    , Html.A_CONTENT, "text/html; charset=UTF-8");
            head.addElement(Html.META
                    , "http-equiv", "Content-Style-Type"
                    , Html.A_CONTENT, "text/css");
            head.addElement(Html.META
                    , "Content-Script-Type", "text/javascript"
                    , Html.A_CONTENT, "text/javascript");
            head.addElement(Html.LINK
                    , Html.A_REL,  "shortcut icon"
                    , Html.A_CONTENT, "/images/favicon.ico");
            head.addElement(Html.LINK
                    , Html.A_REL, "stylesheet"
                    , Html.A_HREF, "benchmark.css"
                    , Html.A_MEDIA, "all");
            head.addElement(Html.SCRIPT
                    , Html.A_TYPE, "text/javascript"
                    , Html.A_SRC, "/js/util.js")
                    .addText("");
            head.addElement(Html.STYLE
                    , Html.A_TYPE, "text/css")
                    .addRawText(STOCKS_CSS);
        }

        try (XmlElement body = html.getBody()) {
            body.addElement(Html.H1).addText("Stock Prices");
            try (XmlElement table = body.addElement(Html.TABLE)) {
                try (XmlElement thead = table.addElement(Html.THEAD)) {
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
                    try (XmlElement row = table.addElement(Html.TR, Html.A_CLASS, index[0]++ % 2 == 0 ? "odd" : "even")) {
                        row.addElement(Html.TD).addText(index[0]);
                        row.addElement(Html.TD).addElement(Html.A, Html.A_HREF, "/stocks/" + stock.getSymbol())
                                .addText(stock.getSymbol());
                        row.addElement(Html.TD).addElement(Html.A, Html.A_HREF, stock.getUrl())
                                .addText(stock.getName());
                        row.addElement(Html.TD).addElement("strong").addText(stock.getPrice());
                        row.addElement(Html.TD, Html.A_CLASS, stock.getChange() < 0 ? "minus" : null)
                                .addText(stock.getChange());
                        row.addElement(Html.TD, Html.A_CLASS, stock.getRatio() < 0 ? "minus" : null)
                                .addText(stock.getRatio());
                    }
                }
            }
        }

        output.setCharacterEncoding(UTF_8.toString());
        html.toWriter(new XmlWriter(output.getWriter().append(HtmlElement.HEADER).append("\n"), "\t"));
    }

    /** A Stock class */
    private static class Stock {

        private String name;

        private String name2;

        private String url;

        private String symbol;

        private double price;

        private double change;

        private double ratio;

        public Stock(String name, String name2, String url, String symbol, double price, double change, double ratio) {
            this.name = name;
            this.name2 = name2;
            this.url = url;
            this.symbol = symbol;
            this.price = price;
            this.change = change;
            this.ratio = ratio;
        }

        public String getName() {
            return this.name;
        }

        public String getName2() {
            return this.name2;
        }

        public String getUrl() {
            return this.url;
        }

        public String getSymbol() {
            return this.symbol;
        }

        public double getPrice() {
            return this.price;
        }

        public double getChange() {
            return this.change;
        }

        public double getRatio() {
            return this.ratio;
        }
    }

    public static List<Stock> dummyItems() {
        List<Stock> items = new ArrayList<Stock>();
        items.add(new Stock("Adobe Systems", "Adobe Systems Inc.", "http://www.adobe.com", "ADBE", 39.26, 0.13, 0.33));
        items.add(new Stock("Advanced Micro Devices", "Advanced Micro Devices Inc.", "http://www.amd.com", "AMD",
                16.22, 0.17, 1.06));
        items.add(new Stock("Amazon.com", "Amazon.com Inc", "http://www.amazon.com", "AMZN", 36.85, -0.23, -0.62));
        items.add(new Stock("Apple", "Apple Inc.", "http://www.apple.com", "AAPL", 85.38, -0.87, -1.01));
        items.add(new Stock("BEA Systems", "BEA Systems Inc.", "http://www.bea.com", "BEAS", 12.46, 0.09, 0.73));
        items.add(new Stock("CA", "CA, Inc.", "http://www.ca.com", "CA", 24.66, 0.38, 1.57));
        items.add(new Stock("Cisco Systems", "Cisco Systems Inc.", "http://www.cisco.com", "CSCO", 26.35, 0.13, 0.5));
        items.add(new Stock("Dell", "Dell Corp.", "http://www.dell.com/", "DELL", 23.73, -0.42, -1.74));
        items.add(new Stock("eBay", "eBay Inc.", "http://www.ebay.com", "EBAY", 31.65, -0.8, -2.47));
        items.add(new Stock("Google", "Google Inc.", "http://www.google.com", "GOOG", 495.84, 7.75, 1.59));
        items.add(new Stock("Hewlett-Packard", "Hewlett-Packard Co.", "http://www.hp.com", "HPQ", 41.69, -0.02, -0.05));
        items.add(new Stock("IBM", "International Business Machines Corp.", "http://www.ibm.com", "IBM", 97.45, -0.06,
                -0.06));
        items.add(new Stock("Intel", "Intel Corp.", "http://www.intel.com", "INTC", 20.53, -0.07, -0.34));
        items.add(new Stock("Juniper Networks", "Juniper Networks, Inc", "http://www.juniper.net/", "JNPR", 18.96, 0.5,
                2.71));
        items.add(new Stock("Microsoft", "Microsoft Corp", "http://www.microsoft.com", "MSFT", 30.6, 0.15, 0.49));
        items.add(new Stock("Oracle", "Oracle Corp.", "http://www.oracle.com", "ORCL", 17.15, 0.17, 1.1));
        items.add(new Stock("SAP", "SAP AG", "http://www.sap.com", "SAP", 46.2, -0.16, -0.35));
        items.add(new Stock("Seagate Technology", "Seagate Technology", "http://www.seagate.com/", "STX", 27.35, -0.36,
                -1.3));
        items.add(new Stock("Sun Microsystems", "Sun Microsystems Inc.", "http://www.sun.com", "SUNW", 6.33, -0.01,
                -0.16));
        items.add(new Stock("Yahoo", "Yahoo! Inc.", "http://www.yahoo.com", "YHOO", 28.04, -0.17, -0.6));

        return items;
    }

    /** Get inline CSS */
    private static String getCss() {
        return String.join("\n"
                , "/*<![CDATA[*/"
                , "body {"
                , "\tcolor: #333333;"
                , "\tline-height: 150%;"
                , "}"
                , ""
                , "thead {"
                , "\tfont-weight: bold;"
                , "\tbackground-color: #CCCCCC;"
                , "}"
                , ""
                , ".odd {"
                , "\tbackground-color: #FFCCCC;"
                , "}"
                , ""
                , ".even {"
                , "\tbackground-color: #CCCCFF;"
                , "}"
                , ""
                , ".minus {"
                , "\tcolor: #FF0000;"
                , "}"
                , ""
                , "/*]]>*/");
    }

    @Override
    protected void doPost(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        doGet(input, output);
    }
}
