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
 * Assigns a value converter to the attribute.
 * <p>The converter class implements one of the interfaces:
 * <ul>
 *   <li>{@code org.ujorm.core.converter.DbConverter} of the Ujorm, which needs no Jakarta Persistence API.
 *       Two converters of the same class should be equal (override {@code equals()} and {@code hashCode()}),
 *       otherwise two columns with the converter can't be compared in a query.</li>
 *   <li>{@code jakarta.persistence.AttributeConverter}, which requires the Jakarta Persistence API at runtime.</li>
 * </ul>
 * The converter must be a public (static nested) class with a public no-argument constructor,
 * and it must be stateless. The value type of the converter equals the (wrapped) attribute type.
 * <p>An Ujorm alternative of the Jakarta Persistence {@code @Convert}: a domain class can use it
 * without the {@code jakarta.persistence} API on the classpath. Both variants are supported;
 * an element carrying both of them must declare the same values.
 * <p>The attributes are a subset of the {@code @Convert} attributes, with the same names, meanings
 * and default values; Ujorm doesn't read the other ones.
 *
 * @since 3.0.7
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UConvert {
    /** The converter class; the default value means no converter. */
    Class<?> converter() default void.class;

    /** Value {@code true} turns off any conversion of the attribute, an auto-applied converter included. */
    boolean disableConversion() default false;
}
