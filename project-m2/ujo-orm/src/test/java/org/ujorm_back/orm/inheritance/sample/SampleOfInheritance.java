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
package org.ujorm_back.orm.inheritance.sample;

import java.util.logging.*;
import org.ujorm.Ujo;
import org.ujorm_back.orm.*;
import org.ujorm.criterion.*;
import org.ujorm_back.orm.inheritance.sample.bo.Customer;
import org.ujorm_back.orm.inheritance.sample.bo.IUser;
import org.ujorm_back.orm.inheritance.sample.bo.User;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.*;

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
        customer.setCompany("ABC");
        customer.setDiscount(10);
        //
        Session session = handler.getSession();
        // session.save(customer.getUser()); // Since Ujorm 1.20 the statement is not necessary.
        session.save(customer);
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

    /** Now, how to select Customers? */
    public void useSelectCountDistinct() {

        Criterion<Customer> cn1, cn2, crit;

        cn1 = Criterion.where(Customer.user.add(User.login), "ponec");
        cn2 = Criterion.where(Customer.company, "ABC");
        crit = cn1.or(cn2);

        Session session = handler.getSession();
        Query<Customer> customers = session.createQuery(crit);
        customers.setDistinct();

        Long count = customers.getCount();
        System.out.println("Count: " + count);
    }

    /** Print some meta-data of the property Order.note. */
    public void printMetadata() {
        MetaColumn col = (MetaColumn) handler.findColumnModel(Customer.discount);

        String msg = "** METADATA OF COLUMN: " + Customer.discount.toString() + '\n'
            + "DB name: " + col.getFullName()  + '\n'
            + "Comment: " + col.getComment()   + '\n'
            + "Length : " + col.getMaxLength() + '\n'
            + "NotNull: " + col.isMandatory()  + '\n'
            + "Primary: " + col.isPrimaryKey() + '\n'
            + "Dialect: " + col.getDialectName()
            ;
        System.out.println(msg);
    }


    /** Close Ujorm session to clear a session cache including
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
            sample.useSelectCountDistinct();
            sample.printMetadata();

        } finally {
            sample.useCloseSession();
        }
    }
}
