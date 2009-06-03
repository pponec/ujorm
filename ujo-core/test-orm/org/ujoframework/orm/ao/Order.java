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

package org.ujoframework.orm.ao;

import java.util.Date;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.orm.annot.Table;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 */
@Table(name="bo_order_01")
public class Order extends TableUjo<Order> {

    /** Unique key */
    @Column(pk=true)
    public static final UjoProperty<Order,Long> ID = newProperty("id", Long.class);
    /** User key */
    @Column(pk=true)
    public static final UjoProperty<Order,Integer> USER_ID = newProperty("usrId", Integer.class);
    /** Date of creation */
    @Column(pk=true)
    public static final UjoProperty<Order,Date> DATE = newProperty("date", Date.class);
    /** Description of the order */
    public static final UjoProperty<Order,String> DESCR = newProperty("description", String.class);
    

    // --- An optional property unique name test ---
    static { UjoManager.checkUniqueProperties(Order.class); }


}
