/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.xmlSpeed;


import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import junit.framework.*;
import org.ujorm.MyTestCase;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.core.XmlHeader;


/**
 * XmlSpeedTest
 * @author Pavel Ponec
 */
public class XmlSpeedTest extends MyTestCase {

    public static final int DEEP  = 4;
    public static final int COUNT = 6000;

    public XmlSpeedTest(String testName) {
        super(testName);
    }

    /** Is the JaxbXmlEnabled test enabled? */
    protected boolean isJaxbXmlEnabled() {
        return false;
    }

    protected int getCount() {
        return 10;
        //return 1;
    }


    /** Returns time in minutes */
    private String getTime(long t1, long t2, long t3, long t4) {
        double time1 = (t2 - t1) / 1000d;
        double time2 = (t4 - t3) / 1000d;
        double time3 = (t2 - t1 + t4 - t3) / 1000d;
        return "\tTimes:\t"
        + time1
        + "\t"
        + time2
        + "\t"
        + time3
        ;
    }

    public void testMapXML() throws Throwable {
        System.out.println("MapUJO:Speed: " + suite());
        ByteArrayOutputStream dataFile = createOS(getTestDir()+"data-MAP.xml");


        MapTree tree = new MapTree();
        tree.init(new ZCounter(getCount()), DEEP);
        callGC();

        long time1 = System.currentTimeMillis();
        XmlHeader defaultXmlHeader = new XmlHeader();
        UjoManagerXML.getInstance().saveXML(dataFile, tree, defaultXmlHeader, "Save");
        long time2 = System.currentTimeMillis();
        //
        InputStream is = createIS(dataFile);
        long time3 = System.currentTimeMillis();
        MapTree mapTree =
        UjoManagerXML.getInstance().parseXML(is, MapTree.class, "Load");
        long time4 = System.currentTimeMillis();

        assertEquals(getCount(), mapTree.size()+1);
        System.out.println(">>> MapUJO: "
        + getTime(time1, time2, time3, time4)
        );
    }


    public void testArrayXML() throws Throwable {
        System.out.println("ArrayUJO:Speed: " + suite());
        ByteArrayOutputStream dataFile = createOS(getTestDir()+"data-ARRAY.xml");

        ArrayTree tree = new ArrayTree();
        tree.init(new ZCounter(getCount()), DEEP);
        callGC();

        long time1 = System.currentTimeMillis();
        XmlHeader defaultXmlHeader = new XmlHeader();
        UjoManagerXML.getInstance().saveXML(dataFile, tree, defaultXmlHeader, "Save");
        long time2 = System.currentTimeMillis();
        //
        InputStream is = createIS(dataFile);
        long time3 = System.currentTimeMillis();
        ArrayTree arrayTree =
        UjoManagerXML.getInstance().parseXML(is, ArrayTree.class, "Load");
        long time4 = System.currentTimeMillis();

        assertEquals(getCount(), arrayTree.size()+1);
        System.out.println(">>> ArrayUJO: "
        + getTime(time1, time2, time3, time4)
        );
    }


    public void testBeanUjoXML() throws Throwable {
        System.out.println("BeanUjo:Speed: " + suite());
        ByteArrayOutputStream dataFile = createOS(getTestDir()+"data-POUJO.xml");

        BeanTree tree = new BeanTree();
        tree.init(new ZCounter(getCount()), DEEP);
        callGC();

        long time1 = System.currentTimeMillis();
        XmlHeader defaultXmlHeader = new XmlHeader();
        UjoManagerXML.getInstance().saveXML(dataFile, tree, defaultXmlHeader, "Save");
        long time2 = System.currentTimeMillis();
        //
        InputStream is = createIS(dataFile);
        long time3 = System.currentTimeMillis();
        BeanTree mapTree =
        UjoManagerXML.getInstance().parseXML(is, BeanTree.class, "Load");
        long time4 = System.currentTimeMillis();

        assertEquals(getCount(), mapTree.size()+1);
        System.out.println(">>> BeanUjo: "
        + getTime(time1, time2, time3, time4)
        );
    }



    public void testFieldUjoXML() throws Throwable {
//        System.out.println("FieldUjo:Speed: " + suite().toString());
//        ByteArrayOutputStream dataFile = createOS(getTestDir()+"data-FIELD.xml");
//
//        FieldTree tree = new FieldTree();
//        tree.init(new ZCounter(getCount()), DEEP);
//        callGC();
//
//        long time1 = System.currentTimeMillis();
//        String defaultXmlHeader = null;
//        UjoManagerXML.getInstance().saveXML(dataFile, tree, defaultXmlHeader, "Save");
//        long time2 = System.currentTimeMillis();
//        //
//        InputStream is = createIS(dataFile);
//        long time3 = System.currentTimeMillis();
//        FieldTree fieldTree =
//        UjoManagerXML.getInstance().parseXML(is, FieldTree.class, "Load");
//        long time4 = System.currentTimeMillis();
//
//        assertEquals(getCount(), fieldTree.size()+1);
//        System.out.println(">>> FieldUjo: "
//        + getTime(time1, time2, time3, time4)
//        );
    }


