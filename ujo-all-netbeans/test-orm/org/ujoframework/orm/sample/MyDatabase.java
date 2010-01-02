/*
 *  Copyright 2009-2010 Pavel Ponec
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
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.orm.dialect.H2Dialect;

/**
 * An table definition of the one database (a sample).
 */
@Db( dialect=H2Dialect.class, user="sa", password="")
public class MyDatabase extends OrmTable<MyDatabase> {

    /** Customer order. The used annotation overwrites a database schema from the property schema. */
    @Table(name="ORD_ORDER_NEW")
    public static final RelationToMany<MyDatabase,Order> ORDERS = newRelation("ORD_ORDER", Order.class);

    /** Items of the Customer order */
    public static final RelationToMany<MyDatabase,Item> ORDER_ITEMS = newRelation("ORD_ITEMS", Item.class);



}
