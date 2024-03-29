/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.xmlSpeed;

import org.junit.jupiter.api.Test;


/**
 * XmlSpeedTest2 - this is no regular test case.
 * @author Pavel Ponec
 */
public class XmlSpeedTest2 extends XmlSpeedTest {

    /** Is the JaxbXmlEnabled test enabled? */
    @Override
    protected boolean isJaxbXmlEnabled() {
        return true;
    }

    @Override
    protected int getCount() {
        return COUNT;
    }

    // ---------------------------------------------------

    @Test
    public void test2MapXML1() throws Throwable {
        System.out.println("MapUJO.Speed:WARMING: " + testName());
        super.testMapXML();
    }

    @Test
    public void test2ArrayXML1() throws Throwable {
        System.out.println("ArrayUJO.Speed:WARMING: " + testName());
        super.testArrayXML();
    }

    @Test
    public void testBeanUjoXML1() throws Throwable {
        System.out.println("BeanUjo.Speed:WARMING: " + testName());
        super.testBeanUjoXML();
    }

    @Test
    public void testFieldUjoXML1() throws Throwable {
        System.out.println("FieldUjo.Speed:WARMING: " + testName());
        super.testFieldUjoXML();
    }

    // -----

    @Test
    public void testXMLEncoderXML1() throws Throwable {
        System.out.println("XMLEncoder.Speed:WARMING: " + testName());
        super.testXMLEncoderXML();
    }

    @Test
    public void testJaxbXML1() throws Throwable {
        if (isJaxbXmlEnabled()) {
            System.out.println("PojoJAXB.Speed:WARMING: " + testName());
            super.testJaxbXML();
        }
    }

    @Test
    public void testJavolution1() throws Throwable {
        System.out.println("Javolution .Speed:WARMING: " + testName());
        super.testJavolution();
    }

    @Test
    public void testSeparator() {
        System.out.println("= = = = = = = = = = = = = = = = = = ");
    }



}
