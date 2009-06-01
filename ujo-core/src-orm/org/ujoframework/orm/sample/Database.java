/*
 *  Copyright 2009 Paul Ponec
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

package org.ujoframework.orm.sample;

import org.ujoframework.orm.annot.Db;
import org.ujoframework.orm.annot.Table;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.orm.annot.View;

/**
 * An table mapping to a database (a sample of usage).
 * @hidden
 */
//@Db(schema="db1", dialect=org.ujoframework.orm.dialect.H2Dialect.class, user="sa", password="", jdbcUrl="jdbc:h2:mem:db1")
@Db(schema="db1", dialect=org.ujoframework.orm.dialect.DerbyDialect.class, user="sa", password="", jdbcUrl="jdbc:derby:C:\\temp\\derby-sample;create=true")
//@Db(schema="db1", dialect=org.ujoframework.orm.dialect.PostgreSqlDialect.class, user="sa", password="sa", jdbcUrl="jdbc:postgresql://127.0.0.1:5432/db1")
public class Database extends TableUjo<Database> {

    /** Customer order. The used annotation overwrites a database schema from the property schema. */
    @Table(name="ord_order")
    public static final RelationToMany<Database,Order> ORDERS = newRelation("ord_order", Order.class);

    /** Items of the Customer order */
    public static final RelationToMany<Database,Item> ORDER_ITEMS = newRelation("ord_item", Item.class);

    /** View to aggregate data. */
    @View(name="ord_order")
    public static final RelationToMany<Database,ViewOrder> VIEW_ORDERS = newRelation("view_order", ViewOrder.class);


}
