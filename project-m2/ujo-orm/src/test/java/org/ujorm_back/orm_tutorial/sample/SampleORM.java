/*
 *  Copyright 2009-2011 Pavel Ponec
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

package org.ujorm_back.orm_tutorial.sample;

import org.ujorm.orm.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.*;
import org.ujorm.Ujo;
import org.ujorm.UjoProperty;
import org.ujorm.core.UjoIterator;
import org.ujorm.criterion.*;
import org.ujorm.orm.ao.CachePolicy;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.utility.OrmTools;
import static org.ujorm.criterion.Operator.*;

/**
 * The tutorial in the class for the Ujorm <br>
 * --------------------------------------- <br>
 * Learn the basic skills in 15 minutes by a live Java code.
 * The next several methods demonstrate the use of statements:
 *     CREATE TABLE, INSERT, SELECT, UPDATE or DELETE
 * and show how to use a meta-model.
 *
 * Entities: <pre>
 *  - Order [ID, NOTE, CREATED, ...]
 *  - Item [ID, ORDER, NOTE, ...]
 * </pre>

 * Copyright 2011, Pavel Ponec
 *
 * @see Order
 * @see Item
 */
public class SampleORM {

    // ------- TUTORIAL MENU: -------

    public static void main(String[] args) {
        SampleORM sample = new SampleORM();
        try {
            sample.createMetaModel();
            sample.useInsert();
            sample.useSelect();
            sample.useBatchInsert();
            sample.useCriterions();
            sample.useSortOrders();
            sample.useSortOrderItems();
            sample.useSelectViewOrders();
            sample.useSelectWithNativeSQL();
            sample.useSelectItems_1();
            sample.useSelectItems_2();
            sample.useSelectItems_3();
            sample.useSelectItems_4();
            sample.useSelectItems_5();
            sample.useSelectItems_5b();
            sample.useOptimizedSelect();
            sample.useNativeCriterion();
            sample.useReloading();
            sample.useLimitAndOffset();
            sample.useSelectCount();
            sample.useForeignKey();
            sample.useIteratorSkip();
            sample.useRelation();
            sample.useStoredProcedure();
            sample.useUpdate();
            sample.useBatchUpdate();
            sample.usePesimisticUpdate();
            sample.useDelete();
            sample.useBatchDelete();
            sample.useMetadata();
        } finally {
            sample.useCloseSession();
        }
    }

    // ------- CHAPTERS: -------

    /** The handler contains the one or more database meta-models,
     * the one applicatin can have more OrmHandler instances. */
    private OrmHandler handler;

    /** The session contains a cache and database connections. */
    private Session session;

