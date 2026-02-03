package org.ujorm.tools.converter;

import org.junit.jupiter.api.Test;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.request.HttpContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneratedHtmlTest {

    /** Generated for use with: org.ujorm:ujo-web:2.30 (2026-02-08) */
    public String htmlGenerator() {
        var result = HttpContext.of();
        try (var html = HtmlElement.niceOf("Test", result)) {
            html.setAttribute(Html.A_LANG, "cs");
            try (var body = html.addBody()) {
                body.addHeadingX(2, "c1", "c2")
                    .addText("Title");
                try (var div = body.addDiv("container", "main")) {
                    div.setId("outer");
                    try (var div2 = div.addDiv()) {
                        div2.setId("inner");
                        div2.addElement("my-custom-tag", "custom")
                            .setAttribute("my-custom-attribute", "c");
                        div2.addScript()
                            .addRawText("\n" +
                                """
                                               console.log('test1' + ' very very very long text;');
                                               console.log('test2' + ' very very very long text;');
                                            \
                                """);
                    }
                    try (var form = div.addForm()) {
                        form.addLabel()
                            .setFor("in")
                            .addText("First name");
                        form.addInput()
                            .setType("text")
                            .setName("firstname")
                            .setId("in");
                    }
                    try (var form2 = div.addElement("form2")) {
                        try (var label = form2.addLabel()) {
                            label.setFor("in");
                            label.addText("First name:");
                            label.addInput()
                                .setType("text")
                                .setName("firstname")
                                .setId("in");
                        }
                    }
                    div.addAnchor("http://ujorm.org", "niceClass", "linkClass")
                        .addText("The Ujorm");
                    div.addAnchor("http://test.org")
                        .addText("The Test");
                    try (var p = div.addParagraph()) {
                        p.addText("Welcome user.");
                        p.addBreak();
                        p.addText("Please visit ");
                        p.addAnchor("#")
                            .addText("our website");
                        p.addText(".");
                    }
                    div.addImg()
                        .setAttribute(Html.A_SRC, "image.png")
                        .setAttribute(Html.A_ALT, "Sample");
                    try (var ul = div.addUnorderedlist()) {
                        ul.addListItem()
                            .addText("Unordered item");
                    }
                    try (var ol = div.addOrderedList()) {
                        ol.addListItem()
                            .addText("Ordered item 1");
                        ol.addListItem()
                            .addText("Ordered item 2");
                    }
                    div.addPreformatted()
                        .addText("print(\"Hello World\");");
                    try (var table = div.addTable()) {
                        try (var thead = table.addTableHead()) {
                            try (var tr = thead.addTableRow()) {
                                tr.addTableDetail()
                                    .addText("Header Cell");
                            }
                        }
                        try (var tbody = table.addTableBody()) {
                            try (var tr = tbody.addTableRow()) {
                                tr.addTableDetail()
                                    .addText("Data Cell");
                            }
                        }
                    }
                }
            }
        }
        return result.toString();
    }

    @Test
    void testGenerateJavaCode() throws Exception {
        // 1. + 2. Načtení zdrojového HTML
        String test = htmlGenerator();
        System.out.println("""
                //---- Generated ----
                %s
                //----
                """.formatted(test));
        assertTrue(test.contains("<html lang=\"cs\">"));
        assertTrue(test.contains("<title>Test</title>"));
        assertTrue(test.contains("<div class=\"container main\" id=\"outer\">"));
        assertTrue(test.contains("<div id=\"inner\">"));
        assertTrue(test.contains("console.log('test1' + ' very very very long text;')"));
        assertTrue(test.contains("<label for=\"in\">First name</label>"));
        assertTrue(test.contains("<a class=\"niceClass linkClass\" href=\"http://ujorm.org\">The Ujorm</a>"));
        assertTrue(test.contains("<a href=\"http://test.org\">The Test</a>"));
        //
        assertFalse(test.contains("<html lang=\"en\">"));
        assertFalse(test.contains("<title>Demo</title>"));
    }

}