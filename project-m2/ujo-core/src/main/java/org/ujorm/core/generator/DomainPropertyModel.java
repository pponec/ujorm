/*
 * Copyright 2026-2026 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.core.generator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.common.Primitive;

import java.util.List;

public record DomainPropertyModel(
        /** Name of property from the class field. */
        String propertyName,
        /** Type of property from the class field. */
        Class<?> propertyType,
        /** Name of the getter method */
        String getter,
        /** Missing setter has the NULL value */
        @Nullable
        String setter,
        /** Name of database column */
        String dbColumName,
        /** Non-null column feature by the JPA annotation */
        boolean required,
        /** Database primary key */
        boolean primaryKey,
        /** Database foreign key */
        boolean foreignKey
) {

    public Class<?> propertyObjectType() {
        return Primitive.wrapPrimitive(propertyType);
    }

    public boolean isPrimitive() {
        return propertyType.isPrimitive();
    }


    /**
     * Creates a list of DomainPropertyModel for the given class (Bean or Record).
     * Parses JPA annotations to populate database column names, primary keys, and nullability.
     * Traverses the class hierarchy to include inherited properties.
     *
     * @param beanOrRecord The class to inspect.
     * @return List of property models describing the class attributes.
     */
    @NotNull
    public static List<DomainPropertyModel> of(@NotNull Class<?> beanOrRecord) {
        return new DomainModelBuilder().createPropertyModel(beanOrRecord);
    }
}