/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.listener;

import junit.framework.TestCase;
import static org.ujoframework.listener.Person.*;

/**
 *
 * @author Pavel Ponec
 */
public class RegistrarUjoExtTest extends TestCase {
    
    public RegistrarUjoExtTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of writeValue method, of class RegistrarExt.
     */
    public void testWriteValueBefore() {
        System.out.println("writeValueBefore");
        
        Listener listenerBefore = new Listener();
        //Listener listenerAfter  = new Listener();
        
        Person person = new Person();
        person.addPropertyChangeListener(ID, true, listenerBefore);
        
        // -----------------------
        Integer valueNew = 100;
        Integer valueOld = person.get(ID);
        person.set(ID, valueNew);
        assertEquals(1       , listenerBefore.size());
        assertEquals(valueOld, listenerBefore.getLastOldValue());
        assertEquals(valueNew, listenerBefore.getLastNewValue());
        
        
        // -----------------------
        valueNew = 220;
        valueOld = person.get(ID);
        person.set(ID, valueNew);
        assertEquals(2       , listenerBefore.size());
        assertEquals(valueOld, listenerBefore.getLastOldValue());
        assertEquals(valueNew, listenerBefore.getLastNewValue());
        

        // -----------------------
        valueNew = Integer.MAX_VALUE;
        valueOld = person.get(ID);
        person.set(ID, valueNew);
        assertEquals(3       , listenerBefore.size());
        assertEquals(valueOld, listenerBefore.getLastOldValue());
        assertEquals(valueNew, listenerBefore.getLastNewValue());
        

    }

    /**
     * Test of writeValue method, of class RegistrarExt.
     */
    public void testWriteValueAfger() {
        System.out.println("writeValueAftef");
        
        Listener listenerAfter = new Listener();
        
        Person person = new Person();
        person.addPropertyChangeListener(ID, false, listenerAfter);
        
        // -----------------------
        Integer valueNew = 100;
        Integer valueOld = person.get(ID);
        person.set(ID, valueNew);
        assertEquals(1       , listenerAfter.size());
        assertEquals(valueOld, listenerAfter.getLastOldValue());
        assertEquals(valueNew, listenerAfter.getLastNewValue());
        
        
        // -----------------------
        valueNew = 220;
        valueOld = person.get(ID);
        person.set(ID, valueNew);
        assertEquals(2       , listenerAfter.size());
        assertEquals(valueOld, listenerAfter.getLastOldValue());
        assertEquals(valueNew, listenerAfter.getLastNewValue());
        

        // -----------------------
        valueNew = Integer.MIN_VALUE;
        valueOld = person.get(ID);
        person.set(ID, valueNew);
        assertEquals(3       , listenerAfter.size());
        assertEquals(valueOld, listenerAfter.getLastOldValue());
        assertEquals(valueNew, listenerAfter.getLastNewValue());
    }


    /**
     * Test of writeValue method, of class RegistrarExt.
     */
    public void testWriteValueBoth_A() {
        System.out.println("writeValueBoth_A");
        
        Listener listenerBoth = new Listener();
        
        Person person = new Person();
        person.addPropertyChangeListener(ID, true , listenerBoth);
        person.addPropertyChangeListener(ID, false, listenerBoth);
        
        // -----------------------
        Integer valueNew = 100;
        Integer valueOld = person.get(ID);
        person.set(ID, valueNew);
        assertEquals(1*2     , listenerBoth.size());
        assertEquals(valueOld, listenerBoth.getLastOldValue());
        assertEquals(valueNew, listenerBoth.getLastNewValue());
        assertEquals(valueOld, listenerBoth.getLast2OldValue());
        assertEquals(valueNew, listenerBoth.getLast2NewValue());
        
        
        // -----------------------
        valueNew = 220;
        valueOld = person.get(ID);
        person.set(ID, valueNew);
        assertEquals(2*2     , listenerBoth.size());
        assertEquals(valueOld, listenerBoth.getLastOldValue());
        assertEquals(valueNew, listenerBoth.getLastNewValue());
        assertEquals(valueOld, listenerBoth.getLast2OldValue());
        assertEquals(valueNew, listenerBoth.getLast2NewValue());
        

        // -----------------------
        valueNew = Integer.MAX_VALUE;
        valueOld = person.get(ID);
        person.set(ID, valueNew);
        assertEquals(3*2     , listenerBoth.size());
        assertEquals(valueOld, listenerBoth.getLastOldValue());
        assertEquals(valueNew, listenerBoth.getLastNewValue());
        assertEquals(valueOld, listenerBoth.getLast2OldValue());
        assertEquals(valueNew, listenerBoth.getLast2NewValue());

    }    

    /**
     * Test of writeValue method, of class RegistrarExt.
     */
    public void testWriteValueBoth_B() {
        System.out.println("writeValueBoth_B");
        
        Listener listenerBoth = new Listener();
        
        Person person = new Person();
        person.addPropertyChangeListener(ID, null, listenerBoth);
        
        // -----------------------
        Integer valueNew = 100;
        Integer valueOld = person.get(ID);
        person.set(ID, valueNew);
        assertEquals(1*2     , listenerBoth.size());
        assertEquals(valueOld, listenerBoth.getLastOldValue());
        assertEquals(valueNew, listenerBoth.getLastNewValue());
        assertEquals(valueOld, listenerBoth.getLast2OldValue());
        assertEquals(valueNew, listenerBoth.getLast2NewValue());
        
        
        // -----------------------
        valueNew = 220;
        valueOld = person.get(ID);
        person.set(ID, valueNew);
        assertEquals(2*2     , listenerBoth.size());
        assertEquals(valueOld, listenerBoth.getLastOldValue());
        assertEquals(valueNew, listenerBoth.getLastNewValue());
        assertEquals(valueOld, listenerBoth.getLast2OldValue());
        assertEquals(valueNew, listenerBoth.getLast2NewValue());
        

        // -----------------------
        valueNew = Integer.MIN_VALUE;
        valueOld = person.get(ID);
        person.set(ID, valueNew);
        assertEquals(3*2     , listenerBoth.size());
        assertEquals(valueOld, listenerBoth.getLastOldValue());
        assertEquals(valueNew, listenerBoth.getLastNewValue());
        assertEquals(valueOld, listenerBoth.getLast2OldValue());
        assertEquals(valueNew, listenerBoth.getLast2NewValue());

    }    
    
}
