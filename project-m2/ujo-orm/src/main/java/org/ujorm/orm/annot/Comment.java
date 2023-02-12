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
 * Use the annotation to comment a database table or column.<br>
 * In case you are using the database MySQL, please see more information about the column {@link org.ujorm.orm.dialect.MySqlDialect#printComment(org.ujorm.orm.metaModel.MetaColumn, java.lang.Appendable) implementation }
 * @see org.ujorm.orm.dialect.MySqlDialect#printComment(org.ujorm.orm.metaModel.MetaColumn, java.lang.Appendable) MySqlDialect column implementation
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Comment {

    /** A String for the NULL value. */
    String NULL = ""; // TODO: try to use: "<NULL>"

    String value() default NULL;

}
