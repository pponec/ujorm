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

import org.ujorm.Ujo;
import org.ujorm.core.UjoIterator;
import org.ujorm.extensions.AbstractCollectionProperty;
import org.ujorm.orm.ExtendedOrmUjo;
import org.ujorm.orm.Session;

/**
 * The relation 1:N to another UJO type items
 * @author Pavel Ponec
 * @see org.ujorm.core.UjoIterator
 */
public class RelationToMany<UJO extends ExtendedOrmUjo, ITEM extends ExtendedOrmUjo>
        extends AbstractCollectionProperty<UJO, UjoIterator<ITEM>, ITEM> {

    /** Constructor
     * @param name optional
     * @param itemType optional
     */
    public RelationToMany(String name) {
        this(name, null, UNDEFINED_INDEX, false);
    }

    /** Constructor
     * @param name optional
     * @param itemType optional
     */
    public RelationToMany(String name, Class<ITEM> itemType) {
        this(name, itemType, UNDEFINED_INDEX, false);
    }

    /** Constructor
     * @param name Property name.
     * @param itemType The type of item (optional)
     * @param index An property order (optional)
     */
    @SuppressWarnings("unchecked")
    public RelationToMany(String name, Class<ITEM> itemType, int index, boolean lock) {
        super((Class<UjoIterator<ITEM>>) (Object) UjoIterator.class);
        initItemType(itemType);
        init(name, null, null, null, index, lock);
    }

    /**
     * An alias for the method of(Ujo).
     * The {@code null} value is not replaced for a default one.
     * @see #of(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public UjoIterator<ITEM> of(final UJO ujo) {
        final Session mySession = ujo.readSession();  // maybe readSession() is better?

        if (mySession != null
        &&  mySession.getHandler().isPersistent(this)) {
            return mySession.iterateInternal((RelationToMany) this, ujo);
            // Don't save the result!
        } else {
            return (UjoIterator<ITEM>) ujo.readValue(this);
        }
    }
}