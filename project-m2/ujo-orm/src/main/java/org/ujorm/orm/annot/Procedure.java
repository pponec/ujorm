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

package org.ujorm.orm.annot;
import java.lang.annotation.*;

/**
 * Use the annotation to mark a Key static field like XML Attribute.
 * @see View
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Procedure {

    /** A String for the NULL value. */
    String NULL = Table.NULL;

    /** A named parameter for the stored prodedure. Default value is taken from a relation key name. */
    String name() default NULL;
    /** A shortcut for the attribute "name" of Table.
     * @see #name()
     */
    String value() default NULL;
    /** Name of schema. If the value is empty than a default database schema is used. */
    String schema() default NULL;

}
