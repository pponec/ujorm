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

/** 
 * View is a description of database view. Use it simillary like a table.
 * @see Table
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface View {

    /** A named parameter for the view name. Default value is taken from a relation key name. */
    String name() default Table.NULL;
    /** A shortcut for the attribute "name" of View.
     * @see #name()
     */
    String value() default Table.NULL;
    /** View alias name. The default value is taken from a name. */
    String alias() default Table.NULL;
    /** Mapping a VIEW to the SQL SELECT.
     * The expession <code>${SCHEMA}</code> is replaced for the real schema name in the SQL sttatement.
     * @see org.ujorm.orm.metaModel.MetaSelect.SCHEMA
     */
    String  select() default Table.NULL;
    /** Name of schema. If the value is empty than a default database schema is used. */
    String schema() default Table.NULL;
    
}