    /** Before the first: create a meta-model.
     * Database tables will be CREATED in the first time.
     */
    public void createMetaModel() {

        // Set the log level specifying which message levels will be logged by Ujorm:
        Logger.getLogger(Ujo.class.getPackage().getName()).setLevel(Level.FINE);

        // Create new ORM Handler:
        handler = new OrmHandler();

        // There are prefered default properties for a production environment:
        boolean yesIWantToChangeDefaultParameters = true;
        if (yesIWantToChangeDefaultParameters) {
            MetaParams params = new MetaParams();
            params.set(MetaParams.TABLE_ALIAS_SUFFIX, "_alias");
            params.set(MetaParams.SEQUENCE_SCHEMA_SYMBOL, true);
            params.set(MetaParams.CACHE_POLICY, CachePolicy.SOLID_CACHE);
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
        order.setCreated(new Date());
        order.setNote("My order");
        //ORDER.setBinaryFile("binary".getBytes());

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
    }

    /** Batch insert by a multi row insert statement. */
    public void useBatchInsert() {

        Order order = session.createQuery(Order.class).orderBy(Order.ID.descending()).setLimit(1).uniqueResult();
        List<Item> itemList = new ArrayList<Item>();

        for (int i = 0; i < 3; i++) {
            Item item = new Item();
            item.setOrder(order);
            item.setNote("Item number #i");
            itemList.add(item);
        }
        session.save(itemList);
        session.commit();
    }

    /** Now, how to select Orders from a database by Criterions? <br/>
     * The generated SQL code from this example
     * will be similar like the next statement:
     * <pre>
     * SELECT * FROM item
     * JOIN order ON order.id = item.id_order
     * WHERE item.id >= 1
     *   AND item.note LIKE '%table%'
     *   AND order.note = 'My order';
     * </pre>
     * where both parameters are passed by a 'question mark' notation
     * for a better security.
     */
    public void useSelect() {
        Criterion<Item> crn = Item.ID.where(GE, 1L )
            .and( Item.NOTE.where(CONTAINS, "table" ) )
            .and( Item.ORDER.add(Order.NOTE).whereEq( "My order" ) )
            ;

        for (Item item : session.createQuery(crn)) {
            Date created = item.getOrder().getCreated(); // Lazy loading
            System.out.println("Item: " + item + " // created: " + created);
        }
    }

    /** Lern how to use the Criterion as an simple object validator only. */
    public void useCriterions() {

        final Order order = new Order();
        order.setId(100L);
        order.setNote("my order");
        order.setCreated(new Date());

        Criterion<Order> crnId, crnNote, crnCreated, crn;
        crnId = Order.ID.where(GT, 99L);
        crnNote = Order.NOTE.whereEq("another");
        crnCreated = Order.CREATED.where(LE, new Date());
        crn = null;

        // Simple condition: Order.ID>99
        assert crnId.evaluate(order);

        // Compound condition: Order.ID>99 or Order.NOTE='another'
        crn = crnId.or(crnNote);
        assert crn.evaluate(order);

        // Compound condition with parentheses: Order.CREATED<=now() and (Order.NOTE='another' or Order.ID>99)
        crn = crnCreated.and(crnNote.or(crnId));
        assert crn.evaluate(order);

        // Another condition: (Order.CREATED<=now() or Order.NOTE='another') and Order.ID>99
        crn = (crnCreated.or(crnNote)).and(crnId);
        // ... or simple by a native priority:
        crn =  crnCreated.or(crnNote).and(crnId);
        assert crn.evaluate(order);
    }

    /** Sort orders by two properties: NOTE and CREATED descending. */
    public void useSortOrders() {

        Query<Order> orders = session.createQuery(Order.class);
        orders.orderBy( Order.NOTE
                      , Order.CREATED.descending() );

        System.out.println("VIEW-ORDER COUNT: " + orders.getCount());
    }

    /** Sort items by a <strong>composite</strong> propertry. <br>
     * Note 1: see how a composite property can be used for reading values too. <br>
     * Note 2: the metod loadLazyValues(..) is able to load all lazy properties for the Item and its related Order<br>
     */
    public void useSortOrderItems() {

        Query<Item> items = session.createQuery(Item.class);
        items.orderBy(Item.ORDER.add(Order.CREATED));

        for (Item item : items) {
            OrmTools.loadLazyValues(item, 2);
            System.out.println(item.get(Item.ORDER.add(Order.CREATED)) + " " + item);
        }
        // Another way to avoid the lazy loading by a bulk property loading:
        List<Item> itemList = OrmTools.loadLazyValuesAsBatch(items);
        System.out.println("itemList: " + itemList);
    }

    /** Use a 'native query' where the query is CREATED
     * by a special entity signed by the @View annotation. <br/>
     * Note the special <strong>inner parameter</strong> in the SQL statement on the Annotation of the class ViewOrder,
     * where value for this (optional) parameter is set by the method Query.setSqlParameters();
     * @see Query#setSqlParameters(java.lang.Object[])
     */
    public void useSelectViewOrders() {
        Criterion<ViewOrder> crit = ViewOrder.ITEM_COUNT.whereGt(0);

        long minimalOrderId = 0L;
        long orderCount = session.createQuery(crit)
                .setSqlParameters(minimalOrderId)
                .getCount()
                ;
        System.out.println("Order Count: " + orderCount);

        Query<ViewOrder> orders = session.createQuery(crit)
                .setLimit(5)
                .orderBy(ViewOrder.ID)
                .setSqlParameters(0)
                ;
        for (ViewOrder order : orders) {
            System.out.println("ORDER ROW: " + order);
        }
    }

    /** Use a 'native query' where the query is CREATED
     * by a special entity signed by the @View annotation. <br/>
     * Note the special <strong>inner parameter</strong> in the SQL statement on the Annotation of the class ViewOrder,
     * where value for this (optional) parameter is set by the method Query.setSqlParameters();
     * @see Query#setSqlParameters(java.lang.Object[])
     */
    public void useSelectWithNativeSQL() {
        final Long excludedId = -7L;
        SqlParameters sql = new SqlParameters().setSqlStatement
                ( "SELECT * FROM ("
                + "SELECT ord_order_alias.id"
                +         ", 1000 + count(*) AS item_count"
                + " FROM ${SCHEMA}.ord_order ord_order_alias"
                + " LEFT JOIN ${SCHEMA}.ord_item ord_item_alias"
                + " ON ord_order_alias.id = ord_item_alias.fk_order"
                + " WHERE ord_item_alias.id != ?" // Parameter is replaced by the excludedId
                + " GROUP BY ord_order_alias.id"
                + " ORDER BY ord_order_alias.id"
                + ") testView WHERE true"
                ).setParameters(excludedId);
        Criterion<ViewOrder> crit = ViewOrder.ITEM_COUNT.whereLe(100);
        long orderCount = session.createQuery(crit)
                .setSqlParameters(sql)
                .getCount()
                ;
        System.out.println("Order Count: " + orderCount);

        Query<ViewOrder> orders = session.createQuery(crit)
                .setLimit(5)
                .orderBy(ViewOrder.ID)
                .setSqlParameters(sql)
                ;
        for (ViewOrder order : orders) {
            System.out.println("ORDER ROW: " + order);
        }
    }

    /** Select all ITEMS with a description with the 'table' insensitive text. */
    public void useSelectItems_1() {

        Criterion<Item> crit = Item.NOTE.where(CONTAINS_CASE_INSENSITIVE, "table");
        Query<Item> items = session.createQuery(crit).orderBy(Item.ID.descending());

        for (Item item : items) {
            Order order = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order);
        }
    }

