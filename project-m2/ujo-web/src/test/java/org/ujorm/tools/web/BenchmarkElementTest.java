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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujorm.tools.common.StringUtils;
import org.ujorm.tools.web.request.HttpContext;
import org.ujorm.tools.xml.config.HtmlConfig;
import htmlflow.HtmlFlow;
import java.util.function.Supplier;

import static j2html.TagCreator.*;

/**
 * @author Pavel Ponec
 */
public class BenchmarkElementTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkElementTest.class);

    private static final boolean RUN_BENCHMARK = false;

    @Test
    public void runBenchmark() {
        final var config = RUN_BENCHMARK
                ? Params.ofManyRows()
                : Params.ofUnitTest();
        final var configTest = Params.ofUnitTest();
        LOGGER.info("Starting benchmark for " + config);

        // 1. Warm-up phase
        run("Warming", Params.ofWarming(), () -> warmUp(configTest));

        // 2. Ujorm Element Benchmark
        run("Ujorm", config, ()->runUjorm(config));

        // 3. j2html Benchmark
        run("J2Html", config, ()->runJ2Html(config));

        // 4. HtmlFow Benchmark
        var niceCode = true; // Nice code has a better performance.
        if (niceCode) {
            run("HtmlFlow", config, ()->runHtmlFlowNiceCode(config));
        } else {
            run("HtmlFlow", config, ()->runHtmlFlowShortCode(config));
        }
    }

    /** Run Element of the Ujorm framework */
    private static String runUjorm(final Params params) {
        final var htmlConf = (HtmlConfig) HtmlConfig.ofDefault().setCompressedFormat();
        var response = HttpContext.of();
        try (var html = AbstractHtmlElement.of(response, htmlConf)) {
            try (var body = html.getBody()) {
                try (var table = body.addTable()) {
                    for (int i = 0; i < params.rows; i++) {
                        var tr = table.addElement("tr");
                        for (int j = 0; j < params.cols; j++) {
                            tr.addElement("td").addText("Data " + i + ":" + j);
                        }
                    }
                }
            }
        }
        return response.toString();
    }

    /** Run J2Html framework */
    private static String runJ2Html(final Params params) {
        var table = table();
        var body = body(h1("Simple user form"), table);
        var head = head(
                title("Demo"),
                meta().withCharset("UTF-8")
        );
        for (int i = 0; i < params.rows; i++) {
            var tr = tr();
            for (int j = 0; j < params.cols; j++) {
                tr.with(td("Data " + i + ":" + j));
            }
            table.with(tr);
        }
        return document(html(head, body));
    }

    /** Run HtmlFlow framework with Short Code */
    private static String runHtmlFlowShortCode(final Params params) {
        var view = HtmlFlow.view(v -> {
            var table = v.html().body().table();
            for (int i = 0; i < params.rows; i++) {
                var tr = table.tr();
                for (int j = 0; j < params.cols; j++) {
                    tr.td().text("Data " + i + ":" + j).__();
                }
                tr.__(); // tr
            }
        }).setIndented(false);
        return view.render();
    }

    /** Run HtmlFlow framework with a indented code */
    private static String runHtmlFlowNiceCode(final Params params) {
        var view = HtmlFlow.view(v -> {
            var table = v.html().body().table();
            for (int i = 0; i < params.rows; i++) {
                var tr = table.tr();
                for (int j = 0; j < params.cols; j++) {
                    tr.td().text("Data " + i + ":" + j).__();
                }
                tr.__();
            }
        });
        return view.render();
    }

    /** Worm Up Phase */
    private String warmUp(Params params) {
        runUjorm(params);
        runJ2Html(params);
        runHtmlFlowNiceCode(params);
        return "";
    }

    /** Run the benchmark */
    private static void run(final String title, final Params params, final Supplier<String> supplier) {
        System.gc();
        var result = "";
        var startUjorm = System.currentTimeMillis();
        for (int c = 0; c < params.loops; c++) {
            result = supplier.get();
        }
        var endUjorm = System.currentTimeMillis();
        LOGGER.info("%-8s: %s ms (Length: %s)".formatted(
                title,  format(endUjorm - startUjorm), format(result.length())));
    }

    /** Format number using THIN_NBSP for the thousand separator. */
    private static  String format(long number) {
        return StringUtils.formatSeparator(number);
    }

    record Params(int cols, int rows, int loops) {
        static Params ofManyLoops() {
            return new Params(15, 50, 100_000);
        }
        static Params ofManyRows() {
            return new Params(15, 250_000, 20);
        }
        static Params ofUnitTest() {
            return new Params(15, 1, 1);
        }
        static Params ofWarming() {
            return RUN_BENCHMARK
                    ? ofManyLoops()
                    : new Params(15, 3 , 1);
        }
    }

}
