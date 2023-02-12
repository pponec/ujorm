/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.xmlSpeed;

import junit.framework.TestSuite;


/**
 * XmlSpeedTest2 - this is no regular test case.
 * @author Pavel Ponec
 */
public class XmlSpeedTest2 extends XmlSpeedTest {

    public XmlSpeedTest2(String testName) {
        super(testName);
    }

    /** Is the JaxbXmlEnabled test enabled? */
    @Override
    protected boolean isJaxbXmlEnabled() {
        return true;
    }

    public static TestSuite suite() {
        return new TestSuite(XmlSpeedTest2.class);
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

    @Override
    protected int getCount() {
        return COUNT;
    }

    // ---------------------------------------------------

    public void test2MapXML1() throws Throwable {
        System.out.println("MapUJO.Speed:WARMING: " + suite());
        super.testMapXML();
    }

    public void test2ArrayXML1() throws Throwable {
        System.out.println("ArrayUJO.Speed:WARMING: " + suite());
        super.testArrayXML();
    }

    public void testBeanUjoXML1() throws Throwable {
        System.out.println("BeanUjo.Speed:WARMING: " + suite());
        super.testBeanUjoXML();
    }

    public void testFieldUjoXML1() throws Throwable {
        System.out.println("FieldUjo.Speed:WARMING: " + suite());
        super.testFieldUjoXML();
    }

    // -----

    public void testXMLEncoderXML1() throws Throwable {
        System.out.println("XMLEncoder.Speed:WARMING: " + suite());
        super.testXMLEncoderXML();
    }

    public void testJaxbXML1() throws Throwable {
        if (isJaxbXmlEnabled()) {
            System.out.println("PojoJAXB.Speed:WARMING: " + suite());
            super.testJaxbXML();
        }
    }

    public void testJavolution1() throws Throwable {
        System.out.println("Javolution .Speed:WARMING: " + suite());
        super.testJavolution();
    }

    public void testSeparator() {
        System.out.println("= = = = = = = = = = = = = = = = = = ");
    }



}