    public void testXMLEncoderXML() throws Throwable {
        System.out.println("XMLEncoder:Speed: " + suite());
        ByteArrayOutputStream dataFile = createOS(getTestDir()+"data-POJO.xml");

        XMLEncoder encoder = null;
        XMLDecoder decoder = null;

        PojoTree tree = new PojoTree();
        tree.init(new ZCounter(getCount()), DEEP);
        callGC();

        try {
            long time1 = System.currentTimeMillis();
            encoder = new XMLEncoder(dataFile);
            encoder.writeObject(tree);
            encoder.close();
            long time2 = System.currentTimeMillis();
            //
            InputStream is = createIS(dataFile);
            decoder = new XMLDecoder(is);
            long time3 = System.currentTimeMillis();
            PojoTree o = (PojoTree) decoder.readObject();
            long time4 = System.currentTimeMillis();

            assertEquals(getCount(), o.size()+1);
            System.out.println(">>> XMLEncoder: "
            + getTime(time1, time2, time3, time4)
            );

        } finally {
            if (encoder!=null) { encoder.close(); }
            if (decoder!=null) { decoder.close(); }
        }
    }

    /** Serializable */
    public void XXX_testSerializableBIN() throws Throwable {
        System.out.println("SerializableBIN:Speed: " + suite());
        ByteArrayOutputStream dataFile = createOS(getTestDir()+"data-POJO.bin");

        ObjectOutput encoder = null;
        ObjectInput  decoder = null;

        PojoTree tree = new PojoTree();
        tree.init(new ZCounter(getCount()), DEEP);
        callGC();

        try {
            long time1 = System.currentTimeMillis();
            encoder = new ObjectOutputStream(dataFile);
            encoder.writeObject(tree);
            encoder.close();
            long time2 = System.currentTimeMillis();
            //
            InputStream is = createIS(dataFile);
            decoder = new ObjectInputStream(is);
            long time3 = System.currentTimeMillis();
            PojoTree o = (PojoTree) decoder.readObject();
            long time4 = System.currentTimeMillis();

            assertEquals(getCount(), o.size()+1);
            System.out.println(">>> PojoBIN: "
            + getTime(time1, time2, time3, time4)
            );

        } finally {
            if (encoder!=null) { encoder.close(); }
            if (decoder!=null) { decoder.close(); }
        }
    }

    public void testJaxbXML() throws Throwable {
        if (isJaxbXmlEnabled()) {

            System.out.println("PojoJAXB:Speed: " + suite());
            ByteArrayOutputStream dataFile = createOS(getTestDir()+"data-jaxb.xml");

            PojoTree root = new PojoTree();
            root.init(new ZCounter(getCount()), DEEP);
            callGC();

            if (true) {
                long time1 = System.currentTimeMillis();
                JAXBContext context = JAXBContext.newInstance(PojoTree.class);
                Marshaller m = context.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                m.marshal(root, dataFile);
                long time2 = System.currentTimeMillis();
                //
                long time3 = System.currentTimeMillis();
                InputStream is = createIS(dataFile);
                Unmarshaller u = context.createUnmarshaller();
                PojoTree o = (PojoTree) u.unmarshal(is);
                long time4 = System.currentTimeMillis();

                assertEquals(getCount(), o.size()+1);
                System.out.println(">>> PojoJAXB: "
                + getTime(time1, time2, time3, time4)
                );
            }
        }
    }

    public void testJavolution() throws Throwable {
    /* Javolution *./
        System.out.println("Javolution:Speed: " + suite().toString());
        ByteArrayOutputStream dataFile = createOS(getTestDir()+"javolution.xml");

        PojoTree root = new PojoTree();
        root.init(new ZCounter(getCount()), DEEP);
        callGC();

        if (true) {
            long time1 = System.currentTimeMillis();

            javolution.xml.XMLBinding binding = new javolution.xml.XMLBinding();
            binding.setAlias(PojoTree.class, "Person");
            binding.setClassAttribute("type");

            // Writes the area to a file.
            javolution.xml.XMLObjectWriter writer = javolution.xml.XMLObjectWriter.newInstance(dataFile);
            writer.setBinding(binding); // Optional.
            writer.setIndentation("  "); // Optional (use tabulation for indentation).
            writer.write(root, "Area", PojoTree.class);
            writer.close();
            long time2 = System.currentTimeMillis();

            // Reads the area back
            long time3 = System.currentTimeMillis();
            InputStream is = createIS(dataFile);
            javolution.xml.XMLObjectReader reader = javolution.xml.XMLObjectReader.newInstance(is);
            reader.setBinding(binding);
            PojoTree o = reader.read("Area", PojoTree.class);
            reader.close();
            long time4 = System.currentTimeMillis();

            assertEquals(getCount(), o.size()+1);
            System.out.println(">>> Javolution: "
            + getTime(time1, time2, time3, time4)
            );

            if (!false) {
                System.out.println("Javolution:\n" + dataFile.toString("UTF-8"));
            }
        }
    /.**/
    }

    // ------------------------------------------------------

    /** Create new ByteArrayOutputStream. */
    public ByteArrayOutputStream createOS(String file) {
        ByteArrayOutputStream result = new ByteArrayOutputStream(8000*1000);
        return result;
    }

    public ByteArrayInputStream createIS(ByteArrayOutputStream data) {
        ByteArrayInputStream result = new ByteArrayInputStream(data.toByteArray());
        return result;
    }
}
