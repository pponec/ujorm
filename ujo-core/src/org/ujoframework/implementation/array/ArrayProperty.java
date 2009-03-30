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

package org.ujoframework.implementation.array;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.AbstractProperty;

/**
 * An array property implementation
 * @see ArrayUjo
 * @author Paul Ponec
 */
public class ArrayProperty<UJO extends Ujo,VALUE> extends AbstractProperty<UJO,VALUE> implements Comparable<ArrayProperty> {
    
    /** Constructor */
    public ArrayProperty(String name, Class<VALUE> type, int index) {
        super(name, type, index);
    }
    
    /** Constructor with a default value
     * @param defaultValue The value must be type of VALUE exactly (no child).
     */
    @SuppressWarnings("unchecked")
    public ArrayProperty(String name, VALUE defaultValue, int index) {
        this(name, (Class<VALUE>) (Object) defaultValue.getClass(), index);
        setDefault(defaultValue);
    }
    
    /** Constructor */
    public ArrayProperty(UjoProperty<UJO, VALUE> otherProperty, int index) {
        this(otherProperty.getName(), otherProperty.getType(), index);
    }
    
    /** Compare to another ArrayProperty object by a index code. */
    public int compareTo(final ArrayProperty ap) {
        final int result
        = getIndex() < ap.getIndex() ? -1
        : getIndex() > ap.getIndex() ? +1
        : 0
        ;
        return result;
    }    
}
