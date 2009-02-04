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

package org.ujoframework.core.orm.sample;

import org.ujoframework.UjoProperty;
import org.ujoframework.core.orm.annot.Column;
import org.ujoframework.implementation.db.TableUjo;

/**
 * Each Item have got a reference to a common Order (a sample).
 * @Table=bo_item
 */
public class BoItem extends TableUjo {

    /** Unique key */
    @Column(pk=true)
    public static final UjoProperty<BoItem,Long> ID = newProperty("id", Long.class);
    /** User key */
    public static final UjoProperty<BoItem,Integer> USER_ID = newProperty("usrId", Integer.class);
    /** Description of Item */
    public static final UjoProperty<BoItem,String> DESCR = newProperty("description", String.class);
    /** A reference to common Order */
    public static final UjoProperty<BoItem,BoOrder> ORDER = newProperty("order", BoOrder.class);


}
