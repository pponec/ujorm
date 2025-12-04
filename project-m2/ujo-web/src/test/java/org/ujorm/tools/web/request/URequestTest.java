package org.ujorm.tools.web.request;


import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class URequestTest {

    @Test
    public void getParameters() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("p1", "v1");
        request.setParameter("p2", "v2a", "v2b");
        URequest uRequest = HttpContext.ofServlet(request, new MockHttpServletResponse()).request();

        assertEquals(0, uRequest.getParameters("p0").length);
        assertEquals(1, uRequest.getParameters("p1").length);
        assertEquals(2, uRequest.getParameters("p2").length);

        assertEquals("v1", uRequest.getParameters("p1")[0]);
        assertEquals("v2b", uRequest.getParameters("p2")[1]);
    }


    @Test
    public void getStringParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("p1", "1");
        request.setParameter("p2", "3", "2");
        request.setParameter("p3", "X");
        URequest uRequest = HttpContext.ofServlet(request, new MockHttpServletResponse()).request();

        assertEquals("0", uRequest.getParameter("p0", "0"));
        assertEquals("1", uRequest.getParameter("p1", "0"));
        assertEquals("2", uRequest.getParameter("p2", "0"));
        assertEquals("X", uRequest.getParameter("p3", "3"));
    }

    @Test
    public void getGenericParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("p1", "1");
        request.setParameter("p2", "3", "2");
        request.setParameter("p3", "X");
        URequest uRequest = HttpContext.ofServlet(request, new MockHttpServletResponse()).request();

        assertEquals(0, uRequest.getParameter("p0", 0, Integer::parseInt));
        assertEquals(1, uRequest.getParameter("p1", 0, Integer::parseInt));
        assertEquals(2, uRequest.getParameter("p2", 0, Integer::parseInt));
        assertEquals(3, uRequest.getParameter("p3", 3, Integer::parseInt));
    }

}