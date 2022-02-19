/*
 *  Copyright 2020-2022 Pavel Ponec
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

import benchmark.bo.*;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.annot.Db;


/**
 * An table mapping to a database (a sample of usage).
 * @hidden
 */
@Db(schema="bmk", dialect=org.ujorm.orm.dialect.PostgreSqlDialect.class, user="sa", password="sa", jdbcUrl="jdbc:postgresql://127.0.0.1:5432/benchmark")
//@Db(schema="db1", dialect=org.ujorm.orm.dialect.H2Dialect.class, user="sa", password="", jdbcUrl="jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1")
public class Database extends OrmTable<Database> {

    /** Customer ORDER. The used annotation overwrites a database schema from the key schema. */
    public static final RelationToMany<Database,UjoOrder> ORDER = newRelation("ujo_order");

    /** Items of the Customer ORDER */
    public static final RelationToMany<Database,UjoOrderItem> ORDER_ITEM = newRelation("ujo_item");

    /** View to aggregate data. */
    public static final RelationToMany<Database,UjoUser> USER = newRelation("ujo_user");


}
