/*
 *  Copyright 2009-2014 Pavel Ponec
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

package org.ujorm.orm.relation_M2One;

import java.util.Date;
import java.util.logging.*;
import junit.framework.TestCase;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.UjoIterator;
import org.ujorm.criterion.*;
import org.ujorm.logger.UjoLogger;
import org.ujorm.orm.*;
import org.ujorm.orm.ao.CheckReport;
import org.ujorm.orm.dialect.DerbyDialect;
import org.ujorm.orm.dialect.FirebirdDialect;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.utility.OrmTools;
import org.ujorm.orm_tutorial.sample.entity.MyProcedure;
import org.ujorm.orm_tutorial.sample.entity.ViewOrder;

/**
 * The tutorial in the class for the Ujorm <br>
 * --------------------------------------- <br>
 * Learn the basic skills in 15 minutes by a live Java code.
 * The next several methods demonstrate the use of statements:
 *     CREATE TABLE, INSERT, SELECT, UPDATE or DELETE
 * and show how to use a meta-model.
 *
 * Copyright 2010, Pavel Ponec
 */
public class Orm2Test extends TestCase {

    public Orm2Test(String testName) {
        super(testName);
    }

    private static Class suite() {
        return Orm2Test.class;
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

    // ------- TUTORIAL MENU: -------

    public void testMain() {
        Orm2Test sample = this;
        try {
            sample.loadMetaModel();
            sample.useInsert();
            sample.useSelectOrders();
            sample.useSortOrders();
            sample.useSortOrderItems();
          //  sample.useSelectViewOrders();
            sample.useSelectItems_1();
            sample.useSelectItems_2();
            sample.useSelectItems_3();
            sample.useSelectItems_4();
            sample.useReloading();
            sample.useLimitAndOffset();
            sample.useSelectCount();
            sample.useForeignKey();
            sample.useIteratorSkip();
            sample.useRelation();
          //sample.useStoredProcedure(); // create db procedure first
            sample.useUpdate();
            sample.useDelete_0();
            sample.useDelete_1();
            sample.useDelete_2();
            sample.useMetadata();
        } finally {
            sample.useCloseSession();
        }
    }

    // ------- CHAPTERS: -------

    /** The handler contains the one or more database meta-models,
     * the one application can have more OrmHandler instances. */
    private OrmHandler handler;

    /** The session contains a cache and database connections. */
    private Session session;

    /** Before the first use load a meta-model.
     * Database tables will be created in the first time.
     */
    public void loadMetaModel() {

        Logger.getLogger(Ujo.class.getPackage().getName()).setLevel(UjoLogger.TRACE);
        handler = new OrmHandler();

        boolean yesIWantToChangeDefaultParameters = true;
        if (yesIWantToChangeDefaultParameters) {
            MetaParams params = new MetaParams();
            params.set(MetaParams.TABLE_ALIAS_SUFFIX, "_alias");
            params.set(MetaParams.SEQUENCE_CACHE, 1);
            params.set(MetaParams.CHECK_KEYWORDS, CheckReport.EXCEPTION);
            handler.config(params);
        }

        boolean yesIWantToLoadExternalConfig = false;
        if (yesIWantToLoadExternalConfig) {
            java.net.URL config = getClass().getResource("/org/ujorm/orm/sample/config.xml");
            handler.config(config, true);
        }

        handler.loadDatabase(Database.class);
        session = handler.createSession();
    }

    /** Insert one Order and two Items into database. */
    public void useInsert() {

        Order order = new Order();
        order.setDate(new Date());
        order.setNote("John's order");

        Item item1 = new Item();
        item1.setOrder(order);
        item1.setNote("Yellow table");

        Item item2 = new Item();
        item2.setOrder(order);
        item2.setNote("Green window");

        System.out.println("order: " + order);
        System.out.println("item1: " + item1);
        System.out.println("item2: " + item2);

        session.save(order);
        session.save(item1);
        session.save(item2);

        if (true) {
           session.commit();
        } else {
           session.rollback();
        }

        // ------- Check the assigned properties -------

        assert order.readSession() != null;
        assert order.readChangedProperties(false).length == 0;
        order.setNote("John's order");
        assert order.readChangedProperties(false).length == 1;
        session.update(order);
        assert order.readChangedProperties(false).length == 0;

    }

    /** Now, how to select Orders from the database by Criterions? */
    public void useSelectOrders() {

        Criterion<Order> cn1, cn2, cn3, crit;

        cn1 = Criterion.where(Order.note, "John's order");
        cn2 = Criterion.where(Order.created, Operator.LE, new Date());
        cn3 = Criterion.where(Order.state, Order.State.ACTIVE);
        crit = cn1.and(cn2).and(cn3);

        Query<Order> orders = session.createQuery(crit);

        for (Order order : orders) {
            String note = order.getNote();
            System.out.println("ORDER ROW: " + order + " // note: " + note);
        }
    }

    /** Sort orders by two keys: note and created descending. */
    public void useSortOrders() {

        Query<Order> orders = session.createQuery(Order.class);
        orders.orderBy( Order.note
                      , Order.created.descending() );

        System.out.println("VIEW-ORDER COUNT: " + orders.getCount());
    }

    /** Sort items by a <strong>composite</strong> propertry. <br>
     * Note 1: see how a composite key can be used for reading values too. <br>
     * Note 2: the method loadLazyValues(..) is able to load all lazy keys for the Item and its related Order<br>
     */
    public void useSortOrderItems() {

        Query<Item> items = session.createQuery(Item.class);
        items.orderBy( Item.order.add(Order.created) );

        for (Item item : items) {
            OrmTools.loadLazyValues(item, 2);
            System.out.println(item.get( Item.order.add(Order.created) ) + " " + item);
        }
    }

    /** Use a 'native query' where the query is created
     * by a special entity signed by the @View annotation.
     */
    public void useSelectViewOrders() {
        // Some dialects must have got special SQL statements:
        if (session.hasDialect(ViewOrder.class, DerbyDialect.class, FirebirdDialect.class)
        ||  session.getParameters().isQuotedSqlNames()){ // Columns must be quoted
            return;
        }

        Criterion<ViewOrder> crit = Criterion.where(ViewOrder.ID, Operator.GE, 0L);
        Query<ViewOrder> orders = session.createQuery(crit);
        System.out.println("VIEW-ORDER COUNT: " + orders.getCount());

        for (ViewOrder order : orders) {
            System.out.println("ORDER ROW: " + order);
        }
    }

    /** Select all items with a description with the 'table' insensitive text. */
    public void useSelectItems_1() {

        Criterion<Item> crit = Criterion.where(Item.note, Operator.CONTAINS_CASE_INSENSITIVE, "table");
        Query<Item> items = session.createQuery(crit).orderBy(Item.id.descending());

        for (Item item : items) {
            Order order = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order);
        }
    }

