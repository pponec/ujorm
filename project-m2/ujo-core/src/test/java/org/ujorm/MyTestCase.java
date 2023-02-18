/*
 * MyTestCase.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import junit.framework.*;
import org.ujorm.extensions.UjoTextable;

/**
 *
 * @author Pavel Ponec
 */
abstract public class MyTestCase extends org.junit.jupiter.api.Assertions{

    public MyTestCase() {
    }

    @Deprecated
    public MyTestCase(String name) {
    }

    @Deprecated
    protected String suite() {
        return getClass().getSimpleName();
    }

    /** A TimeTest loop size. */
    public int getTimeLoopCount() {
        //return 5*1000*1000;
        return 5;
    }

    /** Print the time in seconds. */
    public final void printTime(final String msg, final long time1) {
        long duration = System.currentTimeMillis() - time1;
        System.out.println(msg + duration/1000d + " [sec]");
    }

    /** Modify a Test directory */
    public String getTestDir() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Compare two Ujo objects.
     */
    @SuppressWarnings("unchecked")
    public static void assertEquals(Ujo expected, Ujo actual) {

        if (expected==actual) { return; }
        Assertions.assertEquals(expected.getClass(), expected.getClass());

        KeyList keys = expected.readKeys();

        if (expected instanceof UjoTextable) {
            for (int i=keys.size()-1; i>=0; i--) {
                Key key = keys.get(i);
                String o1 = String.valueOf(((UjoTextable)expected).readValueString(key, null));
                String o2 = String.valueOf(((UjoTextable)actual  ).readValueString(key, null));
                Assertions.assertEquals(o1, o2, "Property \"" + key.getName() + "\""); //MSg
            }
        }


        for (int i=keys.size()-1; i>=0; i--) {
            Key<? super Ujo,?> key = keys.get(i);
            Object o1 = key.of(expected);
            Object o2 = key.of(actual);

            String item = "Property \"" + key.getName() + "\"";
            if (byte[].class.equals(key.getType())) {
                assertEquals(item, (byte[]) o1, (byte[]) o2);
            } else if (char[].class.equals(key.getType())) {
                assertEquals(item, (char[]) o1, (char[]) o2);
            } else if (key.isTypeOf(List.class)) {
                assertEquals(item, (List) o1, (List) o2);
            } else {
                Assertions.assertEquals(o1, o2, item);
            }
        }
    }

    /**
     * Compare two Ujo objects.
     */
    public static void assertEquals(String item, byte[] expected, byte[] actual) {
        assertTrue(Arrays.equals(expected, actual), item); // msg
    }

    /**
     * Compare two Ujo objects.
     */
    public static void assertEquals(String item, char[] expected, char[] actual) {
        assertTrue(Arrays.equals(expected, actual), item); // msg
    }

    /**
     * Compare two Ujo objects.
     */
    public static void assertEquals(String item, List expected, List actual) {
        if (expected==actual) { return; }
        Assertions.assertEquals(expected.size(), actual.size(), item); // msg

        if (item.endsWith("\"")) {
            item = item.substring(0, item.length()-1);
        }

        for (int i=0; i<expected.size(); i++) {
            Object oe = expected.get(i);
            Object oa = actual.get(i);
            if (oe==oa) { continue; }
            Assertions.assertEquals(oe, oa, item+"["+i+"\"]");
        }
    }

    /** Call GC and sleep .025 ms */
    protected void callGC() {
        System.gc();
        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    protected void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }
}
