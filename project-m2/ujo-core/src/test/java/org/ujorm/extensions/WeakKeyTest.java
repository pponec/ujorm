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

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import static org.ujorm.extensions.WeakKeyCollectionSample.*;
import static java.lang.Boolean.*;

/**
 *
 * @author ponec
 */
public class WeakKeyTest extends TestCase {
    
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
    public void testDefaultValues() {
        System.out.println("testDefaultValues");
        
        assertEquals(0, NAME.getIndex());
        assertEquals(1, BORN.getIndex());
        assertEquals(2, CASH.getIndex());        
        assertEquals(3, WIFE.getIndex());

        assertEquals(null, NAME.getDefault());
        assertEquals(null, BORN.getDefault());
        assertEquals(0.00, CASH.getDefault());        
        assertEquals(TRUE, WIFE.getDefault());        
    }
    

    /**
     * Test of setValue method, of class WeakKey.
     */
    public void testSetValue2Map_1() {
        System.out.println("testSetValue2Map_1");
        Map<String, Object> map = new HashMap<String, Object>();
        
        assertEquals(null, NAME.of(map));
        assertEquals(null, BORN.of(map));
        assertEquals(TRUE, WIFE.of(map));
        assertEquals(0.00, CASH.of(map));
        
        String name = "Pavel";
        Date today = new Date();
        Boolean wife = true;
        double cash = 10.0;
        
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
        assertEquals(0.0, CASH.of(map));
    }
    
    /**
     * Test of setValue method, of class WeakKey.
     */
    public void testSetValue2List_1() {
        System.out.println("testSetValue2List_2");
        List<Object> list = new ArrayList<Object>();
        
        assertEquals(null, NAME.of(list));
        assertEquals(null, BORN.of(list));
        assertEquals(TRUE, WIFE.of(list));
        assertEquals(0.00, CASH.of(list));
                
        String name = "Eva";
        Date today = new Date();
        Boolean wife = true;
        double cash = 10.0;
        
        NAME.setValue(list, name);
        BORN.setValue(list, today);
        WIFE.setValue(list, wife);
        CASH.setValue(list, cash);
        
        assertEquals(name, NAME.of(list));
        assertEquals(wife, WIFE.of(list));
        assertEquals(cash, CASH.of(list));
        assertEquals(today, BORN.of(list));
        
        // -- Defalut test:

        CASH.setValue(list, null);
        assertEquals(0.0, CASH.of(list));
    }
    
    
    /**
     * Test of setValue method, of class WeakKey.
     */
    public void testSetValue2List_2() {
        System.out.println("testSetValue2List_2");
        List<Object> list = new ArrayList<Object>();
        
        assertEquals(null, NAME.of(list));
        assertEquals(null, BORN.of(list));
        assertEquals(TRUE, WIFE.of(list));
        assertEquals(0.00, CASH.of(list));
                
        String name = "Eva";
        Date today = new Date();
        Boolean wife = true;
        double cash = 10.0;
        
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
        assertEquals(0.0, CASH.of(list));
    }
    
    /**
     * Test of setValue method, of class WeakKey.
     */
    public void testSetValue2List_3() {
        System.out.println("testSetValue2List_3");
        List<Double> list = new ArrayList<Double>();        
        assertEquals(0.00, CASH.of(list));                
        double cash = 10.0;        
        CASH.setValue(list, cash);
        assertEquals(cash, CASH.of(list));
        
        // -- Defalut test:

        CASH.setValue(list, null);
        assertEquals(0.0, CASH.of(list));
    }
    

}
