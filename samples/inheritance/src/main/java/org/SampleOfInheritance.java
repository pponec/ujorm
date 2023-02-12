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

import java.util.logging.*;
import org.bo.*;
import org.ujorm.Ujo;
import org.ujorm.orm.*;
import org.ujorm.criterion.*;

/**
 * Sample of inheritance for the persistent objects.
 *
 * Copyright 2010, Pavel Ponec
 */
public class SampleOfInheritance {

    private OrmHandler handler = new OrmHandler();

    /** Before the first use load a metamodel.
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
        customer.setCompany("ABC");
        customer.setDiscount(10);
        //
        Session session = handler.getSession();
        session.insert(customer.getUser());
        session.insert(customer);
        session.commit();

        IUser user = customer; // A test of the INHERITANCE
        System.out.println("User: " + user);
    }

    /** Now, how to select Customers? */
    public void useSelect() {

        Criterion<Customer> cn1, cn2, crit;

        cn1 = Criterion.where(Customer.user.add(User.login), "ponec");
        cn2 = Criterion.where(Customer.company, "ABC");
        crit = cn1.and(cn2);

        Session session = handler.getSession();
        Query<Customer> customers = session.createQuery(crit);

        for (IUser user : customers) {
            System.out.println("User: " + user);
        }
    }

    /** Close Ujorm session to clear a session cache
     * including a database connection(s)
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
