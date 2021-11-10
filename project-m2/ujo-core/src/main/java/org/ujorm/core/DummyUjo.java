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
package org.ujorm.core;

import org.jetbrains.annotations.Nullable;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.extensions.Property;

/**
 * Data object abstract implementation.
 * Java 1.5 syntax complied.
 * @author Pavel Ponec
 */
final class DummyUjo implements Ujo {

    public static final Key<DummyUjo,Object> P0 = Property.of("A", Object.class, DummyUjo.class, Property.UNDEFINED_INDEX);
    public static final Key<DummyUjo,Object> P1 = Property.of("B", Object.class, DummyUjo.class, Property.UNDEFINED_INDEX);

    /** A dummy implementation. */
    @Override
    public void writeValue(Key key, Object value) {}

    /** A dummy implementation. */
    @Override
    @Nullable
    public Object readValue(Key key) { return null;  }

    /** Returns unsorted keys. */
    @Override
    @SuppressWarnings("unchecked")
    public KeyList readKeys() {
        final Key[] ps = UjoManager.getInstance().readPropertiesNocache(getClass(), false);
        return KeyRing.of(DummyUjo.class, ps);
    }

    /** Is an order of keys reversed? */
    public Boolean isPropertiesReversed() {
        final KeyList props = readKeys();
        final Boolean result = props.get(0)==P1;
        return result;
    }

    /**
     * Get a visibility of the key for different actions.
     */
    @Override
    public boolean readAuthorization(UjoAction action, Key key, Object value) {
        return true;
    }


}
