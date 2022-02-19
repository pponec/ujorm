/*
 * Copyright 2016-2022 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.orm;

import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Validator;
import org.ujorm.core.KeyFactory;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.orm.OrmProperty;
import org.ujorm.implementation.orm.RelationToMany;

/**
 * OrmFactory
 * @author Pavel Ponec
 */
public class OrmKeyFactory<UJO extends OrmUjo> extends KeyFactory<UJO> {

    public OrmKeyFactory(Class<? extends UJO> type, boolean propertyCamelCase, KeyList<?> abstractSuperProperties) {
        super(type, propertyCamelCase, abstractSuperProperties);
    }

    public OrmKeyFactory(Class<? extends UJO> type, boolean propertyCamelCase) {
        super(type, propertyCamelCase);
    }

    public OrmKeyFactory(Class<? extends UJO> type) {
        super(type);
    }

    /** Common protected factory method */
    @Override
    protected <T> Key<UJO,T> createKey(String name, T defaultValue, Validator<T> validator) {
        final OrmProperty<UJO,T> p = new OrmProperty(Property.UNDEFINED_INDEX, name, defaultValue, validator);
        addKey(p);
        return p;
    }

    /** A KeyIterator Factory creates an new key and assign a next index.
     * @return RelationToMany
     * @hidden
     */
    public <UJO extends ExtendedOrmUjo, ITEM extends ExtendedOrmUjo> RelationToMany<UJO,ITEM> newRelation() {
        final RelationToMany<UJO,ITEM> p = new RelationToMany<>(null);
        addKey(p);
        return p;
    }


}
