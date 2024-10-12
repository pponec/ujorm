package org.ujorm.tools.web.context;

import org.mockito.Mockito;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.*;

class UResponseTest {

    /**
     * Caused by: java.lang.reflect.InaccessibleObjectException: Unable to make protected final java.lang.Class java.lang.ClassLoader.defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain) throws java.lang.ClassFormatError accessible: module java.base does not "opens java.lang" to unnamed module @61dc03ce
     * @throws IOException
     */
    //@Test
    public void compilationTest() throws IOException {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Writer writer = response.getWriter();
        Assertions.assertTrue(writer != null);
    }

}