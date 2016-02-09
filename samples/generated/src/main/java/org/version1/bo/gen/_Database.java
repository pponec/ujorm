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

package org.version1.bo.gen;

import org.version1.Database;
import org.version1.bo.Item;
import org.version1.bo.MyProcedure;
import org.version1.bo.Order;
import org.version1.bo.ViewOrder;
import org.ujorm.Key;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.annot.Procedure;
import org.ujorm.orm.annot.Table;
import org.ujorm.orm.annot.View;

abstract public class _Database extends OrmTable<_Database> {

    /** Customer order. The used annotation overwrites a database schema from the key schema. */
    @Table("ord_order")
    public static final RelationToMany<Database,Order> ORDERS = newRelation(Order.class);

    /** Items of the Customer order */
    @Table("ord_item")
    public static final RelationToMany<Database,Item> ORDER_ITEMS = newRelation(Item.class);

    /** View to aggregate data. */
    @View("ord_order")
    public static final RelationToMany<Database,ViewOrder> VIEW_ORDERS = newRelation(ViewOrder.class);

    // ----- STORED PROCEDURE(S) --------

    /** Database stored procedure */
    @Procedure("ujorm_test")
    public static final Key<Database,MyProcedure> myProcedure = newProperty(MyProcedure.class);

}
