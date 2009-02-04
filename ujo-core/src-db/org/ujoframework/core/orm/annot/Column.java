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

package org.ujoframework.core.orm.annot;
import java.lang.annotation.*;
import org.ujoframework.core.orm.DbType;

/** 
 * Use the annotation to mark a UjoProperty static field like XML Attribute.
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.FIELD)
public @interface Column {

    /** Column name */
    String name() default "";
    /** The primary key */
    boolean pk() default false;
    /** Database column type */
    DbType type() default DbType.Automatic;
    /** Database column lenght */
    int maxLenght() default -1;
    /** Database column presision */
    int precision() default -1;
    /** Not null value */
    boolean mandatory() default false;
    /** The column is included in the index of the name. */
    String indexName() default "";

    
}
