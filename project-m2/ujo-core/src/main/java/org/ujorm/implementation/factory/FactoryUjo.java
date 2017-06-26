/*
 *  Copyright 2008-2014 Pavel Ponec
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

package org.ujorm.implementation.factory;

import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.SuperAbstractUjo;

/**
 * The Ujo Factory. A method called readValue() create new instance of the key always by a key type.
 * <br>Each the key type class (see getType() method) must have got at least one of constructor:
 * <ul>
 * <li>an two parameters constructor with types <code>Ujo</code> and <code>Key</code> or</li>
 * <li>a no parameter constructor</li>
 * </ul>
 * @author Pavel Ponec
 * @since ujo-tool
 * @composed 1 - * FactoryProperty
  */
public abstract class FactoryUjo extends SuperAbstractUjo {

    /** It is an unsupported function in this implementation. */
    @Override
    public void writeValue(final Key key, final Object value) {
        throw new UnsupportedOperationException();
    }
    
    /** Method readValue() creates a new instance of the key always.
     * @see FactoryProperty#getValue(Ujo) FactoryProperty.getValue(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object readValue(final Key key) {
        return ((FactoryProperty) key).readValue(this);
    }
    
    // --------- STATIC METHODS -------------------
    
    /** Returns a new instance of key where the default value is null.
     * @hidden     
     */
    @SuppressWarnings("unchecked")
    protected static <UJO extends Ujo,VALUE> FactoryProperty<UJO, VALUE> newKey
        ( String name) {
        return new FactoryProperty<>(name, (Class) null, Property.UNDEFINED_INDEX);
    }
    
}
