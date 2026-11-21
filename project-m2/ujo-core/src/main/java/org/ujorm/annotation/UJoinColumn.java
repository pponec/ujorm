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
 * Maps a relation (a foreign key) to a database column.
 * The {@code nullable} attribute selects the JOIN type of a query: {@code INNER} for {@code false}, else {@code LEFT}.
 * <p>An Ujorm alternative of the Jakarta Persistence {@code @JoinColumn}: a domain class can use it
 * without the {@code jakarta.persistence} API on the classpath. Both variants are supported;
 * an element carrying both of them must declare the same values.
 * <p>The attributes are a subset of the {@code @JoinColumn} attributes, with the same names, meanings
 * and default values; Ujorm doesn't read the other ones.
 *
 * @since 3.0.7
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UJoinColumn {
    /** Database column holding the foreign key; an empty value means the attribute name in the snake_case. */
    String name() default "";

    /** The foreign key accepts the {@code NULL} value. */
    boolean nullable() default true;
}
