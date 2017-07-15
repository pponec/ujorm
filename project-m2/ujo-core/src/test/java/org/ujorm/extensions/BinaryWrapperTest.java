package org.ujorm.extensions;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Pavel Ponec
 */
public class BinaryWrapperTest {


    /** Constructor tests */
    @Test
    public void testConstructors() {
        System.out.println("Constructors");
        final String code = "ABC";

        BinaryWrapper instance1 = BinaryWrapper.of(code);
        BinaryWrapper instance2 = new BinaryWrapper(instance1.getBase64());
        BinaryWrapper instance3 = new BinaryWrapper(code.toCharArray());
        BinaryWrapper instance4 = new BinaryWrapper(instance3.getBinary());

        assertEquals(instance1, instance2);
        assertEquals(instance2, instance3);
        assertEquals(instance3, instance4);
        assertEquals(instance4, instance1);
    }

     /** Test of toString method, of class BinaryWrapper. */
    @Test
    public void testToString() {
        System.out.println("toString");
        String code = "ABC";
        String expResult = "QUJD"; // Base64 format
        BinaryWrapper instance = BinaryWrapper.of(code);
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /** Test of getBase64 method, of class BinaryWrapper. */
    @Test
    public void testGetBase64() {
        System.out.println("getBase64");
        BinaryWrapper instance = BinaryWrapper.of("ABC");
        String expResult = "QUJD";
        String result = instance.getBase64();
        assertEquals(expResult, result);
    }

    /** Test of exportToString method, of class BinaryWrapper. */
    @Test
    public void testExportToString() {
        System.out.println("exportToString");
        BinaryWrapper instance = BinaryWrapper.of("ABC");
        String expResult = "QUJD";
        String result = instance.exportToString();
        assertEquals(expResult, result);
        assertEquals(instance.getBase64(), result);
    }

    /** Test of getBinary method, of class BinaryWrapper. */
    @Test
    public void testGetBinary() {
        System.out.println("getBinary");
        BinaryWrapper instance = BinaryWrapper.of("ABC");
        byte[] expResult = {'A','B','C'};
        byte[] result = instance.getBinary();
        assertArrayEquals(expResult, result);
    }

    /** Test of internalClean method, of class BinaryWrapper */
    @Test
    public void testEquals() {
        System.out.println("equals");
        BinaryWrapper instance1 = BinaryWrapper.of("ABC");
        BinaryWrapper instance2 = BinaryWrapper.of("ABC");
        BinaryWrapper instance3 = BinaryWrapper.of("XXX");
        assertEquals(instance1, instance2);
        assertNotEquals(instance1, instance3);
    }

    /** Test of of method, of class PasswordWrapper. */
    @Test
    public void testOf() {
        System.out.println("of");
        String base64 = "QUJD";
        BinaryWrapper result = BinaryWrapper.of("ABC");

        assertEquals(BinaryWrapper.class, result.getClass());
        assertEquals(base64, result.getBase64());
    }

    /** Test of internalClean method, of class BinaryWrapper. */
    @Test
    public void testInternalClean() {
        System.out.println("finalize");
        BinaryWrapper instance = BinaryWrapper.of("ABC");
        try {
            instance.internalClean();
        } catch (Throwable e) {
            Logger.getLogger(PasswordWrapperTest.class.getName()).log(Level.SEVERE, null, e);
        }
        try {
            instance.getBinary();
            assertFalse(true);
        } catch (IllegalStateException e) {
            // OK
        } catch (Throwable e) {
            throw e;
        }
    }

}
