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
import org.ujoframework.extensions.SuperProperty;

/**
 * A Abstract property implementation.
 * @see MapUjo
 * @author Pavel Ponec
 */
public class MapProperty<UJO extends Ujo,VALUE> 
    extends SuperProperty<UJO,VALUE> {
       
    /** Constructor */
    public MapProperty(String name, Class<VALUE> type) {
        super(name, type, -1);
    }

    /** Constructor with a default value
     * @param defaultValue The value must be type of VALUE exactly (no child).
     */
    @SuppressWarnings("unchecked")
    public MapProperty(String name, VALUE defaultValue) {
        this(name, (Class<VALUE>) (Object) defaultValue.getClass());
        setDefault(defaultValue);
    }
    
    /** Constructor */
    public MapProperty(UjoProperty<UJO, VALUE> otherProperty) {
        this(otherProperty.getName(), otherProperty.getType());
    }
    
}
