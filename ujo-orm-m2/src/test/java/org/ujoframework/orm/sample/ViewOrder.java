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

import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.orm.annot.Column;

/**
 * Simple order for goods with a collection of Items (a sample).
 * @Table=bo_order
 */
public class ViewOrder extends OrmTable<ViewOrder> {

    /** Unique key */
    @Column(pk=true)
    public static final Property<ViewOrder,Long> ID = newProperty("id", Long.class);
    /** User key */
    public static final Property<ViewOrder,Integer> USER_ID = newProperty("usrId", Integer.class);
    /** ItemCount */
    public static final Property<ViewOrder,Integer> ITEM_COUNT = newProperty("usrId", 0);

}
