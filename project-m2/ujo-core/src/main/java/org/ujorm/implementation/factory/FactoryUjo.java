/*
 *  Copyright 2008-2010 Pavel Ponec
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
 * The Ujo Factory. A method called readValue() create new instance of the property always by a property type.
 * <br>Each the property type class (see getType() method) must have got at least one of constructor:
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
    public void writeValue(final Key property, final Object value) {
        throw new UnsupportedOperationException();
    }
    
    /** Method readValue() creates a new instance of the property always.
     * @see FactoryProperty#getValue(Ujo) FactoryProperty.getValue(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object readValue(final Key property) {
        return ((FactoryProperty) property).readValue(this);
    }
    
    // --------- STATIC METHODS -------------------
    
    /** Returns a new instance of property where the default value is null.
     * @hidden     
     */
    protected static <UJO extends Ujo,VALUE> FactoryProperty<UJO, VALUE> newKey
        ( String name) {
        return new FactoryProperty<UJO,VALUE> (name, (Class) null, Property.UNDEFINED_INDEX);
    }

    /** Returns a new instance of property where the default value is null.
     * @deprecated Use method newKey(..) rather
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo,VALUE> FactoryProperty<UJO, VALUE> newProperty
        ( String name
        , Class<VALUE> type
        ) {
        return new FactoryProperty<UJO,VALUE> (name, type, Property.UNDEFINED_INDEX);
    }
    
}
