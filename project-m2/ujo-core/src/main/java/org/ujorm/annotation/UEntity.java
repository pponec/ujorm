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
 * Marks a class mapped to a database table, the {@link UTable} annotation is optional then.
 * It triggers the generation of the {@code Meta*} class by the Ujorm APT processor.
 * <p>An Ujorm alternative of the Jakarta Persistence {@code @Entity}: a domain class can use it
 * without the {@code jakarta.persistence} API on the classpath. Both variants are supported;
 * an element carrying both of them must declare the same values.
 * <p>It has no attributes, because Ujorm reads none of the {@code @Entity} attributes.
 *
 * @since 3.0.7
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UEntity {
}
