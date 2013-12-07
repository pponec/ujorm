/*
 *  Copyright 2007-2013 Pavel Ponec
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

import static org.ujorm.extensions.PropertyModifier.*;

/**
 * Property Setter
 * @author Ponec
 */
public class PropertyModifier {

    /** Property name */
    public static final int NAME = 901;
    /** Property index */
    public static final int INDEX = 902;
    /** Property type (class) */
    public static final int TYPE = 903;
    /** Doman type type (class) */
    public static final int DOMAIN_TYPE = 904;
    /** Property default value */
    public static final int DEFAULT_VALUE = 905;
    /** Input Validator */
    public static final int VALIDATOR = 907;
    /** Lock all properties after initialization */
    public static final int LOCK = 999;


    /** Write property type into property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setType(Class type, Property property) {
        if (!property.isLock()) {
            property.init(TYPE, type);
            property.init(INDEX, property.getIndex());
        }
    }

    /** Write domain type into property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setDomainType(Class domainType, Property property) {
        if (!property.isLock()) {
            property.init(DOMAIN_TYPE, domainType);
            property.init(INDEX, property.getIndex());
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
            property.init(NAME, name);
        }
    }
    
    /** Set the new index and lock the property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setIndex(int anIndex, Property property) {
        setIndex(anIndex, property, true);
    }    

    /** Set the new index */
    @SuppressWarnings("unchecked")
    public static void setIndex(int index, Property property, boolean lock) {
        if (!property.isLock() && property.getIndex()!=index) {
            property.init(INDEX, index);
            property.init(LOCK, lock);
        }
    }

    /** Set the new index and lock the property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void lock(Property property) {
        if (!property.isLock()) {
            property.init(LOCK, true);
        }
    }

    /** Lock the property */
    public static boolean isLock(Property property) {
        return property.isLock();
    }

}
