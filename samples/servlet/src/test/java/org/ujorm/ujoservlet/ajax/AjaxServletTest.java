package org.ujorm.ujoservlet.ajax;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pavel
 */
public class AjaxServletTest {

    /**
     * Test of getResult method, of class AjaxServlet.
     */
    @Test
    public void testGetResult() {
        System.out.println("getResult");
        AjaxServlet instance = new AjaxServlet();

        assertEquals("A<span>B</span>C", instance.highlight("B", "ABC"));
        assertEquals("&lt;<span>B</span>&gt;", instance.highlight("B", "<B>"));
        assertEquals("&lt;<span>&amp;</span>&gt;", instance.highlight("&", "<&>"));
    }
}
