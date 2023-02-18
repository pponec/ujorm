/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.xmlSpeed;

import junit.framework.TestSuite;
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
        System.out.println("MapUJO.Speed:WARMING: " + suite());
        super.testMapXML();
    }

    @Test
    public void test2ArrayXML1() throws Throwable {
        System.out.println("ArrayUJO.Speed:WARMING: " + suite());
        super.testArrayXML();
    }

    @Test
    public void testBeanUjoXML1() throws Throwable {
        System.out.println("BeanUjo.Speed:WARMING: " + suite());
        super.testBeanUjoXML();
    }

    @Test
    public void testFieldUjoXML1() throws Throwable {
        System.out.println("FieldUjo.Speed:WARMING: " + suite());
        super.testFieldUjoXML();
    }

    // -----

    @Test
    public void testXMLEncoderXML1() throws Throwable {
        System.out.println("XMLEncoder.Speed:WARMING: " + suite());
        super.testXMLEncoderXML();
    }

    @Test
    public void testJaxbXML1() throws Throwable {
        if (isJaxbXmlEnabled()) {
            System.out.println("PojoJAXB.Speed:WARMING: " + suite());
            super.testJaxbXML();
        }
    }

    @Test
    public void testJavolution1() throws Throwable {
        System.out.println("Javolution .Speed:WARMING: " + suite());
        super.testJavolution();
    }

    @Test
    public void testSeparator() {
        System.out.println("= = = = = = = = = = = = = = = = = = ");
    }



}