    /** Select one Order by id and print its Items by a criterion */
    public void useSelectItems_2() {

        Order orderValue = session.load(Order.class, 1L);
        Criterion<Item> crit = Criterion.where(Item.order, orderValue);
        Query<Item> items = session.createQuery(crit);

        for (Item item : items) {
            Order order2 = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order2);
        }
    }

    /** Select an Order by id and print its Items
     * by a 'one to many' relation key
     */
    public void useSelectItems_3() {
        Order order = session.load(Order.class, 1L);

        for (Item item : order.getItems()) {
            Order order2 = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order2);
        }
    }

    /** Select items by a composed key.
     * It is a sample of a multi-table query.
     * @see Item#$orderDate
     */
    public void useSelectItems_4() {
        Key<Item,Date> ORDER_DATE = Item.order.add(Order.created); // or use: Item.$orderDate
        Criterion<Item> crit = Criterion.where(ORDER_DATE, Operator.LE, new Date());
        Query<Item> items = session.createQuery(crit);

        for (Item item : items) {
            System.out.println("Item: " + item);
        }
    }

    /** How to reload the object key values from the database ? */
    public void useReloading() {
        Order order = new Order();
        order.setId(1L);

        boolean result = session.reload(order);
        System.out.println("Reloading result: " + result + " for Order: " + order);
    }

    /** How to get the latest order by the LIMIT attribute? */
    public void useLimitAndOffset() {
        Order order = session.createQuery(Order.class)
                .setLimit(1)
                .setOffset(0) // The default value can't be specified
                .orderBy(Order.created.descending())
                .uniqueResult()
                ;
        System.out.println("The latest Order: " + order);
    }

    /** How to count items ? */
    public void useSelectCount() {
        Criterion<Item> crit = Criterion.where(Item.note, Operator.CONTAINS_CASE_INSENSITIVE, "table");
        Query<Item> query = session.createQuery(crit);

        long count = query.getCount();
        System.out.println("Count of the order items: " + count);
    }

    /** How to get a Foreign Key without lazy loading */
    public void useForeignKey() {
        Database db = session.getFirstDatabase();
        for (Item item : db.get(Database.ORDER_ITEMS)) {
            ForeignKey fk1 = item.readFK(Item.order);   // before lazy loading
            item.get(Item.order);                       // the lazy loading
            ForeignKey fk2 = item.readFK(Item.order);   // after lazy loading
            System.out.println("FK: " + fk1 + " " + fk1.equals(fk2));
        }
    }


    /** How to skip items? */
    public void useIteratorSkip() {
        Criterion<Item> crit = Criterion.where(Item.note, Operator.NOT_EQ, "XXXXX");
        UjoIterator<Item> items = session.createQuery(crit).iterator();

        boolean skip = items.skip(1);
        if (items.hasNext()) {
            Item item = items.next();
            System.out.println("Item : " + item);
        }

        skip = items.skip(1);
        boolean isNext =  items.hasNext();
        System.out.println("Next: " + isNext);
    }

    /** Sample for 'one to many' relation.
     * 	Note that it is possible to use a Database configuration object too.
     */
    public void useRelation() {
        Database db = session.getDatabase(Database.class);

        UjoIterator<Order> orders = db.get(Database.ORDERS);
        for (Order order : orders) {
            String note = order.getNote();
            System.out.println("Order: " + order + " // note: " + note);

            for (Item item : order.getItems()) {
                Long itemId = item.getId();
                String itemNote = item.getNote();
                System.out.println(" Item id: " + itemId + " note: " + itemNote);
            }
        }
    }

    /** Call a database stored procedure:
     * <code>
     * CREATE OR REPLACE FUNCTION db1.ujorm_test(integer, boolean) RETURNS integer
     *    AS 'select $1 + $1;'
     *    LANGUAGE SQL
     *    IMMUTABLE
     *    RETURNS NULL ON NULL INPUT;
     * </code>
     * Note: the source code is an aarly implementation prototype.<br>
     * Note: test was running on the PostgreSQL release 8.4
     */
    public void useStoredProcedure() {
        MyProcedure procedure = new MyProcedure();

        // Assign input parameters:
        procedure.set(MyProcedure.RESULT, null); // The output parameter(s) can't be initialized.
        procedure.set(MyProcedure.PARAM_CODE, 5);
        procedure.set(MyProcedure.PARAM_ENABLED, true);

        Integer result = procedure.call(session);
        System.out.println("The stored procedure result #1: " + result);

        // See how to reuse input parameters of the object 'procedure':
        procedure.set(MyProcedure.PARAM_CODE, 24);
        result = procedure.call(session, MyProcedure.RESULT); // Take the RESULT of any (output) parameter
        System.out.println("The stored procedure result #2: " + result);
    }

    /** Call a database stored procedure:
     * <code>
     * CREATE OR REPLACE FUNCTION db1.ujorm_test2(integer)
     * RETURNS refcursor AS 'DECLARE mycurs refcursor;
     * BEGIN
     *    OPEN mycurs FOR SELECT 11, 12;
     *    RETURN mycurs;
     * END;'
     * LANGUAGE plpgsql
     * </code>
     * Note: the source code is an aarly implementation prototype. <br>
     * Note: test was running on the PostgreSQL release 8.4
     */
    public void useStoredProcedure_2() {
        MyProcedure procedure = new MyProcedure();
        // MyProcedure procedure2 = session.newProcedure(MyProcedure.class);

        // Assign input parameters:
        procedure.set(MyProcedure.RESULT, null); // The output parameter(s) can't be initialized.
        procedure.set(MyProcedure.PARAM_CODE, 5);
        procedure.set(MyProcedure.PARAM_ENABLED, true);

        Integer result = procedure.call(session);
        System.out.println("The stored procedure result #1: " + result);

        // See how to reuse input parameters of the object 'procedure':
        procedure.set(MyProcedure.PARAM_CODE, 24);
        result = procedure.call(session, MyProcedure.RESULT); // Take the RESULT of any (output) parameter
        System.out.println("The stored procedure result #2: " + result);
    }

    /** Using the UPDATE */
    public void useUpdate() {
        Order order = session.load(Order.class, 1L);
        order.setDate(new Date());

        session.update(order);
        session.commit();
    }

    /** How to DELETE the one loaded object? */
    public void useDelete_0() {
        Item item = null;

        int deleted = session.delete(item);
        session.commit();
        assertEquals(0, deleted);
    }

    /** How to DELETE the one loaded object? */
    public void useDelete_1() {
        Item item = session.createQuery(Item.class).setLimit(1).uniqueResult();

        session.delete(item);
        session.commit();
        System.out.println("There is DELETED object: " + item);
    }

    /** How to use a batch DELETE? */
    public void useDelete_2() {
        Criterion<Item> crit = Criterion.where(Item.id, 1L);
        int count = session.delete(crit);
        session.commit();
        System.out.println("There are DELETED rows: " + count);
    }

    /** Print some meta-data of the key Order.note. */
    public void useMetadata() {
        MetaColumn c = (MetaColumn) handler.findColumnModel(Order.note);

        StringBuilder msg = new StringBuilder()
            .append("** METADATA OF COLUMN: " + Order.note)
            .append("\n\t Length : " + c.getMaxLength())
            .append("\n\t NotNull: " + c.isMandatory())
            .append("\n\t PrimKey: " + c.isPrimaryKey())
            .append("\n\t DB name: " + c.getFullName())
            .append("\n\t Dialect: " + c.getDialectClass().getSimpleName())
            ;
        System.out.println(msg);
    }

    /** Close Ujorm session to clear a session cache
     * and database connection(s).
     */
    public void useCloseSession() {
        if (session!=null) {
            session.close();
        }
    }

}
