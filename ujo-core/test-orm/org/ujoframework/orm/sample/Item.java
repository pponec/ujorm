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

import java.util.Date;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.PathProperty;
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.orm.annot.Column;

/**
 * Each Item have got a reference to a common Order (a sample).
 * @Table=bo_item
 */
public class Item extends OrmTable<Item> {

    /** Unique key */
    @Column(pk=true)
    public static final Property<Item,Long> ID = newProperty("id", Long.class);
    /** User key */
    public static final Property<Item,Integer> USER_ID = newProperty("usrId", Integer.class);
    /** Description of Item */
    public static final Property<Item,String> DESCR = newProperty("description", String.class);
    /** A reference to common Order */
    public static final Property<Item,Order> ORDER = newProperty("order", Order.class);

    // ----------- PATH ------------

    /** A property to an Date of Order */
    public static final UjoProperty<Item,Date> ORDER_DATE = PathProperty.newInstance(ORDER, Order.DATE);


}
