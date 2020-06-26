
package org.ujorm.tools.web.ao;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ujorm.tools.web.HtmlElement;

/**
 *
 * @author Pavel Pone
 */
public class ProxyServletResponseTest {

    /**
     * Test of getContent method, of class ProxyServletResponse.
     */
    @Test
    public void testGetContent_1() throws IOException {
        System.out.println("getContent");
        ProxyServletResponse instance = new ProxyServletResponse();
        instance.getWriter().write("ABC:ČÁŠ");
        assertEquals("ABC:ČÁŠ", instance.toString());
    }

    /**
     * Test of getContent method, of class ProxyServletResponse.
     */
    @Test
    public void testGetContent_2() {
        System.out.println("getContent");
        ProxyServletResponse instance = new ProxyServletResponse();

        try (HtmlElement html = HtmlElement.niceOf(instance)) {
            html.getBody().addHeading("ABC:ČÁŠ", "cssType");
        }
        String result = instance.toString();
        String expected = String.join("\n"
                , "<!DOCTYPE html>"
                , "<html lang=\"en\">" 
                , "    <head>" 
                , "        <meta charset=\"UTF-8\"/>"
                , "        <title>Demo</title>"
                , "    </head>" 
                , "    <body>"
                , "        <h1 class=\"cssType\">ABC:ČÁŠ</h1>"
                , "    </body>"
                , "</html>");
        assertEquals(expected, result);

    }

}
