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

package org.ujorm.orm.pojo.orm_tutorial.sample.entity.generated;

import org.ujorm.Key;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.View;
import org.ujorm.extensions.UjoMiddle;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.*;

/**
 * The column mapping to FROM view.
 * @hidden
 */
@View(select="SELECT * FROM ( "
    + "SELECT ord_order_alias.id"
    +         ", count(*) AS item_count"
    + " FROM ${SCHEMA}.ord_order ord_order_alias"
    + " LEFT JOIN ${SCHEMA}.ord_item ord_item_alias"
    + " ON ord_order_alias.id = ord_item_alias.fk_order"
    + " WHERE ord_item_alias.id>=? "
    + " GROUP BY ord_order_alias.id"
    + " ORDER BY ord_order_alias.id"
    + ") testView WHERE true"
    , alias="testView"
    )

//  /* MSSQL query */
//  @View(SELECT * FROM ( "
//  + " SELECT ord_order_alias.id, count(*) AS item_count"
//  + " FROM db1.dbo.ord_order ord_order_alias"
//  +     ", db1.dbo.ord_item  ord_item_alias"
//  + " WHERE ord_order_alias.id>=?
//  +   " AND ord_order_alias.id = ord_item_alias.fk_order"
//  + " GROUP BY ord_order_alias.id"
//  + " ORDER BY ord_order_alias.id")
//  + ") testView WHERE true"
//  , alias="testView"
//  )

 public class $ViewOrder extends OrmTable<$ViewOrder> {

    /** Unique key */
    @Column(pk=true, name="id")
    public static final Key<$ViewOrder,Long> ID = newKey();
    /** ItemCount */
    @Column(name="item_count")
    public static final Key<$ViewOrder,Integer> ITEM_COUNT = newKey(0);

}
