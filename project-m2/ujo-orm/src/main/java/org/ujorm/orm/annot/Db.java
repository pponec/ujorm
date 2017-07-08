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

package org.ujorm.orm.annot;
import java.lang.annotation.*;
import org.ujorm.orm.SqlDialect;
import org.ujorm.orm.UjoSequencer;
import org.ujorm.orm.ao.Orm2ddlPolicy;

/** 
 * Use the annotation to mark a Key static field like XML Attribute.
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.TYPE)
public @interface Db {

    /** Default name of table schema is copied into table models if thay are empty. 
     * @see Table#schema() 
     */
    String schema() default "";
    /** SQL dialect by a DB Vendor. */
    Class<? extends SqlDialect> dialect();
    /** The <a href="http://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface" target="_blank">JNDI</a>
     * (java naming and directory interface) connection string.
     * <br>A typical use on the Tomcat can be:<br> jndi = {"java:comp/env/jdbc/TestDB"}
     * <br>See the 
     * <a href="http://www.mkyong.com/tomcat/how-to-configure-mysql-datasource-in-tomcat-6/" target="_blank">link</a> or
     * <a href="http://tomcat.apache.org/tomcat-6.0-doc/jndi-datasource-examples-howto.html" target="_blank">link</a>
     * for more information about JNDI on the Tomcat.
     * @see org.ujorm.orm.metaModel.MetaDatabase#JNDI
     */
    String[] jndi() default {};
    /** JDBC Url */
    String jdbcUrl() default "";
    /** JDBC Driver */
    String jdbcDriver() default "";
    /** Connection User */
    String user()     default "";
    /** Connection password */
    String password() default "";
    /** The sequencer class for tables of the current database.
     * A value can be a subtype of 'org.ujorm.orm.UjoSequencer' with one-parameter constructor type of MetaTable.
     * If the NULL value is specified the then a default sequencer 'UjoSequencer' will be used. */
    Class<? extends UjoSequencer> sequencer() default org.ujorm.orm.UjoSequencer.class;
    /** Default read-only state for all database tables.
     * The parameter value is evaluated in the execution SQL commands type of INSERT, UPDATE, and DELETE.
     * <br>Note, that only the default value FALSE can be overwritten by a table annotation or by a XML config. 
     * @see Table#readOnly()
     */
    boolean readOnly() default false;
    /** Parameter to control how the DLL (Data Definition Language) statements will be used
     * to a defining data structure modification.
     * The value can be defined a parent, so the hierarchy from the parent to a child is:
     * <ul>
     *   <li>Meta Parameters</li>
     *   <li>Database</li>
     *   <li>Table</li>
     * </ul>
     * In case the root Meta Parameters is undefined, then the parameter
     * {@see Orm2ddlPolicy#CREATE_OR_UPDATE_DDL CREATE_OR_UPDATE_DDL}
     * will be used.
     * @see Orm2ddlPolicy#CREATE_OR_UPDATE_DDL
     */
    Orm2ddlPolicy orm2ddlPolicy() default Orm2ddlPolicy.INHERITED;


}
