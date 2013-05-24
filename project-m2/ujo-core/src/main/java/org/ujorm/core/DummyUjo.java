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

package org.ujorm.core;

import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.UjoPropertyList;
import org.ujorm.extensions.Property;
import org.ujorm.UjoAction;

/**
 * Data object abstract implementation.
 * Java 1.5 syntax complied.
 * @author Pavel Ponec
 */
final class DummyUjo implements Ujo {
    
    public static final Key<DummyUjo,Object> P0 = Property.newInstance("A", Object.class, DummyUjo.class, Property.UNDEFINED_INDEX);
    public static final Key<DummyUjo,Object> P1 = Property.newInstance("B", Object.class, DummyUjo.class, Property.UNDEFINED_INDEX);
    
    /** A dummy implementation. */
    @Override
    public void writeValue(Key property, Object value) {}

    /** A dummy implementation. */
    @Override
    public Object readValue(Key property) { return null;  }

    /** Returns unsorted keys. */
    @Override
    @SuppressWarnings("unchecked")
    public KeyList readKeys() {
        final Key[] ps = UjoManager.getInstance().readPropertiesNocache(getClass(), false);
        return KeyRing.of(DummyUjo.class, ps);
    }

    /** Returns unsorted keys. */
    @SuppressWarnings("deprecation")
    public UjoPropertyList readProperties() {
        return new UjoPropertyListImpl(readKeys());
    }
    
    /** Is an order of keys reversed? */
    public Boolean isPropertiesReversed() {
        final KeyList props = readKeys();
        final Boolean result = Boolean.valueOf(props.get(0)==P1);
        return result;
    }

    /**
     * Get a visibility of the property for different actions.
     */
    public boolean readAuthorization(UjoAction action, Key property, Object value) {
        return true;
    }

    
}
