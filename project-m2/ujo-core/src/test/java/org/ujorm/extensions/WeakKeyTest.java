/*
 * Copyright 2012 ponec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.extensions;

import samples.weakKey.MyService;
import java.math.BigDecimal;
import org.ujorm.WeakKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import static java.lang.Boolean.*;
import static java.math.BigDecimal.*;

/**
 *
 * @author ponec
 */
public class WeakKeyTest extends TestCase {
    
    private static final WeakKeyFactory f = new WeakKeyFactory(WeakKeyTest.class); 
    
    public static final WeakKey<String> NAME = f.newKey();
    public static final WeakKey<Date>   BORN = f.newKey();
    public static final WeakKey<Boolean> WIFE = f.newKeyDefault(TRUE);
    public static final WeakKey<BigDecimal> CASH = f.newKeyDefault(ZERO);
    
    static {
        f.lock();
    }

    public WeakKeyTest(String testName) {
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
     * Test of setValue method, of class WeakKey.
     */
    public void testKeyAtrributes() {
        System.out.println("testKeyAtrributes");
        
        assertEquals(0, NAME.getIndex());
        assertEquals(1, BORN.getIndex());
        assertEquals(2, WIFE.getIndex());
        assertEquals(3, CASH.getIndex());

        assertEquals("name", NAME.getName());
        assertEquals("born", BORN.getName());
        assertEquals("wife", WIFE.getName());
        assertEquals("cash", CASH.getName());        
        
        assertSame(null, NAME.getDefault());
        assertSame(null, BORN.getDefault());
        assertSame(TRUE, WIFE.getDefault());        
        assertSame(ZERO, CASH.getDefault());        
    }

    /**
     * Test of setValue method, of class WeakKey.
     */
    public void testSetValue2Map_1() {
        System.out.println("testSetValue2Map_1");
        Map<String, Object> map = new HashMap<String, Object>();
        
        assertSame(null, NAME.of(map));
        assertSame(null, BORN.of(map));
        assertSame(TRUE, WIFE.of(map));
        assertSame(ZERO, CASH.of(map));
        
        String name = "Pavel";
        Boolean wife = false;
        Date today = new Date();
        BigDecimal cash = TEN;
        
        NAME.setValue(map, name);
        WIFE.setValue(map, wife);
        CASH.setValue(map, cash);
        BORN.setValue(map, today);
        
        assertEquals(name, NAME.of(map));
        assertEquals(wife, WIFE.of(map));
        assertEquals(cash, CASH.of(map));
        assertEquals(today, BORN.of(map));
        
        // -- Defalut test:

        CASH.setValue(map, null);
        assertEquals(ZERO, CASH.of(map));
    }
    
    /**
     * Test of setValue method, of class WeakKey.
     */
    public void testSetValue2List_1() {
        System.out.println("testSetValue2List_1");
        List<Object> list = new ArrayList<Object>();
        
        assertEquals(null, NAME.of(list));
        assertEquals(TRUE, WIFE.of(list));
        assertEquals(null, BORN.of(list));
        assertEquals(ZERO, CASH.of(list));
                
        String name = "Eva";
        Boolean wife = true;
        Date today = new Date();
        BigDecimal cash = TEN;
        
        NAME.setValue(list, name);
        WIFE.setValue(list, wife);
        BORN.setValue(list, today);
        CASH.setValue(list, cash);
        
        assertEquals(name, NAME.of(list));
        assertEquals(wife, WIFE.of(list));
        assertEquals(cash, CASH.of(list));
        assertEquals(today, BORN.of(list));
        
        // -- Defalut test:

        CASH.setValue(list, null);
        assertEquals(ZERO, CASH.of(list));
    }
    
    
    /**
     * Test of setValue method, of class WeakKey.
     */
    public void testSetValue2List_2() {
        System.out.println("testSetValue2List_2");
        List<Object> list = new ArrayList<Object>();
        
        assertEquals(null, NAME.of(list));
        assertEquals(TRUE, WIFE.of(list));
        assertEquals(null, BORN.of(list));
        assertEquals(ZERO, CASH.of(list));
                
        String name = "Eva";
        Boolean wife = true;
        Date today = new Date();
        BigDecimal cash = TEN;
        
        BORN.setValue(list, today);
        WIFE.setValue(list, wife);
        CASH.setValue(list, cash);
        NAME.setValue(list, name);
        
        assertEquals(wife, WIFE.of(list));
        assertEquals(cash, CASH.of(list));
        assertEquals(name, NAME.of(list));
        assertEquals(today, BORN.of(list));
        
        // -- Defalut test:

        CASH.setValue(list, null);
        assertEquals(ZERO, CASH.of(list));
    }
    
    /**
     * Test of setValue method, of class WeakKey.
     */
    public void testSetValue2List_3() {
        System.out.println("testSetValue2List_3");
        List<BigDecimal> list = new ArrayList<BigDecimal>();        
        assertEquals(ZERO, CASH.of(list));                
        BigDecimal cash = TEN;        
        CASH.setValue(list, cash);
        assertEquals(cash, CASH.of(list));
        
        // -- Defalut test:

        CASH.setValue(list, null);
        assertEquals(ZERO, CASH.of(list));
    }
    
    /** Test the sample class MyService */
    public void testMyService() {
        System.out.println("testMyService");
        new MyService().testWeakKeys();
        new MyService().testWeakKeyAttributes();
    }
    

}
