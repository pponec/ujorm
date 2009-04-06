/*
 *  Copyright 2008 Paul Ponec
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

package org.ujoframework.implementation.factory;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.AbstractUjo;

/**
 * The Ujo Factory. A method called readValue() create new instance of the property always by a property type.
 * <br>Each the property type class (see getType() method) must have got at least one of constructor:
 * <ul>
 * <li>an two parameters constructor with types <code>Ujo</code> and <code>UjoProperty</code> or</li>
 * <li>a no parameter constructor</li>
 * </ul>
 * @author Pavel Ponec
 * @since 0.81 
 * @composed 1 - * FactoryProperty
  */
public abstract class FactoryUjo extends AbstractUjo {

    /** It is an unsupported function in this implementation. */
    public void writeValue(final UjoProperty property, final Object value) {
        throw new UnsupportedOperationException();
    }
    
    /** Method readValue() creates a new instance of the property always.
     * @see FactoryProperty#getValue(Ujo) FactoryProperty.getValue(Ujo)
     */
    @SuppressWarnings("unchecked")
    public Object readValue(final UjoProperty property) {
        Object result = ((FactoryProperty) property).readValue(this);
        return result!=null ? result : property.getDefault() ;
    }
    
    // --------- STATIC METHODS -------------------
    
    /** A Property Factory, a default value is null.
     * @hidden     
     */
    protected static <UJO extends Ujo,VALUE> FactoryProperty<UJO, VALUE> newProperty
        ( String name
        , Class<VALUE> type
        ) {
        return new FactoryProperty<UJO,VALUE> (name, type, _nextPropertyIndex());
    }
    
}
