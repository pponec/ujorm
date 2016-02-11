package org.version2.tools;

import junit.framework.TestCase;
import org.version2.bo.generated.*;
import org.version2.bo.*;

/**
 * DefaultUjoConverter Test
 * @author Pavel Ponec
 */
public class UjoPojoConverterTest extends TestCase {

    /**
     * Test of marshal method, of class DefaultUjoConverter.
     */
    public void testMarshal_1() {
        System.out.println("marshal_1");
        Account v = new Account();
        DefaultUjoConverter<$Account> instance = new DefaultUjoConverter<$Account>();
        Object result = instance.marshal(v);
        assertTrue(Account.class.isInstance(result));
        assertTrue($Account.class.isInstance(result));
        //
        Integer expected = 10;
        v.setId(expected);
        assertEquals((($Account)result).getId(), v.getId());
    }

    /**
     * Test of marshal method, of class DefaultUjoConverter.
     */
    public void testMarshal_2() {
        System.out.println("marshal_2");
        Address v = new Address();
        DefaultUjoConverter<$Address> instance = new DefaultUjoConverter<$Address>();
        Object result = instance.marshal(v);
        assertTrue(Address.class.isInstance(result));
        assertTrue($Address.class.isInstance(result));
        //
        Integer expected = 10;
        v.setId(expected);
        assertEquals((($Address)result).getId(), v.getId());
    }

    /**
     * Test of unmarshal method, of class DefaultUjoConverter.
     */
    public void testUnmarshal_1() {
        System.out.println("unmarshal_1");
        $Account v = new $Account(new Account());
        DefaultUjoConverter<$Account> instance = new DefaultUjoConverter<$Account>();
        Object result = instance.unmarshal(v);
        assertTrue(Account.class.isInstance(result));
        assertFalse($Account.class.isInstance(result));
        //
        Integer expected = 10;
        v.setId(expected);
        assertEquals(((Account)result).getId(), v.getId());
    }

    /**
     * Test of unmarshal method, of class DefaultUjoConverter.
     */
    public void testUnmarshal_2() {
        System.out.println("unmarshal_2");
        $Address v = new $Address(new Address());
        DefaultUjoConverter<$Address> instance = new DefaultUjoConverter<$Address>();
        Object result = instance.unmarshal(v);
        assertTrue(Address.class.isInstance(result));
        assertFalse($Address.class.isInstance(result));
        //
        Integer expected = 10;
        v.setId(expected);
        assertEquals(((Address)result).getId(), v.getId());
    }

}
