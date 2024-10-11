package org.ujorm.tools.web.ao;

import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class URequestTest {

    /**
     * aused by: java.lang.reflect.InaccessibleObjectException:
     * Unable to make protected final java.lang.Class
     * java.lang.ClassLoader.defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain) throws java.lang.ClassFormatError accessible:
     * module java.base does not "opens java.lang" to unnamed module @1554909b
     * */
    //@Test
    public void compileTest() throws IOException {

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        BufferedReader reader = request.getReader();

        URequest.ManyMap map = URequest.createMap();
        map.put("p1", "v1");
        map.put("p2", "v2a");
        map.put("p2", "v2b");
        URequest uRequest = map.toRequest(reader);

        assertEquals(1, map.get("p1").length);
        assertEquals(2, map.get("p2").length);

        assertEquals("v1", map.get("p1")[0]);
        assertEquals("v2", map.get("p2b")[1]);


    }

}