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

import org.ujoframework.core.orm.annot.Database;
import org.ujoframework.core.orm.annot.Table;
import org.ujoframework.implementation.db.TableUjo;
import org.ujoframework.implementation.db.RelationToMany;

/**
 * An table definition of the one database (a sample).
 */
@Database(jdbc="jdbc://connection")
public class BoDatabase extends TableUjo {

    /** Customer order. The used annotation overwrites a database name from the property name. */
    @Table(name="ORD_ORDER_NEW")
    public static final RelationToMany<BoDatabase,BoOrder> ORDERS = newRelation("ORD_ORDER", BoOrder.class);

    /** Items of the Customer order */
    public static final RelationToMany<BoDatabase,BoItem> ORDER_ITEMS = newRelation("ORD_ITEMS", BoItem.class);



}
