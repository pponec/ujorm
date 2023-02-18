package org.ujorm.ujoservlet.ajax;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.ujoservlet.ajax.ao.Message;
import org.ujorm.ujoservlet.ajax.ao.Service;

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

        Assertions.assertEquals("A<span>B</span>C", instance.highlight("B", "ABC").getText());
        Assertions.assertEquals("&lt;<span>B</span>&gt;", instance.highlight("B", "<B>").getText());
        Assertions.assertEquals("&lt;<span>&amp;</span>&gt;", instance.highlight("&", "<&>").getText());
        Assertions.assertEquals("IllegalStateException: test", Message.of(new IllegalStateException("test")).getText());
    }
}
