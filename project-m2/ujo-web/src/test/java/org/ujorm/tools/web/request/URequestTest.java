package org.ujorm.tools.web.request;


import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class URequestTest {

    /**
     * aused by: java.lang.reflect.InaccessibleObjectException:
     * Unable to make protected final java.lang.Class
     * java.lang.ClassLoader.defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain) throws java.lang.ClassFormatError accessible:
     * module java.base does not "opens java.lang" to unnamed module @1554909b
     * */
    @Test
    public void compileTest() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("p1", "v1");
        request.setParameter("p2", "v2a", "v2b");
        URequest uRequest = UContext.ofServlet(request, new MockHttpServletResponse()).request();

        assertEquals(0, uRequest.getParameters("p0").length);
        assertEquals(1, uRequest.getParameters("p1").length);
        assertEquals(2, uRequest.getParameters("p2").length);

        assertEquals("v1", uRequest.getParameters("p1")[0]);
        assertEquals("v2b", uRequest.getParameters("p2")[1]);
    }

}