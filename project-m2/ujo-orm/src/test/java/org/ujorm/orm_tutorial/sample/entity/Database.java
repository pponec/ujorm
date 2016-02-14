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

package org.ujorm.orm_tutorial.sample.entity;

import org.ujorm.Key;
import org.ujorm.orm.annot.Table;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.annot.Db;
import org.ujorm.orm.annot.Procedure;
import org.ujorm.orm.annot.View;
import org.ujorm.orm.dialect.*;

/**
 * A class mapping to a database (sample of usage)
 * @hidden
 */
@Db(schema="db1", dialect=H2Dialect.class, user="sa", password="", jdbcUrl="jdbc:h2:mem:db1")
//@Db(schema="db1", dialect=H2Dialect.class, user="sa", password="", jdbcUrl="jdbc:h2:~/ujorm/db1")
//@Db(schema="db1", dialect=PostgreSqlDialect.class, user="sa", password="sa", jdbcUrl="jdbc:postgresql://127.0.0.1:5432/db1")
//@Db(schema="db1", dialect=MySqlDialect.class, user="sa", password="sa", jdbcUrl="jdbc:mysql://127.0.0.1:3306/")
//@Db(schema="db1", dialect=DerbyDialect.class, user="sa", password="", jdbcUrl="jdbc:derby:C:\\temp\\derby-sample;create=true")
//@Db(schema="db1", dialect=HsqldbDialect.class, user="sa", password="", jdbcUrl="jdbc:hsqldb:mem:db1")
//@Db(schema= ""  , dialect=FirebirdDialect.class, user="sysdba", password="masterkey", jdbcUrl="jdbc:firebirdsql:localhost/3050:c:\\progra~1\\firebird\\db\\db1.fdb?lc_ctype=UTF8")
//@Db(schema="db1", dialect=OracleDialect.class, user="sa", password="", jdbcUrl="jdbc:oracle:thin:@myhost:1521:orcl")
//@Db(schema="db1", dialect=org.ujorm.orm.dialect.MSSqlDialect.class, user="sa", password="datamaster", jdbcUrl="jdbc:sqlserver://127.0.0.1:1433")
public class Database extends OrmTable<Database> {

    /** Customer order. The used annotation overwrites a database schema from the key schema. */
    @Table("ord_order")
    public static final RelationToMany<Database,Order> ORDERS = newRelation();

    /** Items of the Customer order */
    @Table("ord_item")
    public static final RelationToMany<Database,Item> ORDER_ITEMS = newRelation();

    /** View to aggregate data. */
    @View("ord_order")
    public static final RelationToMany<Database,ViewOrder> VIEW_ORDERS = newRelation();

    /** Customer */
    @Table("ord_customer")
    public static final RelationToMany<Database,Customer> CUSTOMER = newRelation();

    // ----- STORED PROCEDURES --------

    /** Database stored procedure */
    @Procedure("ujorm_test")
    public static final Key<Database,MyProcedure> myProcedure = newKey();

}
