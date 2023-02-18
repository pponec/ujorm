/*
 *  Copyright 2013-2022 Pavel Ponec
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

import java.util.Date;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.ujorm.transaction.domains.Order;
import org.ujorm.transaction.service.OrderService;

/**
 * Test for the class UjormTransactionManager.
 * @author Pavel Ponec
 */
public class FaultTransManagerTest extends org.junit.jupiter.api.Assertions {

    private ApplicationContext context;


    /** Test of fault TransactonManager. */
    public void testSubTransactionManager1() {
        System.out.println("testSubTransactionManager1");

        OrderService orderService = getBeanFromContext("orderService");
        Order order = createOrder();
        orderService.save(order);

        try {
           orderService.save(order);
            fail("Missing a duplicity exception");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }

        order = orderService.getOrder(order.getId());
        assertNotNull(order);
    }

    /** Test of fault TransactonManager. */
    public void testSubTransactionManager2() {
        System.out.println("testSubTransactionManager2");

        OrderService orderService = getBeanFromContext("proxyOrderService");
        Order order = createOrder();
        orderService.save(order);

        try {
           orderService.save(order);
            fail("Missing a duplicity exception");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }

        order = orderService.getOrder(order.getId());
        assertNotNull(order);
    }

    /** Test of getItemCount method, of class AbstractPropertyList. */
    public void testSubTransactionManager() {
        System.out.println("testSubTransactionManager");

        OrderService orderService = getBeanFromContext("proxyOrderService");
        Order order = createOrder();
        orderService.save(order);

        order = orderService.getOrder(order.getId());
        assertNotNull(order);
    }

    // ------------- HELPER METHODS -------------

    /** Returns a Spring applicaton context */
    private ApplicationContext getApplicationContext() {
        if (context==null) {
            context = new ClassPathXmlApplicationContext("/org/ujorm/transaction/config/applicationContext.xml");
        }
        return context;
    }

    @SuppressWarnings("unchecked")
    private <T> T getBeanFromContext(String beanName) {
        return (T) getApplicationContext().getBean(beanName);
    }

    private Order createOrder() {
        Order order = new Order();
        order.setCreated(new Date());
        order.setNote("Test");
        order.setState(Order.State.ACTIVE);
        order.setUserId(1000);
        return order;
    }
}
