/*
 *  Copyright 2010 Pavel Ponec
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
package org;

import java.math.BigDecimal;
import java.util.logging.*;
import org.bo.Customer;
import org.bo.User;
import org.ujoframework.Ujo;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.orm.*;
import org.ujoframework.criterion.*;
import static org.bo.Customer.*;

/**
 * Sample of inheritance for the persistent objects.
 *
 * Copyright 2010, Pavel Ponec
 */
public class SampleOfInheritance {

    private OrmHandler handler = new OrmHandler();

    /** Before the first use load a meta-model.
     * Database tables will be name in the first time.
     */
    public void loadMetaModel() {

        Logger.getLogger(Ujo.class.getPackage().getName()).setLevel(Level.ALL);
        handler.loadDatabase(Database.class);
    }

    /** Insert one User and two Items into database. */
    public void useInsert() {

        Customer customer = Customer.newInstance();
        //
        customer.setLogin("ponec");
        customer.setPassword("xxx");
        customer.setName("Pavel Ponec");
        customer.set(company, "ABC");
        customer.set(discount, 10);
        //
        Session session = handler.getSession();
        session.save(customer.getUser());
        session.save(customer);
        session.commit();
    }

    /** Now, how to select Orders from the database by Criterions? */
    public void useSelect() {

        Criterion<Customer> cn1, cn2, crit;

        cn1 = Criterion.where(Customer.user.add(User.login), "ponec");
        cn2 = Criterion.where(Customer.company, "ABC");
        crit = cn1.and(cn2);

        Session session = handler.getSession();
        UjoIterator<Customer> customers = session.createQuery(crit).iterate();

        for (Customer customer : customers) {
            System.out.println("Customer: " + customer);
        }
    }

    /** Close Ujorm session to clear a session cache include
     * a database connection(s)
     */
    public void useCloseSession() {
        handler.getSession().close();
    }

    /** Run the tutorial */
    public static void main(String[] args) {
        SampleOfInheritance sample = new SampleOfInheritance();

        try {
            sample.loadMetaModel();
            sample.useInsert();
            sample.useSelect();

        } finally {
            sample.useCloseSession();
        }
    }
}
