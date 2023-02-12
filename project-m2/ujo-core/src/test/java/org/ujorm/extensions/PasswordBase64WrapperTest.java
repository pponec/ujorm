/*
 * Copyright 2017-2022 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.extensions;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;

/**
 * Test of the class PasswordWrapper
 * @author Pavel Ponec
 */
public class PasswordBase64WrapperTest {

    /** Constructor tests. */
    @Test
    public void testConstructors() {
        System.out.println("Constructors");
        String password = "ABC";

        PasswordBase64Wrapper instance1 = PasswordBase64Wrapper.of(password);
        PasswordBase64Wrapper instance2 = new PasswordBase64Wrapper(instance1.getBase64());
        PasswordBase64Wrapper instance3 = new PasswordBase64Wrapper(password.toCharArray());
        PasswordBase64Wrapper instance4 = new PasswordBase64Wrapper();

        assertEquals(instance1, instance2);
        assertEquals(instance2, instance3);
        assertEquals(instance3, instance1);
        assertEquals("changeit", instance4.getPassword());
    }

    /** Test of toString method, of class PasswordWrapper. */
    @Test
    public void testToString() {
        System.out.println("toString");
        PasswordBase64Wrapper instance = PasswordBase64Wrapper.of("ABC");
        String expResult = "***";
        String result = instance.toString();
        assertEquals(expResult, result);
        //
        instance.internalClean();
        assertEquals("null", instance.toString());
    }

    /** Test of getPassword method, of class PasswordWrapper. */
    @Test
    public void testGetPassword() {
        System.out.println("getPassword");
        String expResult = "ABC";
        PasswordBase64Wrapper instance = PasswordBase64Wrapper.of(expResult);
        String result = instance.getPassword();
        assertEquals(expResult, result);
    }

    /** Test of getPasswordAsChars method. */
    @Test
    public void testGetPasswordAsChars() {
        System.out.println("getPasswordAsChars");
        char[] expResult = {'A','B','C'};
        PasswordBase64Wrapper instance = new PasswordBase64Wrapper(expResult);
        char[] result = instance.getPasswordAsChars();
        assertArrayEquals(expResult, result);
    }

    /** Test of exportToString method, of class PasswordWrapper. */
    @Test
    public void testExportToString() {
        System.out.println("exportToString");
        BinaryWrapper instance = BinaryWrapper.of("ABC");
        String expResult = "QUJD";
        String result = instance.exportToString();
        assertEquals(expResult, result);
        assertEquals(instance.getBase64(), result);
    }

    /** Test of of method, of class PasswordWrapper. */
    @Test
    public void testOf() {
        System.out.println("of");
        String password = "ABC";
        PasswordBase64Wrapper result = PasswordBase64Wrapper.of(password);

        assertEquals(PasswordBase64Wrapper.class, result.getClass());
        assertEquals(password, result.getPassword());
    }

    /** Test of finalize method, of class PasswordWrapper. */
    @Test
    public void testFinalize() {
        System.out.println("finalize");
        PasswordBase64Wrapper instance = PasswordBase64Wrapper.of("ABC");
        try {
            instance.finalize();
        } catch (Throwable e) {
            Logger.getLogger(PasswordBase64WrapperTest.class.getName()).log(Level.SEVERE, null, e);
        }
        try {
            instance.getPassword();
            assertFalse(true);
        } catch (IllegalStateException e) {
            // OK
        } catch (Throwable e) {
            throw e;
        }
    }

}
