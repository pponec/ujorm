package org.ujorm.ujoservlet.ajax;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pavel
 */
public class ServiceTest {

    /**
     * Test of getResult method, of class AjaxServlet.
     */
    @Test
    public void testHighlight() {
        System.out.println("getResult");
        Service instance = new Service();

        assertEquals("A<span>B</span>C", instance.highlight("B", "ABC").getText());
        assertEquals("&lt;<span>B</span>&gt;", instance.highlight("B", "<B>").getText());
        assertEquals("&lt;<span>&amp;</span>&gt;", instance.highlight("&", "<&>").getText());
        assertEquals("IllegalStateException: test", Message.of(new IllegalStateException("test")).getText());
    }
}
