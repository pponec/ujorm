/*
 * Copyright 2013 ponec.
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
package org.ujorm.orm.template;

import org.ujorm.orm_tutorial.sample.entity.Order;
import org.ujorm.orm_tutorial.sample.entity.Database;
import org.ujorm.orm_tutorial.sample.entity.Item;
import java.util.logging.*;
import junit.framework.TestCase;
import org.ujorm.Ujo;
import org.ujorm.logger.UjoLogger;
import org.ujorm.orm.*;
import org.ujorm.orm.ao.CachePolicy;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm_tutorial.sample.*;
import static org.ujorm.orm.template.AliasTable.Build.*;

/**
 * AliasTableTest
 * @author Pavel Ponec
 */
public class AliasTableTest extends TestCase {

    public AliasTableTest(String testName) {
        super(testName);
    }

    /** Test of getTableModel method, of class AliasTable. */
    public void testAlias_1() {
        System.out.println("testAlias_1");
        OrmHandler handler = createHandler();

        AliasTable<Order> order = handler.tableOf(Order.class, "order");
        AliasTable<Item> item = handler.tableOf(Item.class, "item");

        String sqlExpected = "SELECT order.CREATED, item.NOTE "
                + "FROM db1.ord_order order, db1.ord_item item "
                + "WHERE order.ID = item.fk_order";
        String sql
                = SELECT(order.column(Order.CREATED), item.column(Item.NOTE) )
                + FROM (order, item)
                + WHERE(order.column(Order.ID), " = ", item.column(Item.ORDER));
        System.out.println("sql: " + sql);
        assertEquals(sqlExpected, sql);
    }

    /** Test of getTableModel method, of class AliasTable. */
    public void testAlias_2() {
        System.out.println("testAlias_2");
        OrmHandler handler = createHandler();

        AliasTable<Order> order = handler.tableOf(Order.class, "a");
        AliasTable<Item> item = handler.tableOf(Item.class, "b");

        String sqlExpected = "SELECT a.CREATED AS CREATED, b.NOTE AS NOTE "
                + "FROM db1.ord_order a, db1.ord_item b "
                + "WHERE b.fk_order = a.ID";
        String sql = "SELECT " + order.columnAs(Order.CREATED) + ", " + item.columnAs(Item.NOTE)
                + " FROM " + order + ", " + item
                + " WHERE " + item.column(Item.ORDER) + " = " + order.column(Order.ID);
        System.out.println("sql: " + sql);
        assertEquals(sqlExpected, sql);
    }

    // ------------------------ TOOLS ---------------------------------

    /** Before the first: create a meta-model.
     * Database tables will be CREATED in the first time.
     */
    private OrmHandler createHandler() {

        // Set the log level specifying which message levels will be logged by Ujorm:
        Logger.getLogger(Ujo.class.getPackage().getName()).setLevel(UjoLogger.DEBUG);

        // Create new ORM Handler:
        OrmHandler handler = new OrmHandler();

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
        return handler;
    }

}
