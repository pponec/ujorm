/*
 *  Copyright 2020-2026 Pavel Ponec
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

package org.ujorm.core.annot;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
    /** JDBC Url */
    String jdbcUrl() default "";
    /** JDBC Driver */
    String jdbcDriver() default "";
    /** Connection User */
    String user()     default "";
    /** Connection password */
    String password() default "";
    /** Default read-only state for all database tables.
     * The parameter value is evaluated in the execution SQL commands type of INSERT, UPDATE, and DELETE.
     * <br>Note, that only the default value FALSE can be overwritten by a table annotation or by a XML config.
     * @see Table#readOnly()
     */
    boolean readOnly() default false;


}
