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

package org.ujoframework.orm_tutorial.sample;

import org.ujoframework.UjoProperty;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.orm.annot.View;


/**
 * The column mapping to FROM view.
 * @hidden
 */

@View(select="SELECT ord_order_alias.id, count(*) AS item_count"
    + " FROM db1.ord_order ord_order_alias"
    +     ", db1.ord_item  ord_item_alias"
    + " WHERE ord_order_alias.id = ord_item_alias.fk_order"
    + " GROUP BY ord_order_alias.id"
    + " ORDER BY ord_order_alias.id")
  
    /* MSSQL query*/
/*    @View(select="SELECT ord_order_alias.id, count(*) AS item_count"
    + " FROM db1.dbo.ord_order ord_order_alias"
    +     ", db1.dbo.ord_item  ord_item_alias"
    + " WHERE ord_order_alias.id = ord_item_alias.fk_order"
    + " GROUP BY ord_order_alias.id"
    + " ORDER BY ord_order_alias.id")
*/
 public class ViewOrder extends OrmTable<ViewOrder> {

    /** Unique key */
    @Column(pk=true)
    public static final UjoProperty<ViewOrder,Long> ID = newProperty(Long.class);
    /** ItemCount */
    public static final UjoProperty<ViewOrder,Integer> ITEM_COUNT = newProperty(0);

}
