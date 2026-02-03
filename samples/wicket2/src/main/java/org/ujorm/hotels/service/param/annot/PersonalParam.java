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

package org.ujorm.hotels.service.param.annot;
import java.lang.annotation.*;

/** The Annotation is intended for personal key parameter.
 * The keys without the annotation are system parameters by default.
 * The System parameter can be changed to the Personal parameter by assigning
 * the annotation only without database modifications unlike private parameters
 * where database modifications are necessary.
 * @see org.ujorm.hotels.service.ModuleParams
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface PersonalParam {

    /** Default personal value is {@code true}- */
    boolean value() default true;

}
