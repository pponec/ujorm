/*
 * Copyright 2026-2026 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.core.generator;

import java.util.List;

public record DomainModel(
        /** Data class of the bean or record. */
        Class<?> domainClass,
        /** Database table attribues */
        TableIdentifier database,
        /** List of properties */
        List<DomainPropertyModel> properties
) {

    /** Is the domain object the Record ?*/
    public boolean isRecord() {
        return domainClass.isRecord();
    }

    /**
     * Factory method to create a BeanModel instance.
     *
     * @param domainClass The class to inspect.
     * @return result - The populated BeanModel.
     */
    public static DomainModel of(Class<?> domainClass) {
        var properties = DomainPropertyModel.of(domainClass);
        var database = TableIdentifier.of(domainClass);
        return new DomainModel(domainClass, database, properties);
    }

}