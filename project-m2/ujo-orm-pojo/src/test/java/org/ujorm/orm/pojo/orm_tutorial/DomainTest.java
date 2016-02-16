/*
 *  Copyright 2016-2016 Pavel Ponec
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

package org.ujorm.orm.pojo.orm_tutorial;

import junit.framework.TestCase;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.Customer;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.generated.$Customer;


/**
 * Domain test
 * @author Pavel Ponec
 */
public class DomainTest extends TestCase {

    public DomainTest(String testName) {
        super(testName);
    }

    /**
     * Test of getItemCount method, of class AbstractPropertyList.
     */
    public void testCustomer() {
        System.out.println("$Customer");

        Customer customer = new $Customer();
        customer.setId(1L);
        customer.setFirstname("Petr");
        customer.setSurname("Pavel");
        customer.setParent(new Customer());
        //
        assertNotNull(customer);
        assertTrue(customer instanceof Customer);
        assertTrue(customer instanceof $Customer);
        assertSame(customer.getId(), 1L);
        assertSame(customer.getFirstname(), "Petr");
        assertSame(customer.getSurname(), "Pavel");
        assertTrue(customer.getParent() instanceof Customer);
        assertTrue(customer.getParent() instanceof $Customer);
    }

}
