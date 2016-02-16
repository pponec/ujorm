/*
 *  Copyright 2009-2015 Pavel Ponec
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
package org.ujorm.orm_tutorial.sample;

import org.ujorm.orm_tutorial.sample.entity.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.*;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.UjoIterator;
import org.ujorm.criterion.*;
import org.ujorm.orm.*;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.ao.LazyLoading;
import org.ujorm.orm.dialect.DerbyDialect;
import org.ujorm.orm.dialect.FirebirdDialect;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.utility.OrmTools;
import static org.ujorm.Checks.*;
import org.ujorm.core.UjoManager;
import org.ujorm.orm.template.AliasTable;
import static org.ujorm.criterion.Operator.*;
import static org.ujorm.orm.template.AliasTable.Build.*;

/**
 * The tutorial in the class for the Ujorm <br/>
 * --------------------------------------- <br/>
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
            sample.useSelectWithAliasTable();
            sample.useSelectItems_1();
            sample.useSelectItems_2();
            sample.useSelectItems_3();
            sample.useSelectItems_4();
            sample.useSelectItems_5();
            sample.useSelectItems_5b();
            sample.useSelectItems_6();
            sample.useSelectItems_7();
            sample.useHierarchicalQuery();
            sample.useHierarchicalQuerySimple();
            sample.useHierarchicalQueryMore();
            sample.useOptimizedSelect();
            sample.useOneRequestLoading();
            sample.useNativeCriterion();
            sample.useReloading();
            sample.useLazyLoadingOnClosedSession();
            sample.useLimitAndOffset();
            sample.useSelectCount();
            sample.useForeignKey();
            sample.useIteratorSkip();
            sample.useRelation();
            sample.useStoredProcedure();
            sample.useUpdate();
            sample.useBatchUpdate();
            sample.useExtendedUpdate();
            sample.usePesimisticUpdate();
            sample.useDelete();
            sample.useBatchDelete();
            sample.useExtendedDelete();
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
    /** Temporary field */
    private Long anyOrderId;

    /** Before the first: create a meta-model.
     * Database tables will be CREATED in the first time.
     */
    public void createMetaModel() {

        // Set the log level specifying which message levels will be logged by Ujorm:
        Logger.getLogger(Ujo.class.getPackage().getName()).setLevel(Level.INFO);

        // Create new ORM Handler:
        handler = new OrmHandler();

        // There are prefered default keys for a production environment:
        boolean yesIWantToChangeDefaultParameters = true;
        if (yesIWantToChangeDefaultParameters) {
            MetaParams params = new MetaParams();
            params.set(MetaParams.SEQUENCE_SCHEMA_SYMBOL, true);
            params.set(MetaParams.TABLE_ALIAS_SUFFIX, "_alias");
            params.setQuotedSqlNames(false);
            params.set(MetaParams.INITIALIZATION_BATCH, new InitializationBatch() {
                @Override public void run(Session session) throws Exception {
                    if (!session.exists(Customer.class)) {
                        System.out.println("Create some customers, if you can.");
                    }
                }
            });
            handler.config(params);
        }

        // Do jou need to load an external confuguration from XML?
        boolean yesIWantToLoadExternalConfig = false;
        if (yesIWantToLoadExternalConfig) {
            java.net.URL config = getClass().getResource("config.xml");
            handler.config(config, true);
        }

        // Load Meta-model and lock it to a read-only mode:
        handler.loadDatabase(Database.class);

        // Open an ORM session (which is no thread safe):
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

        final Transaction tr = session.beginTransaction();
        session.save(order);
        session.save(item1);
        session.save(item2);

        for (int i = 0; i < 10; i++) {
            Item item3 = new Item();
            item3.setOrder(order);
            item3.setNote("Green window " + i);
            session.save(item3);
        }

        if (true) {
            tr.commit();
        } else {
            tr.rollback();
        }
        // Save the identifier:
        anyOrderId = order.getId();
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
            .and( Item.ORDER.add(Order.NOTE).whereEq( "My order" ));

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
        crn = crnCreated.or(crnNote).and(crnId);
        assert crn.evaluate(order);
    }

    /** Sort orders by two keys: NOTE and CREATED descending. */
    public void useSortOrders() {

        Query<Order> orders = session.createQuery(Order.class);
        orders.orderBy( Order.NOTE
                      , Order.CREATED.descending() );

        logInfo("View-order count: %s", orders.getCount());
    }

    /** Sort items by a <strong>composite</strong> property. <br>
     * Note 1: see how a composite key can be used for reading values too. <br>
     * Note 2: the method loadLazyValues(..) is able to load all lazy keys for the Item and its related Order<br>
     */
    public void useSortOrderItems() {

        Query<Item> items = session.createQuery(Item.class);
        items.orderBy(Item.ORDER.add(Order.CREATED));

        for (Item item : items) {
            OrmTools.loadLazyValues(item, 2);
            logInfo("Created: %s of %s", item.get(Item.ORDER.add(Order.CREATED)), item);
        }
        // Another way to avoid the lazy loading by a bulk key loading:
        List<Item> itemList = OrmTools.loadLazyValuesAsBatch(items);
        logInfo("ItemList: %s", itemList);
    }

    /** Use a 'native query' where the query is CREATED
     * by a special entity signed by the @View annotation. <br/>
     * Note the special <strong>inner parameter</strong> in the SQL statement on the Annotation of the class ViewOrder,
     * where value for this (optional) parameter is set by the method Query.setSqlParameters();
     * @see Query#setSqlParameters(java.lang.Object[])
     */
    public void useSelectViewOrders() {
        // Some dialects must have got special SQL statements:
        if (session.hasDialect(ViewOrder.class, DerbyDialect.class, FirebirdDialect.class)
        ||  session.getParameters().isQuotedSqlNames()){ // Columns must be quoted
            return;
        }

        Criterion<ViewOrder> crit = ViewOrder.ITEM_COUNT.whereGt(0);

        long minimalOrderId = 0L;
        long orderCount = session.createQuery(crit)
                .setSqlParameters(minimalOrderId)
                .getCount()
                ;
        logInfo("Order count: %s", orderCount);

        Query<ViewOrder> orders = session.createQuery(crit)
                .setLimit(5)
                .orderBy(ViewOrder.ID)
                .setSqlParameters(0)
                ;
        for (ViewOrder order : orders) {
            logInfo("Order row: %s", order);
        }
    }

    /** Use a 'native query' where the query is CREATED
     * by a special entity signed by the @View annotation. <br/>
     * Note the special <strong>inner parameter</strong> in the SQL statement on the Annotation of the class ViewOrder,
     * where value for this (optional) parameter is set by the method Query.setSqlParameters();
     * @see Query#setSqlParameters(java.lang.Object[])
     */
    public void useSelectWithNativeSQL() {
        // Some dialects must have got special SQL statements:
        if (session.hasDialect(ViewOrder.class, DerbyDialect.class, FirebirdDialect.class)
        ||  session.getParameters().isQuotedSqlNames()){ // Columns must be quoted
            return;
        }

        final Long excludedId = -7L;
        SqlParameters sql = new SqlParameters().setSqlStatement
                ( "SELECT * FROM ("
                + "SELECT ord_order_alias.id"
                + ", count(*) AS item_count"
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
                .getCount();

        logInfo("Order Count: %s", orderCount);

        Query<ViewOrder> orders = session.createQuery(crit)
                .setLimit(5)
                .orderBy(ViewOrder.ID)
                .setSqlParameters(sql)
                ;
        for (ViewOrder order : orders) {
            logInfo("Order row: %s", order);
        }
    }

    /** Use a 'native query' build by the AliasTable object */
    @SuppressWarnings("unchecked")
    public void useSelectWithAliasTable() {
        final Long excludedId = -7L;
        final AliasTable order = handler.tableOf(ViewOrder.class, "o");
        final AliasTable item = handler.tableOf(Item.class, "i");

        String expected = "SELECT o.ID"
                + ", COUNT(*) AS ITEM_COUNT"
                + " FROM db1.ord_order o"
                + " INNER JOIN db1.ord_item i ON i.fk_order = o.ID "
                + " WHERE o.ID!=?"
                + " GROUP BY o.ID"
                + " ORDER BY o.ID";
        String innerSql = SELECT(order.column(Order.ID)
                , order.columnAs("COUNT(*)", ViewOrder.ITEM_COUNT))
                + FROM (order)
                + INNER_JOIN(item, item.column(Item.ORDER), "=", order.column(Order.ID))
                + WHERE(order.column(Order.ID), "!=", PARAM)
                + GROUP_BY(order.column(Order.ID))
                + ORDER_BY(order.column(Order.ID));

        assert expected.equals(innerSql);
        String sql = SELECT("*") + FROM("(" + innerSql + ")") + "  testView " + WHERE("true");

        SqlParameters sqlParam = new SqlParameters().setSqlStatement(sql).setParameters(excludedId);
        Criterion<ViewOrder> crit = ViewOrder.ITEM_COUNT.whereLe(100);
        long orderCount = session.createQuery(crit)
                .setSqlParameters(sqlParam)
                .getCount();
        logInfo("Order Count: %s", orderCount);

        Query<ViewOrder> orders = session.createQuery(crit)
                .setLimit(5)
                .orderBy(ViewOrder.ID)
                .setSqlParameters(sqlParam)
                ;
        for (ViewOrder viewOrder : orders) {
            logInfo("Order row: %s", viewOrder);
        }
    }

    /** Select all ITEMS with a description with the 'table' insensitive text. */
    public void useSelectItems_1() {

        Criterion<Item> crit = Item.NOTE.where(CONTAINS_CASE_INSENSITIVE, "table");
        Query<Item> items = session.createQuery(crit).orderBy(Item.ID.descending());

        for (Item item : items) {
            Order order = item.getOrder();
            logInfo("Item row: %s of the Order: %s", item, order);
        }
    }

    /** Select one Order by ID and print its Items by a criterion */
    public void useSelectItems_2() {

        Order orderValue = session.load(Order.class, anyOrderId);
        Query<Item> items = session.createQuery(Item.ORDER.whereEq(orderValue));

        for (Item item : items) {
            Order order2 = item.getOrder();
            logInfo("item row: %s of the Order: %s", item, order2);
        }
    }

    /** Select an Order by ID and print its Items
     * by a 'one to many' relation key
     */
    public void useSelectItems_3() {
        Order order = session.load(Order.class, anyOrderId);

        for (Item item : order.getItems()) {
            Order order2 = item.getOrder();
            logInfo("Item row: %s of the Order: %s", item, order2);
        }
    }

    /** Select items by a composed key.
     * It is a sample of a multi-table query.
     * @see Item#$ORDER_CREATED
     */
    public void useSelectItems_4() {
        Key<Item, Date> ORDER_DATE = Item.ORDER.add(Order.CREATED); // or use: Item.$ORDER_CREATED
        Query<Item> items = session.createQuery(ORDER_DATE.whereLe(new Date()));

        for (Item item : items) {
            logInfo("Item: %s", item);
        }
    }

    /** Select items by a composed key.
     * It is a sample of a multi-table query.
     * See used Criterion with the whereIn method. The value list can be empty and the RESULT returns FALSE always in this case.
     * @see Item#$ORDER_CREATED
     */
    public void useSelectItems_5() {
        Query<Item> items = session.createQuery(Item.ID.whereIn(1L, 2L, 3L, 4L, 5L));

        for (Item item : items) {
            logInfo("Item: %s", item);
        }
    }

    /** Select using the IN operator with persistent objects. */
    public void useSelectItems_5b() {
        Order orderA = new Order(1L);
        Order orderB = new Order(2L);

        for (Item item : session.createQuery(Item.ORDER.whereIn(orderA, orderB))) {
            logInfo("Item: %s", item);
        }

        // --- Or dirty hack using identifiers directly ---

        Collection ids = Arrays.asList(1L, 2L);
        Criterion<Item> crn = Item.ORDER.whereIn(ids);
        for (Item item : session.createQuery(crn)) {
            logInfo("Item: %s", item);
        }

    }

    /** Select one items without Order */
    public void useSelectItems_6() {
        Query<Item> items = session.createQuery(Item.ORDER.add(Order.CUSTOMER).whereNull());
        for (Item item : items) {
            logInfo("Item without order: %s", item);
        }
    }

    /** Using IN phrase for a list of Ujo objects. */
    public void useSelectItems_7() {
        List<Order> orders = session.createQuery(Order.ID.forAll()).setLimit(1).list();
        List<Item> items = session.createQuery(Item.ORDER.whereIn(orders)).list();
        assert items.size() > 0 : "The result have got two Items";
    }

    /** Sample for a DB query with relations to yourself.<br>
     * All relations of the same entity must be marked by an unique alias name.
     * @see Key#alias(java.lang.String)
     */
    public void useHierarchicalQuery() {
        Criterion<Customer> crn1, crn2, crn3;
        crn1 = Customer.PARENT.alias("parent1")
          .add(Customer.PARENT).alias("parent2")
          .add(Customer.SURNAME)
          .whereEq("Smith");
        crn2 = Customer.SURNAME.whereEq("Brown");
        crn3 = crn1.and(crn2);

        createHierarchicalCustomers("Smith", "Brown");
        Customer customer = session.createQuery(crn3).uniqueResult();

        assert customer != null : "The result have got the one customers";
        assert Customer.SURNAME.equals(customer, "Brown") : "Wrong customer";
    }

    /**
     * TODO: fix it<br/>
     * Simple hierarchical Query (to yourself) for the special case<br>
     * where the relation {@link Customer#PARENT PARENT} is created by method {@link KeyFactory#newKeyAlias(java.lang.String)}
     * and the request have got the <strong>first level</strong> of hierarchical relations only.
     * @see org.ujorm.core.KeyFactory#newKeyAlias(java.lang.String)
     */
    public void useHierarchicalQuerySimple() {
        Key<Customer, String> parentName = Customer.PARENT.add(Customer.SURNAME);
        Customer customer = session.createQuery(parentName.whereEq("Smith"))
                .addColumn(parentName)
                .orderBy(parentName)
                .uniqueResult();

        assert customer != null : "The result have got the one customers";
        assert Customer.PARENT instanceof CompositeKey : "The key is type of CompositeKey" + Customer.PARENT.getClass();
        assert parentName.getFullName().equals("Customer.parent[customerAlias].surname") : "The wrong implementation CompositeKey.toString()";
        assert "Smith".equals(customer.get(parentName));
    }

    /** DB query with relations to yourself as a value of Criterion */
    public void useHierarchicalQueryMore() {
        Criterion<Customer> crn1;
        crn1 = Customer.FIRSTNAME.whereEq(
               Customer.PARENT.alias("parent1")
               .add(Customer.PARENT).alias("parent2")
               .add(Customer.SURNAME));

        Customer customer = session.createQuery(crn1).uniqueResult();
        assert customer != null : "The result have got the one customers";
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
            logInfo("Note: %s", item.getNote());

            // Other columns have got the default value always:
            assert item.getId() == Item.ID.getDefault();
            assert item.getOrder() == Item.ORDER.getDefault();
        }
    }

    /** Fetch column from related tables */
    public void useOneRequestLoading() {
        Query<Item> items = session.createQuery(Item.ID.whereNeq(0L));
        Key<Item, Date> orderCreated = Item.ORDER.add(Order.CREATED);

        // Fetch the Order's CREATED column (and the primary key):
        items.setColumns(true, orderCreated);
        for (Item item : items.list()) {
            expectNull(false, item.getId()); // due the request: addPrimaryKey
            expectNull(false, item.get(orderCreated));
            expectNull(false, item.get(Item.ORDER));
            // checkNull(false , item.get(Item.ORDER.add(Order.ID))); // TODO FixIT (?)
            expectNull(true , item.get(Item.NOTE));
            expectNull(true , item.get(Item.ORDER.add(Order.NOTE))); // Eeach lazy Order has a not-null NOTE!
        }

        // Fetch all the Order columns:
        items.setColumns(true, Item.ORDER);
        for (Item item : items.list()) {
            expectNull(false, item.getId()); // due the request: addPrimaryKey
            expectNull(true , item.get(Item.NOTE));
            expectNull(false, item.get(Item.ORDER));
            expectNull(false, item.get(orderCreated));
            expectNull(false, item.get(Item.ORDER.add(Order.ID)));
            expectNull(false, item.get(Item.ORDER.add(Order.NOTE)));
        }
    }

    /** Select orders using a native criterion */
    public void useNativeCriterion() {
        // The base using: the first arguments is replaced by column, the second is replaced using argument.
        Criterion<Order> crn = Order.ID.forSql("{0} > {1}", 0L)
                .and(Order.CREATED.where(LE, new Date()));
        Order.ID.forSql("{0} > {1}", 1L).getRightNode();
        Query<Order> orders = session.createQuery(crn);

        for (Order order : orders) {
            logInfo("Order: %s", order);
        }

        // Special using without arguments:
        if (session.getParameters().isQuotedSqlNames()) {
            return;  // Columns must be quoted
        }

        crn = Order.STATE.forSql("ord_order_alias.id > 0")
             .and(Order.CREATED.where(LE, new Date()));
        orders = session.createQuery(crn);

        for (Order order : orders) {
            logInfo("Order: %s", order);
        }
    }

    /** How to reload the object key values from the database ? */
    public void useReloading() {
        Order order = new Order(anyOrderId);
        boolean result = session.reload(order);
        logInfo("Reloading result: %s for Order: %s", result, order);
    }

    /** How to reload the object key values from the database ? */
    public void useLazyLoadingOnClosedSession() {
        Item item = handler.createSession().createQuery(Item.ID.forAll())
                .orderBy(Item.ID)
                .setLimit(1)
                .uniqueResult();
        item.readSession().close();

        try {
            Order order2 = item.getOrder();
            assert false : "Lazy-loading for a closed session is disabled by default, the Item is: " + order2.getId();
        } catch (IllegalStateException e) {
            logInfo("OK: %s", e.getClass().getSimpleName());
        }

        item.readSession().setLazyLoading(LazyLoading.ALLOWED_ANYWHERE_WITH_WARNING); // Enable lazy-loading
        Order order3 = item.getOrder();
        Item item4 = order3.getItems().next(); // Lazy loading type of one to many
        logInfo("Lazy Order: %s and Item: %s", order3, item4);
    }

    /** How to get the latest order by the LIMIT attribute? */
    public void useLimitAndOffset() {
        Order order = session.createQuery(Order.class)
                .setLimit(1)
                .setOffset(0) // The default value can't be specified
                .orderBy(Order.CREATED.descending())
                .uniqueResult()
                ;
        logInfo("The latest Order: %s", order);
    }

    /** How to count items ? */
    public void useSelectCount() {
        Query<Item> query = session.createQuery(Item.NOTE.where(CONTAINS_CASE_INSENSITIVE, "table"));
        long count = query.getCount();
        logInfo("Count of the order items: %s", count);
    }

    /** How to get a Foreign Key without lazy loading */
    public void useForeignKey() {
        Database db = session.getFirstDatabase();
        for (Item item : db.get(Database.ORDER_ITEMS)) {
            ForeignKey fk1 = item.readFK(Item.ORDER);   // before lazy loading
            item.get(Item.ORDER);                       // the lazy loading
            ForeignKey fk2 = item.readFK(Item.ORDER);   // after lazy loading
            logInfo("FK1: %s where FK1 eq FK2: %s", fk1, fk1.equals(fk2));
        }
    }

    /** How to skip items? */
    public void useIteratorSkip() {
        UjoIterator<Item> items = session.createQuery(Item.NOTE.where(NOT_EQ, "XXXXX")).iterator();

        boolean skip = items.skip(1);
        if (items.hasNext()) {
            Item item = items.next();
            logInfo("Item: %s", item);
        }

        skip = items.skip(1);
        boolean isNext = items.hasNext();
        logInfo("Next: %s", isNext);
    }

    /** Sample for 'one to many' relation.
     * Note that it is possible to use a Database configuration object too.
     */
    public void useRelation() {
        Database db = session.getDatabase(Database.class);

        UjoIterator<Order> orders = db.get(Database.ORDERS);
        for (Order order : orders) {
            String note = order.getNote();
            logInfo("Order: %s with Note: %s", order, note);

            for (Item item : order.getItems()) {
                Long itemId = item.getId();
                String itemDescr = item.getNote();
                logInfo(" Item id: %s with Note: %s", itemId, itemDescr);
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
        logInfo("The stored procedure result #1: %s", result);

        // Another way how to get the output parameter:
        procedure.set(MyProcedure.PARAM_CODE, 24);
        procedure.set(MyProcedure.RESULT, null); // The output parameter(s) can't be initialized.
        procedure.call(session);
        result = procedure.get(MyProcedure.RESULT); // Take the RESULT from any output parameter
        logInfo("The stored procedure result #2: %s", result);
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
        logInfo("The stored procedure result #1: %s", result);

        // See how to reuse input parameters of the object 'procedure':
        procedure.set(MyProcedure.PARAM_CODE, 24);
        result = procedure.call(session, MyProcedure.RESULT); // Take the RESULT of any (output) parameter
        logInfo("The stored procedure result #2: %s", result);
    }

    /** Using the database UPDATE */
    public void useUpdate() {
        Order order = session.load(Order.class, anyOrderId);
        order.setCreated(new Date());

        session.update(order);
        session.commit();
    }

    /** The batch UPDATE of selected columns for required database rows. <br />
     * The example updates one database column (CREATED) to the current date for all Orders where ID>=1 .
     */
    public void useBatchUpdate() {
        Order order = new Order();
        // Activate the Change column management:
        order.writeSession(session);
        // Set a value(s) to the change:
        order.setCreated(new Date());

        session.update(order, Order.ID.whereGe(anyOrderId));
        session.commit();
    }

    /** The batch UPDATE of selected columns for required database rows for an extended condition.<br />
     * See the next example:
     */
    public void useExtendedUpdate() {
        Order order = new Order();
        // Activate the Change column management:
        order.writeSession(session);
        // Set a value(s) to the change:
        order.setCreated(new Date());

        Criterion<Item> crn = Item.ID.whereGt(0L)
                .and(Item.ORDER.add(Order.NOTE).whereNull());
        session.update(order, crn);
        session.commit();
    }

    /** Using the pessimistic database UPDATE by the method: setLockRequest(). */
    public void usePesimisticUpdate() {
        Order order = session.createQuery(Order.ID.whereEq(anyOrderId))
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
        logInfo("There is DELETED object:  %s", item);
    }

    /** How to use a batch DELETE? <br/>
     * The next example deletes all Items where Item.ID = 1
     */
    public void useBatchDelete() {
        int count = session.delete(Item.ID.whereEq(anyOrderId));
        session.commit();
        logInfo("There are DELETED rows:  %s", count);
    }

    /** How to use a batch DELETE for an extended conditon? <br/>
     * See the next example:
     */
    public void useExtendedDelete() {
        Criterion<Item> crn = Item.ID.whereGt(0L)
                .and(Item.ORDER.add(Order.NOTE).whereNull());
        int count = session.delete(crn);
        session.commit();
        logInfo("There are DELETED rows:  %s", count);
    }

    /** Print some meta-data of the key Order.NOTE. */
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
        logInfo(msg);

        // See, how to get an annotation of an Key in run-time:
        Comment annotation = UjoManager.findAnnotation(Order.ID, Comment.class);
        logInfo(annotation.value());
    }

    /** Close Ujorm session to clear a session cache
     * and database connection(s).
     */
    public void useCloseSession() {
        if (session != null) {
            session.close();
        }
    }

    /** Log the information with the {code Level.INFO} */
    private void logInfo(String message, Object... args) {
        final Logger logger = Logger.getLogger(SampleORM.class.getName());
        if (logger.isLoggable(Level.INFO)
        &&  args.length > 0) {
            message = String.format(message, args);
        }
        logger.info(message);
    }

    /** Create the hierarchical data */
    private void createHierarchicalCustomers(String superName, String currentName) {
        if (!session.exists(Customer.SURNAME.whereEq(superName))) {
            Customer c2 = new Customer();
            Customer.SURNAME.setValue(c2, superName);
            Customer.FIRSTNAME.setValue(c2, "John");
            Customer.PARENT.setValue(c2, null);
            //
            Customer c1 = new Customer();
            Customer.SURNAME.setValue(c1, superName + "-" + currentName);
            Customer.FIRSTNAME.setValue(c1, "Jack");
            Customer.PARENT.setValue(c1, c2);
            //
            Customer c0 = new Customer();
            Customer.SURNAME.setValue(c0, currentName);
            Customer.FIRSTNAME.setValue(c0, superName);
            Customer.PARENT.setValue(c0, c1);
            //
            session.save(c2);
            session.save(c1);
            session.save(c0);
        }
    }
}