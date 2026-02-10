/*
 * Copyright 2019-2026 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/XmlModel.java
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

package org.ujorm.tools.web;

import org.junit.jupiter.api.Test;
import org.ujorm.tools.common.StringUtils;
import org.ujorm.tools.web.request.HttpContext;
import org.ujorm.tools.xml.config.HtmlConfig;
import htmlflow.HtmlFlow;
import static j2html.TagCreator.*;

/**
 * @author Pavel Ponec
 */
public class BenchmarkElementTest {

    private static final boolean RUN_BENCHMARK = false;

    @Test
    public void runBenchmark() {
        var rows = RUN_BENCHMARK ? 100_000 : 1;
        var cols = 50;
        System.out.println("Starting benchmark for " + rows + " rows...");

        // 1. WARM-UP PHASE
        warmUp();

        // 2. Ujorm Element Benchmark
        System.gc();
        var startUjorm = System.currentTimeMillis();
        var ujormHtml = runUjorm(rows, cols);
        var endUjorm = System.currentTimeMillis();
        System.out.println("Ujorm Element: " + format(endUjorm - startUjorm) + " ms (Length: " + format(ujormHtml.toString().length()) + ")");

        // 3. j2html Benchmark
        System.gc();
        var startJ2 = System.currentTimeMillis();
        var j2Html = runJ2Html(rows, cols);
        var endJ2 = System.currentTimeMillis();
        System.out.println("j2html: " + format(endJ2 - startJ2) + " ms (Length: " + format(j2Html.length()) + ")");

        // 4. HtmlFow Benchmark
        System.gc();
        var startHF = System.currentTimeMillis();
        var htmlHF = runHtmlFlow(rows, cols);
        var endHF = System.currentTimeMillis();
        System.out.println("htmlFlow: " + format(endHF - startHF) + " ms (Length: " + format(htmlHF.length()) + ")");
    }

    /** Run Element of the Ujorm framework */
    private static HttpContext runUjorm(int rowCount, int colCount) {
        var response = HttpContext.of();
        var config = HtmlConfig.ofDefault();
        config.setCompressedFormat();
        try (var html = AbstractHtmlElement.of(response, config)) {
            try (var body = html.getBody()) {
                try (var table = body.addTable()) {
                    for (int i = 0; i < rowCount; i++) {
                        var tr = table.addElement("tr");
                        for (int j = 0; j < colCount; j++) {
                            tr.addElement("td").addText("Data " + i + ":" + j);
                        }
                    }
                }
            }
        }
        return response;
    }

    /** Run J2Html framework */
    private static String runJ2Html(int rowCount, int colCount) {
        var table = table();
        var body = body(h1("Simple user form"), table);
        var head = head(
                title("Demo"),
                meta().withCharset("UTF-8")
        );
        for (int i = 0; i < rowCount; i++) {
            var tr = tr();
            for (int j = 0; j < colCount; j++) {
                tr.with(td("Data " + i + ":" + j));
            }
            table.with(tr);
        }
        return document(html(head, body));
    }

    /** Run HtmlFlow framework */
    private static String runHtmlFlow(int rowCount, int colCount) {
        var niceCode = true; // Nice code has a better performance.
        return niceCode
                ? runHtmlFlowNiceCode(rowCount, colCount)
                : runHtmlFlowShortCode(rowCount, colCount);
    }

    /** Run HtmlFlow framework with Short Code */
    private static String runHtmlFlowShortCode(int rowCount, int colCount) {
        var view = HtmlFlow.view(v -> {
            var table = v.html().body().table();
            for (int i = 0; i < rowCount; i++) {
                var tr = table.tr();
                for (int j = 0; j < colCount; j++) {
                    tr.td().text("Data " + i + ":" + j).__();
                }
                tr.__(); // tr
            }
        }).setIndented(false);
        return view.render();
    }

    /** Run HtmlFlow framework with a indented code */
    private static String runHtmlFlowNiceCode(int rowCount, int colCount) {
        var view = HtmlFlow.view(v -> {
            var table = v.html().body().table();
            for (int i = 0; i < rowCount; i++) {
                var tr = table.tr();
                for (int j = 0; j < colCount; j++) {
                    tr.td().text("Data " + i + ":" + j).__();
                }
                tr.__();
            }
        });
        return view.render();
    }

    /** Worm Up Phase */
    private static void warmUp() {
        int rows = RUN_BENCHMARK ? 100_000 : 1;
        int cols = 50;
        for (int i = 0; i < 5; i++) {
            runUjorm(rows, cols);
            runJ2Html(rows, cols);
            runHtmlFlow(rows, cols);
        }
    }

    /** Format number using THIN_NBSP for the thousand separator. */
    private static  String format(long number) {
        return StringUtils.formatSeparator(number);
    }

}
