/*
 *  Copyright 2007 Paul Ponec
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

package org.ujoframework.core;

import org.ujoframework.UjoPropertyList;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.Property;
import org.ujoframework.extensions.UjoAction;

/**
 * Data object abstract implementation.
 * Java 1.5 syntax complied.
 * @author Pavel Ponec
 */
final class DummyUjo implements Ujo {
    
    public static final Property P0 = Property.newInstance("A", Object.class, -1);
    public static final Property P1 = Property.newInstance("B", Object.class, -1);
    
    /** A dummy implementation. */
    public void writeValue(UjoProperty property, Object value) {}

    /** A dummy implementation. */
    public Object readValue(UjoProperty property) { return null;  }

    /** Returns unsorted properties. */
    public UjoPropertyList readProperties() {
        final UjoProperty[] ps = UjoManager.getInstance().readPropertiesNocache(getClass(), false);
        return new UjoPropertyListImpl(DummyUjo.class, ps);
    }    
    
    /** Is an order of properties reversed? */
    public Boolean isPropertiesReversed() {
        final UjoPropertyList props = readProperties();
        final Boolean result = Boolean.valueOf(props.get(0)==P1);
        return result;
    }

    /**
     * Get a visibility of the property for different actions.
     */
    public boolean readAuthorization(UjoAction action, UjoProperty property, Object value) {
        return true;
    }

    
}
