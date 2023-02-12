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

package org.ujorm.orm.annot;
import java.lang.annotation.*;
import org.ujorm.orm.ao.Orm2ddlPolicy;
import org.ujorm.orm.ao.QuoteEnum;

/**
 * Use the annotation to mark a Key static field like XML Attribute.
 * @see View
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Table {

    /** A String for the NULL value. */
    String NULL = ""; // TODO: try to use: "<NULL>"

    /** A named parameter for the table name. Default value is taken from a related key name. */
    String name() default NULL;
    /** A shortcut for the attribute "name" of Table.
     * @see #name()
     */
    String value() default NULL;
    /** Table alias name. The default value is taken from a name. */
    String alias() default NULL;
    /** Name of schema. If the value is empty than a default database schema is used.
     * @see Db#schema()
     */
    String schema() default NULL;
    /** Name of DB sequence. The value is not used by default,
     * however a special implementation of the UjoSequencer can do it. */
    String sequence() default NULL;
    /** Database table can have the the read-only state. The value can change the default value of the @Db.readOnly() only.
     * The parameter value is evaluated in the execution SQL commands type of INSERT, UPDATE, and DELETE.
     * @see Db#readOnly()
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

    /** Quoting policy for database table */
    QuoteEnum quoted() default QuoteEnum.BY_CONFIG;
}
