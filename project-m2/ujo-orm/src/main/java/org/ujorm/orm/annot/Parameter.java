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
 * A parameter annotation of the stored procedure.
 * The first key/parameter has always the output type and it provides a result of the stored procedure. If prcedure does not have a return value, the key type must be Void.
 * The next keys/parameters have an input type by default, there is possible to change this type by this annotations.
 * @see Procedure
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.FIELD)
public @interface Parameter {

    /** An input parameter of the stored procedure. */
    boolean input() default false;
    /** An output parameter of the stored procedure. */
    boolean output() default false;

}
