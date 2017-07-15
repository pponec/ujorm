/*
 * Copyright 2017-2017 Pavel Ponec
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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test of the class PasswordWrapper
 * @author Pavel Ponec
 */
public class PasswordWrapperTest {

    /** Constructor tests. */
    @Test
    public void testConstructors() {
        System.out.println("Constructors");
        String password = "ABC";

        PasswordWrapper instance1 = PasswordWrapper.of(password);
        PasswordWrapper instance2 = new PasswordWrapper(instance1.getBase64());
        PasswordWrapper instance3 = new PasswordWrapper(password.toCharArray());
        PasswordWrapper instance4 = new PasswordWrapper();

        assertEquals(instance1, instance2);
        assertEquals(instance2, instance3);
        assertEquals(instance3, instance1);
        assertEquals("changeit", instance4.getPassword());
    }

    /** Test of toString method, of class PasswordWrapper. */
    @Test
    public void testToString() {
        System.out.println("toString");
        PasswordWrapper instance = PasswordWrapper.of("ABC");
        String expResult = "*";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /** Test of getPassword method, of class PasswordWrapper. */
    @Test
    public void testGetPassword() {
        System.out.println("getPassword");
        String expResult = "ABC";
        PasswordWrapper instance = PasswordWrapper.of(expResult);
        String result = instance.getPassword();
        assertEquals(expResult, result);
    }

    /** Test of getPasswordAsChars method. */
    @Test
    public void testGetPasswordAsChars() {
        System.out.println("getPasswordAsChars");
        char[] expResult = {'A','B','C'};
        PasswordWrapper instance = new PasswordWrapper(expResult);
        char[] result = instance.getPasswordAsChars();
        assertArrayEquals(expResult, result);
    }

    /** Test of of method, of class PasswordWrapper. */
    @Test
    public void testOf() {
        System.out.println("of");
        String password = "ABC";
        PasswordWrapper result = PasswordWrapper.of(password);

        assertEquals(PasswordWrapper.class, result.getClass());
        assertEquals(password, result.getPassword());
    }

    /** Test of finalize method, of class PasswordWrapper. */
    @Test
    public void testFinalize() {
        System.out.println("finalize");
        PasswordWrapper instance = PasswordWrapper.of("ABC");
        try {
            instance.finalize();
        } catch (Throwable e) {
            Logger.getLogger(PasswordWrapperTest.class.getName()).log(Level.SEVERE, null, e);
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
