
package org.ujorm.tools.web.ao;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.ujorm.tools.web.HtmlElement;

/**
 *
 * @author Pavel Pone
 */
public class MockServletResponseTest {

    /**
     * Test of getContent method, of class MockServletResponse.
     */
    @Test
    public void testGetContent_1() throws IOException {
        System.out.println("getContent");
        MockServletResponse instance = new MockServletResponse();
        instance.getWriter().write("ABC:ČÁŠ");
        assertEquals("ABC:ČÁŠ", instance.toString());
    }

    /**
     * Test of getContent method, of class MockServletResponse.
     */
    @Test
    public void testGetContent_2() {
        System.out.println("getContent");
        MockServletResponse instance = new MockServletResponse();

        try (HtmlElement html = HtmlElement.niceOf(instance)) {
            html.getBody().addHeading("ABC:ČÁŠ", "cssType");
        }
        String result = instance.toString();
        String expected = String.join("\n"
                , "<!DOCTYPE html>"
                , "<html lang=\"en\">"
                , "\t<head>"
                , "\t\t<meta charset=\"UTF-8\"/>"
                , "\t\t<title>Demo</title>"
                , "\t</head>"
                , "\t<body>"
                , "\t\t<h1 class=\"cssType\">ABC:ČÁŠ</h1>"
                , "\t</body>"
                , "</html>");
        assertEquals(expected, result);

    }

}
