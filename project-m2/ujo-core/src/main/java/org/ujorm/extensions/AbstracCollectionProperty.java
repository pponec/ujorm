/*
 *  Copyright 2012-2012 Pavel Ponec
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

package org.ujorm.extensions;

import org.ujorm.Ujo;
import org.ujorm.core.annot.Immutable;

/**
 * The abstract implementation for property of the collection type.
 * @see AbstracCollectionProperty
 * @author Pavel Ponec
 */
@Immutable
abstract public class AbstracCollectionProperty<UJO extends Ujo,VALUE,ITEM> extends Property<UJO,VALUE> {

    /** Class of the list item. */
    private Class<ITEM> itemType;

    public AbstracCollectionProperty(Class<VALUE> collectionType) {
        super(UNDEFINED_INDEX);
        init(null, collectionType, null, UNDEFINED_INDEX, false);
    }

    public AbstracCollectionProperty(String name, Class<VALUE> collectionType, int index) {
        super(index);
        init(name, collectionType, null, index, false);
    }
    
    /** Return a Class of the Item. */
    public Class<ITEM> getItemType() {
        return itemType;
    }
    
    /** Returns true if the item type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    public boolean isItemTypeOf(final Class type) {
        return type.isAssignableFrom(this.itemType);
    }    
    
    /**
     * Item type property initialization.
     * @param itemType Item type
     */
    @SuppressWarnings("unchecked")
    final protected void initItemType(final Class<ITEM> itemType) {
        checkLock();
        if (itemType != null) {
            this.itemType = itemType;
        }
    }
}
