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

package org.ujorm.orm.pojo.orm_tutorial.sample.entity;

import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.View;

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

 public class ViewOrder {

    /** Unique key */
    @Column(pk=true, name="id")
    private Long id;
    /** ItemCount */
    @Column(name="item_count")
    private Integer itemCount = 0;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }
    
}