    /** Select one Order by ID and print its Items by a criterion */
    public void useSelectItems_2() {

        Order orderValue = session.load(Order.class, 1L);
        Query<Item> items = session.createQuery(Item.ORDER.whereEq(orderValue));

        for (Item item : items) {
            Order order2 = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order2);
        }
    }

    /** Select an Order by ID and print its Items
     * by a 'one to many' relation property
     */
    public void useSelectItems_3() {
        Order order = session.load(Order.class, 1L);

        for (Item item : order.getItems()) {
            Order order2 = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order2);
        }
    }

    /** Select items by a composed property.
     * It is a sample of a multi-table query.
     * @see Item#$ORDER_CREATED
     */
    public void useSelectItems_4() {
        UjoProperty<Item, Date> ORDER_DATE = Item.ORDER.add(Order.CREATED); // or use: Item.$ORDER_CREATED
        Query<Item> items = session.createQuery(ORDER_DATE.whereLe(new Date()));

        for (Item item : items) {
            System.out.println("Item: " + item);
        }
    }

    /** Select items by a composed property.
     * It is a sample of a multi-table query.
     * See used Criterion with the whereIn method. The value list can be empty and the RESULT returns FALSE always in this case.
     * @see Item#$ORDER_CREATED
     */
    public void useSelectItems_5() {
        Query<Item> items = session.createQuery(Item.ID.whereIn(1L,2L,3L,4L,5L));

        for (Item item : items) {
            System.out.println("Item: " + item);
        }
    }

    /** Select using the IN operator with persistent objects. */
    public void useSelectItems_5b() {
        Order orderA = new Order(1L);
        Order orderB = new Order(2L);

        for (Item item : session.createQuery(Item.ORDER.whereIn(orderA, orderB))) {
            System.out.println("Item: " + item);
        }
    }

    /** Create a SELECT for the one column only
     * with no duplicate rows for a better performance.
     */
    public void useOptimizedSelect() {
        Query<Item> items = session.createQuery(Item.ID.whereNeq(0L))
                .setColumn(Item.NOTE) // Select the one column
                .setDistinct()        // Remove duplicate rows
                ;
        for (Item item : items) {
            System.out.println("Note: " + item.getNote());

            // Other columns have got the default value always:
            assert item.getId() == Item.ID.getDefault();
            assert item.getOrder() == Item.ORDER.getDefault();
        }
    }

    /** Select all items with a description with the 'table' insensitive text. */
    public void useNativeCriterion() {

        Criterion<Order> crn = Order.STATE.forSql("ord_order_alias.id>0")
             .and(Order.CREATED.where(LE, new Date()));
        Query<Order> orders = session.createQuery(crn);

        for (Order order : orders) {
            System.out.println("ORDER: " + order);
        }
    }

    /** How to reload the object property values from the database ? */
    public void useReloading() {
        Order order = new Order(1L);

        boolean result = session.reload(order);
        System.out.println("Reloading result: " + result + " for Order: " + order);
    }

    /** How to get the latest order by the LIMIT attribute? */
    public void useLimitAndOffset() {
        Order order = session.createQuery(Order.class)
                .setLimit(1)
                .setOffset(0) // The default value can't be specified
                .orderBy(Order.CREATED.descending())
                .uniqueResult()
                ;
        System.out.println("The latest Order: " + order);
    }

    /** How to count items ? */
    public void useSelectCount() {
        Query<Item> query = session.createQuery(Item.NOTE.where(CONTAINS_CASE_INSENSITIVE, "table"));
        long count = query.getCount();
        System.out.println("Count of the order items: " + count);
    }

    /** How to get a Foreign Key without lazy loading */
    public void useForeignKey() {
        Database db = session.getFirstDatabase();
        for (Item item : db.get(Database.ORDER_ITEMS)) {
            ForeignKey fk1 = item.readFK(Item.ORDER);   // before lazy loading
            item.get(Item.ORDER);                       // the lazy loading
            ForeignKey fk2 = item.readFK(Item.ORDER);   // after lazy loading
            System.out.println("FK: " + fk1 + " " + fk1.equals(fk2));
        }
    }

    /** How to skip items? */
    public void useIteratorSkip() {
        UjoIterator<Item> items = session.createQuery(Item.NOTE.where(NOT_EQ, "XXXXX")).iterator();

        boolean skip = items.skip(1);
        if (items.hasNext()) {
            Item item = items.next();
            System.out.println("Item : " + item);
        }

        skip = items.skip(1);
        boolean isNext = items.hasNext();
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
                String itemDescr = item.getNote();
                System.out.println(" Item id: " + itemId + " note: " + itemDescr);
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
        boolean isProcedureCreated = false; // create the database procedure first
        if ( ! isProcedureCreated ) return;

        MyProcedure procedure = new MyProcedure();

        // Assign input parameters:
        procedure.set(MyProcedure.PARAM_CODE, 5);
        procedure.set(MyProcedure.PARAM_ENABLED, true);
        Integer result = procedure.call(session); // Take the RESULT from the first parameter
        System.out.println("The stored procedure result #1: " + result);

        // Another way how to get the output parameter:
        procedure.set(MyProcedure.PARAM_CODE, 24);
        procedure.set(MyProcedure.RESULT, null); // The output parameter(s) can't be initialized.
        procedure.call(session);
        result = procedure.get(MyProcedure.RESULT); // Take the RESULT from any output parameter
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

    /** Using the database UPDATE */
    public void useUpdate() {
        Order order = session.load(Order.class, 1L);
        order.setCreated(new Date());

        session.update(order);
        session.commit();
    }

    /** The batch UPDATE of selected columns for required database rows. <br />
     * The exsample updates one database column (CREATED) to the current date for all Orders where ID>=1 .
     */
    public void useBatchUpdate() {
        Order order = new Order();
        // Activate the Change column management:
        order.writeSession(session);
        // Set a value(s) to the change:
        order.setCreated(new Date());

        session.update(order, Order.ID.whereGe(1L));
        session.commit();
    }

    /** Using the pesimistic database UPDATE by the method: setLockRequest(). */
    public void usePesimisticUpdate() {
        Order order = session.createQuery(Order.ID.whereEq(1L))
            .setLockRequest()
            .uniqueResult()
            ;
        order.setCreated(new Date());
        session.update(order);
        session.commit();
    }

    /** How to DELETE the one loaded object? */
    public void useDelete() {
        Item item = session.createQuery(Item.class).setLimit(1).uniqueResult();

        session.delete(item);
        session.commit();
        System.out.println("There is DELETED object: " + item);
    }

    /** How to use a batch DELETE? <br/>
     *  The next example deletes all Items where Item.ID = 1
     */
    public void useBatchDelete() {
        int count = session.delete(Item.ID.whereEq(1L));
        session.commit();
        System.out.println("There are DELETED rows: " + count);
    }

    /** Print some meta-data of the property Order.NOTE. */
    public void useMetadata() {
        MetaColumn col = (MetaColumn) handler.findColumnModel(Order.NOTE);

        String msg = "** METADATA OF COLUMN: " + Order.NOTE.toString() + '\n'
            + "DB name: " + col.getFullName()  + '\n'
            + "Comment: " + col.getComment()   + '\n'
            + "Length : " + col.getMaxLength() + '\n'
            + "NotNull: " + col.isMandatory()  + '\n'
            + "Primary: " + col.isPrimaryKey() + '\n'
            + "Dialect: " + col.getDialectName()
            ;
        System.out.println(msg);

        // See, how to get an annotation of a persistent UjoProperty in run-time:
        Comment annotation = handler.findAnnotation(Order.ID, Comment.class);
        System.out.println(annotation.value());
    }

    /** Close Ujorm session to clear a session cache
     * and database connection(s).
     */
    public void useCloseSession() {
        if (session != null) {
            session.close();
        }
    }
}
