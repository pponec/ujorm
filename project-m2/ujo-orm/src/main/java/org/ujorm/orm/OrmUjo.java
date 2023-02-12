/*
 *  Copyright 2020-2022 Pavel Ponec
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

package org.ujorm.orm;

import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.implementation.orm.RelationToMany;

/**
 * The OrmUjo is a basic interface of the persistent object in the ORM support.
 * The class can be Serializable.
 * A class that implements the interface must have got a next special features:
 * <ul>
 *   <li>exactly one Key must be identified as the primary key. </li>
 *   <li>A Key for a relation type of many-to-one (for lazy lodading) must be an instance of {@link OrmKey}</li>
 *   <li>reference to a foreign BO must be able to store an object of any type by the method Ujo.writeProperty(...).
 *       This feature is necessary for the proper functioning of the lazy initialization</li>
 *   <li>relation many to one can be mapped by a RelationToMany key</li>
 *   <li>each table OrmUjo must be registered in the Database by a key type of RelationToMany</li>
 * </ul>
 *
 * @author Ponec
 * @see OrmKeyFactory
 * @see OrmKey
 * @see RelationToMany
 */
public interface OrmUjo extends Ujo {

    /** Read an ORM session where the session is an transient key. */
    @Nullable Session readSession();

    /** Write an ORM session. */
    void writeSession(@Nullable Session session);

    /**
     * Returns keys of changed values in a time when any <strong>session</strong> is assigned.
     * The method is used by a SQL UPDATE statement to update assigned values only.
     * Implementation tip: create a new key type of {@link Set<Key>}
     * and in the method writeValue assign the current Key always.
     * @param clear True value clears all the key changes.
     * @return Key array of the modified values.
     * @see Use the {@link #checkModificatinFlag(org.ujorm.Key) } rather
     */
    @Deprecated
    Key[] readChangedProperties(boolean clear);

    /** Check the attribute modification flag */
    <UJO, VALUE> boolean checkModificationFlag(@NotNull Key key);

    /** Clear all modification flags */
    void clearModificationFlags();

    /** Clone the first level of properties */
    @NotNull OrmUjo cloneUjo();

}
