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

package org.ujoframework.implementation.map;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.AbstractProperty;

/**
 * A Abstract property implementation.
 * @see MapUjo
 * @author Pavel Ponec
 */
public class MapProperty<UJO extends Ujo,VALUE> 
    extends AbstractProperty<UJO,VALUE> {
       
    /** Constructor */
    public MapProperty(final String name, final Class<VALUE> type) {
        this(name, type, -1);
    }

    /**
     * Constructor with an property order
     * @param name
     * @param type
     * @param index On order of the property.
     */
    public MapProperty(final String name, final Class<VALUE> type, final int index) {
        super(name, type, index);
    }

    /** Constructor with a default value
     * @param defaultValue The value must be type of VALUE exactly (no child).
     */
    public MapProperty(final String name, final VALUE defaultValue) {
        this(name, defaultValue, -1);
    }


    /** Constructor with a default value
     * @param defaultValue The value must be type of VALUE exactly (no child).
     */
    @SuppressWarnings("unchecked")
    public MapProperty(final String name, final VALUE defaultValue, final int index) {
        this(name, (Class<VALUE>) (Object) defaultValue.getClass(), index);
        setDefault(defaultValue);
    }

    /** Constructor */
    public MapProperty(UjoProperty<UJO, VALUE> otherProperty, int index) {
        this(otherProperty.getName(), otherProperty.getType(), index);
    }

    // --------- STATIC METHODS -------------------

    /** A Property Factory, a default value is null.
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> MapProperty<UJO,VALUE> newInstance(String name, Class<VALUE> type, int index) {
        return new MapProperty<UJO,VALUE> (name, type, index);
    }

    /** A Property Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo, VALUE> MapProperty<UJO, VALUE> newInstance(String name, VALUE value, int index) {
        return new MapProperty<UJO, VALUE>(name, value, index);
    }
    
}
