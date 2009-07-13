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

package benchmark;

import org.ujoframework.orm.annot.Db;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.implementation.orm.RelationToMany;
import benchmark.bo.*;


/**
 * An table mapping to a database (a sample of usage).
 * @hidden
 */
@Db(schema="ujorm", dialect=org.ujoframework.orm.dialect.PostgreSqlDialect.class, user="sa", password="sa", jdbcUrl="jdbc:postgresql://127.0.0.1:5432/benchmark")
//@Db(schema="db1", dialect=org.ujoframework.orm.dialect.H2Dialect.class, user="sa", password="", jdbcUrl="jdbc:h2:mem:db1")
public class Database extends OrmTable<Database> {

    /** Customer order. The used annotation overwrites a database schema from the property schema. */
    public static final RelationToMany<Database,PrfOrder> Order = newRelation("PrfOrder", PrfOrder.class);

    /** Items of the Customer order */
    public static final RelationToMany<Database,PrfOrderItem> OrderItem = newRelation("PrfOrderItem", PrfOrderItem.class);

    /** View to aggregate data. */
    public static final RelationToMany<Database,PrfUser> User = newRelation("PrfUser", PrfUser.class);


}
