/*
 *  Copyright 2007-2012 Pavel Ponec
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

/**
 * Property Setter
 * @author Ponec
 */
public class PropertyModifier {

    /** Write property type into property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setType(Class type, Property property) {
        if (!property.isLock()) {
            final int index = property.getIndex();
            property.init(null, type, null, null, index, false);
        }
    }

    /** Write domain type into property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setDomainType(Class domainType, Property property) {
        if (!property.isLock()) {
            final int index = property.getIndex();
            property.init(null, null, domainType, null, index, false);
        }
    }

    /** Write an item type into property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setItemType(Class itemType, AbstractCollectionProperty property) {
        if (itemType==null) {
            throw new IllegalArgumentException("Item type is undefined for property: " + property);
        }
        if (!property.isLock()) {
            property.initItemType(itemType);
        }
    }
    
    /** Write name into property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setName(String name, Property property) {
        if (!property.isLock()) {
            property.init(name, null, null, null, Property.UNDEFINED_INDEX, false);
        }
    }
    
    /** Set the new index and lock the property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setIndex(int anIndex, Property property) {
        setIndex(anIndex, property, true);
    }    

    /** Set the new index */
    @SuppressWarnings("unchecked")
    public static void setIndex(int anIndex, Property property, boolean lock) {
        if (!property.isLock() && property.getIndex()!=anIndex) {
            property.init(null, null, null, null, anIndex, lock);
        }
    }

    /** Set the new index and lock the property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void lock(Property property) {
        if (!property.isLock()) {
            property.init(null, null, null, null, Property.UNDEFINED_INDEX, true);
        }
    }

    /** Lock the property */
    public static boolean isLock(Property property) {
        return property.isLock();
    }

}
