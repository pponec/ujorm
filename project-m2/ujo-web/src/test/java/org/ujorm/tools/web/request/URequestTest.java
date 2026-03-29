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

        assertEquals(0, uRequest.parameters("p0").length);
        assertEquals(1, uRequest.parameters("p1").length);
        assertEquals(2, uRequest.parameters("p2").length);

        assertEquals("v1", uRequest.parameters("p1")[0]);
        assertEquals("v2b", uRequest.parameters("p2")[1]);
    }


    @Test
    public void getStringParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("p1", "1");
        request.setParameter("p2", "3", "2");
        request.setParameter("p3", "X");
        URequest uRequest = HttpContext.ofServlet(request, new MockHttpServletResponse()).request();

        assertEquals("0", uRequest.parameter("p0", "0"));
        assertEquals("1", uRequest.parameter("p1", "0"));
        assertEquals("2", uRequest.parameter("p2", "0"));
        assertEquals("X", uRequest.parameter("p3", "3"));
    }

    @Test
    public void getGenericParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("p1", "1");
        request.setParameter("p2", "3", "2");
        request.setParameter("p3", "X");
        URequest uRequest = HttpContext.ofServlet(request, new MockHttpServletResponse()).request();

        assertEquals(0, uRequest.parameter("p0", Integer::parseInt, 0));
        assertEquals(1, uRequest.parameter("p1", Integer::parseInt, 0));
        assertEquals(2, uRequest.parameter("p2", Integer::parseInt, 0));
        assertEquals(3, uRequest.parameter("p3", Integer::parseInt, 3));
    }

}