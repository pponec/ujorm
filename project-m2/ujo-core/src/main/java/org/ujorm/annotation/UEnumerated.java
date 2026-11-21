/*
 *  Copyright 2026-2026 Pavel Ponec
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
package org.ujorm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Selects the database representation of an Enum attribute.
 * An Enum attribute without this annotation is stored by its name.
 * <p>An Ujorm alternative of the Jakarta Persistence {@code @Enumerated}: a domain class can use it
 * without the {@code jakarta.persistence} API on the classpath. Both variants are supported;
 * an element carrying both of them must declare the same values.
 * <p>The attributes are a subset of the {@code @Enumerated} attributes, with the same names, meanings
 * and default values; Ujorm doesn't read the other ones.
 *
 * @since 3.0.7
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UEnumerated {
    /** The Enum representation; the default is {@code ORDINAL} like in the Jakarta Persistence. */
    UEnumType value() default UEnumType.ORDINAL;
}
