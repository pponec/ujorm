/*
 *  Copyright 2009 Pavel Ponec
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

package org.ujorm.orm.inheritance.sample;


import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.annot.Db;
import org.ujorm.orm.dialect.*;
import org.ujorm.orm.inheritance.sample.bo.Customer;
import org.ujorm.orm.inheritance.sample.bo.User;

/**
 * An table mapping to a database (a sample of usage).
 * @hidden
 */
@Db(schema="db_i", dialect=H2Dialect.class, user="sa", password="", jdbcUrl="jdbc:h2:mem:db_i")
//@Db(schema="db1", dialect=PostgreSqlDialect.class, user="sa", password="sa", jdbcUrl="jdbc:postgresql://127.0.0.1:5432/db1")
//@Db(schema="db1", dialect=MySqlDialect.class, user="sa", password="sa", jdbcUrl="jdbc:mysql://127.0.0.1:3306/?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC")
//@Db(schema="db1", dialect=DerbyDialect.class, user="sa", password="", jdbcUrl="jdbc:derby:C:\\temp\\derby-sample;create=true")
//@Db(schema="db1", dialect=HsqldbDialect.class, user="sa", password="", jdbcUrl="jdbc:hsqldb:mem:db1")
//@Db(schema= ""  , dialect=FirebirdDialect.class, user="sysdba", password="masterkey", jdbcUrl="jdbc:firebirdsql:localhost/3050:c:\\progra~1\\firebird\\db\\db1.fdb?lc_ctype=UTF8")
//@Db(schema="db1", dialect=OracleDialect.class, user="sa", password="", jdbcUrl="jdbc:oracle:thin:@myhost:1521:orcl")
//@Db(schema="db1", dialect=org.ujorm.orm.dialect.MSSqlDialect.class, user="sa", password="datamaster", jdbcUrl="jdbc:sqlserver://127.0.0.1:1433")
public class Database extends OrmTable<Database> {

    /** User */
    public static final RelationToMany<Database,User> users = newRelation();

    /** Customer extends the User */
    public static final RelationToMany<Database,Customer> customers = newRelation();

}
