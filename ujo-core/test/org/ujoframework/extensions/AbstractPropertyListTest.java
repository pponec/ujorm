/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.extensions;

import junit.framework.TestSuite;
import org.ujoframework.MyTestCase;
import static org.ujoframework.extensions.PersonExt.*;

/**
 * List testing ...
 * @author pavel
 */
public class AbstractPropertyListTest extends MyTestCase {
    
    public AbstractPropertyListTest(String testName) {
        super(testName);
    }  
    
    public static TestSuite suite() {
        return new TestSuite(AbstractPropertyListTest.class);
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
     * Test of getItemCount method, of class AbstractPropertyList.
     */
    public void testGetItemCount() {
        System.out.println("getItemCount");
        Person root = new Person(0);
        Person p0 = new Person(0);
        Person p1 = new Person(1);
        Person px = new Person(2);
        Person p  = null;
        
        assertEquals(0, Person.PERS.getItemCount(root));
        Person.PERS.addItem(root, p0);
        assertEquals(1, Person.PERS.getItemCount(root));
        Person.PERS.addItem(root, p1);
        assertEquals(2, Person.PERS.getItemCount(root));
        //
        
        boolean result = Person.PERS.removeItem(root, p0);
        assertEquals(true, result);
        assertEquals(1, Person.PERS.getItemCount(root));
        result = Person.PERS.removeItem(root, p0);
        assertEquals(false, result);
        assertEquals(1, Person.PERS.getItemCount(root));
        result = Person.PERS.removeItem(root, p1);
        assertEquals(true, result);
        assertEquals(0, Person.PERS.getItemCount(root));
        result = Person.PERS.removeItem(root, p1);
        assertEquals(false, result);
        assertEquals(0, Person.PERS.getItemCount(root));
        //
        
        assertEquals(0, Person.PERS.getItemCount(root));
        Person.PERS.addItem(root, p0);
        assertEquals(1, Person.PERS.getItemCount(root));
        Person.PERS.addItem(root, p1);
        assertEquals(2, Person.PERS.getItemCount(root));
        //
        
        assertEquals(p0, Person.PERS.getItem(root, 0));
        assertEquals(p1, Person.PERS.getItem(root, 1));
        //
        int i = 1;
        p = Person.PERS.setItem(root, i, px);
        assertEquals(p1, p);
        assertEquals(px, Person.PERS.getItem(root, i));
        //
        i = 0;
        p = Person.PERS.setItem(root, i, px);
        assertEquals(p0, p);
        assertEquals(px, Person.PERS.getItem(root, i));
        //
        
        root = new Person(0);
        result = Person.PERS.removeItem(root, p0);
        assertEquals(false, false);
    }

    /**
     * Test of getItemCount method, of class AbstractPropertyList.
     */
    public void testGetItemCount_EXT() {
        System.out.println("getItemCount_EXT");
        PersonExt root = new PersonExt(0);
        PersonExt p0 = new PersonExt(0);
        PersonExt p1 = new PersonExt(1);
        PersonExt px = new PersonExt(2);
        PersonExt p  = null;
        
        assertEquals(0, root.getItemCount(PERS));
        root.add(PERS, p0);
        assertEquals(1, PersonExt.PERS.getItemCount(root));
        root.add(PERS, p1);
        assertEquals(2, PersonExt.PERS.getItemCount(root));
        //
        
        boolean result = root.remove(PERS, p0);
        assertEquals(true, result);
        assertEquals(1, root.getItemCount(PERS));
        result = root.remove(PERS, p0);
        assertEquals(false, result);
        assertEquals(1, root.getItemCount(PERS));
        result = PersonExt.PERS.removeItem(root, p1);
        assertEquals(true, result);
        assertEquals(0, root.getItemCount(PERS));
        result = root.remove(PERS, p1);
        assertEquals(false, result);
        assertEquals(0, root.getItemCount(PERS));
        //
        
        assertEquals(0, root.getItemCount(PERS));
        PersonExt.PERS.addItem(root, p0);
        assertEquals(1, root.getItemCount(PERS));
        PersonExt.PERS.addItem(root, p1);
        assertEquals(2, root.getItemCount(PERS));
        //
        
        assertEquals(p0, root.get(PERS, 0));
        assertEquals(p1, root.get(PERS, 1));
        //
        int i = 1;
        p = root.list(PERS).set(i, px);
        assertEquals(p1, p);
        assertEquals(px, root.get(PERS, i));
        //
        i = 0;
        p = PersonExt.PERS.setItem(root, i, px);
        assertEquals(p0, p);
        assertEquals(px, root.get(PERS, i));
        //
        
        root = new PersonExt(0);
        result = root.remove(PERS, p0);
        assertEquals(false, false);
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

}
