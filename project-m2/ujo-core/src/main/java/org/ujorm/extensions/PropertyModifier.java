/*
 *  Copyright 2007-2014 Pavel Ponec
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

import java.util.List;
import org.ujorm.Ujo;
import org.ujorm.tools.Assert;
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
    /** Domain type type (class) */
    public static final int DOMAIN_TYPE = 904;
    /** Property default value */
    public static final int DEFAULT_VALUE = 905;
    /** Input Validator */
    public static final int VALIDATOR = 907;
    /** Lock all properties after initialization */
    public static final int LOCK = 999;


    /** Write key type into key if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static <U extends Ujo, V> void setType(Class<V> type, Property<U,V> key) {
        if (!key.isLock()) {
            key.init(TYPE, type);
            key.init(INDEX, key.getIndex());
        }
    }

    /** Write domain type into key if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static <U extends Ujo, V> void setDomainType(Class<U> domainType, Property<U,V> key) {
        if (!key.isLock()) {
            key.init(DOMAIN_TYPE, domainType);
            key.init(INDEX, key.getIndex());
        }
    }

    /** Write an item type into key if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static <U extends Ujo, V> void setItemType(Class<V> itemType, AbstractCollectionProperty<U,List,V> key) {
        Assert.notNull(itemType, "Item type is undefined for key: {}", key);

        if (!key.isLock()) {
            key.initItemType(itemType);
        }
    }

    /** Write name into key if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setName(String name, Property key) {
        if (!key.isLock()) {
            key.init(NAME, name);
        }
    }

    /** Write a default value. */
    @SuppressWarnings("unchecked")
    public static <U extends Ujo, V> void setDefaultValue(V value, Property<U, V> key) {
        if (!key.isLock()) {
            key.init(DEFAULT_VALUE, value);
        }
    }

    /** Set the new index and lock the key if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setIndex(int anIndex, Property key) {
        setIndex(anIndex, key, true);
    }

    /** Set the new index */
    @SuppressWarnings("unchecked")
    public static void setIndex(int index, Property key, boolean lock) {
        if (!key.isLock() && key.getIndex()!=index) {
            key.init(INDEX, index);
            key.init(LOCK, lock);
        }
    }

    /** Set the new index and lock the key if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void lock(Property key) {
        if (!key.isLock()) {
            key.init(LOCK, true);
        }
    }

    /** Lock the key */
    public static boolean isLock(Property key) {
        return key.isLock();
    }

}
