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

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.UjoAction;
import org.ujoframework.implementation.map.MapProperty;

/**
 * Data object abstract implementation.
 * Java 1.5 syntax complied.
 * @author Pavel Ponec
 */
@SuppressWarnings("unchecked")
final class DummyUjo implements Ujo {
    
    public static final UjoProperty P0 = new MapProperty("A", Object.class);
    public static final UjoProperty P1 = new MapProperty("B", Object.class);
    
    /** A dummy implementation. */
    public void writeValue(UjoProperty property, Object value) {}

    /** A dummy implementation. */
    public Object readValue(UjoProperty property) { return null;  }

    /** Returns unsorted properties. */
    public UjoProperty[] readProperties() {
        return UjoManager.getInstance().readPropertiesNocache(getClass(), false);
    }    
    
    /** Is an order of properties reversed? */
    public Boolean isPropertiesReversed() {
        final UjoProperty[] props = readProperties();
        final Boolean result = Boolean.valueOf(props[0]==P1);
        return result;
    }

    /**
     * Get a visibility of the property for different actions.
     */
    public boolean readAuthorization(UjoAction action, UjoProperty property, Object value) {
        return true;
    }

    
}
