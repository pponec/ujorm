/*
 *  Copyright 2009-2010 Pavel Ponec
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

package org.ujorm.implementation.orm;

import org.ujorm.Key;
import org.ujorm.extensions.Property;
import org.ujorm.orm.ExtendedOrmUjo;
import org.ujorm.orm.OrmUjo;

/**
 * Foreign key property
 * @author Pavel Ponec
 */
public class RelationToOne<UJO extends ExtendedOrmUjo, VALUE> extends OrmProperty<UJO, VALUE> {

    private Key relatedKey;

    @SuppressWarnings("unchecked")
    protected RelationToOne(String name, Class type, Key relatedKey) {
        super(UNDEFINED_INDEX);
        init(name, type, null, null, UNDEFINED_INDEX, false);
        this.relatedKey = relatedKey;
    }

    /** Return {@code null} if no related key was assigned. */
    public Key getRelatedKey() {
        return relatedKey;
    }

    // ---- Factory methods ----

    /** A Property Factory */
    //public static <UJO extends OrmUjo, VALUE extends OrmUjo> RelationToOne<UJO, VALUE> newInstance(Key<VALUE,?> relatedKey) {
    //    return new RelationToOne<UJO, VALUE>(null, OrmUjo.class, relatedKey);
    //}

    /** A Property Factory */
    public static <UJO extends ExtendedOrmUjo, VALUE extends ExtendedOrmUjo> RelationToOne<UJO, VALUE> newInstance(Class<VALUE> type, Key<VALUE,?> relatedKey) {
        return new RelationToOne<UJO, VALUE>(null, type, relatedKey);
    }


}