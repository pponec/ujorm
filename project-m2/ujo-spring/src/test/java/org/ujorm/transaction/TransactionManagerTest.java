/*
 *  Copyright 2013-2014 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.transaction;

import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.ujorm.transaction.domains.Order;
import org.ujorm.transaction.service.OrderService;

/**
 * Test for the class UjormTransactionManager.
 * @author Pavel Ponec
 */
public class TransactionManagerTest extends TestCase {

    public TransactionManagerTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return TransactionManagerTest.class;
    }

    // ----------------------------------------------------
    
    /**
     * Test of getItemCount method, of class AbstractPropertyList.
     */
    public void testTransactionManager() {
        System.out.println("TransactionManager");

        ApplicationContext context = new ClassPathXmlApplicationContext("/org/ujorm/transaction/config/applicationContext.xml");
        OrderService orderService = (OrderService) context.getBean("orderService");
        Order order = orderService.getOrder(-10L);
        assertNull(order);
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
