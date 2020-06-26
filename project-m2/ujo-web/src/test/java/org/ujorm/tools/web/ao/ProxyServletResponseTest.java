
package org.ujorm.tools.web.ao;

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
    public void testGetContent() {
        System.out.println("getContent");
        ProxyServletResponse instance = new ProxyServletResponse();

        try (HtmlElement html = HtmlElement.niceOf(instance)) {
            html.getBody().addHeading("HELLO", "cssType");
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
                , "        <h1 class=\"cssType\">HELLO</h1>"
                , "    </body>"
                , "</html>");
        assertEquals(expected, result);

    }

}
