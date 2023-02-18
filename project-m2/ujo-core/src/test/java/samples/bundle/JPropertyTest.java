/*
 * JPropertyTest.java
 * JUnit based test
 *
 * Created on 16. June 2007, 15:23
 */

package samples.bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import junit.framework.*;

/**
 *
 * @author Pavel Ponec
 */
public class JPropertyTest extends TestCase {

    /**
     * Test of getItemClass method, of class org.ujorm.core.UjoManagerXML.
     * @throws java.io.IOException
     */
    public void testProperty() throws IOException {

        java.util.Properties props = new Properties();
        props.setProperty("id", "ABC789");
        props.setProperty("firstname", "Pavel");
        props.setProperty("lastname", "Ponec");
        props.put("test_01", " Very Long String ... ");
        props.put("test_02", "Úplně žluťoučný kůň = ŘÍP");

        if (false) {
            props.put("test_01", Integer.valueOf(256));
            props.put("test_02", new Float(25688.25));
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream(128);
        props.storeToXML(os, "Commnent", "UTF-8");
        System.err.println(":>>\n" + new String(os.toByteArray(), "UTF-8"));

        os = new ByteArrayOutputStream(128);
        props.store(os, "HEADER is not mandatory");
        System.err.println(":>>\n" + new String(os.toByteArray(), "UTF-8"));
    }

}