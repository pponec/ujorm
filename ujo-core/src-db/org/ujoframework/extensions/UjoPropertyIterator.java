/*
 *  Copyright 2009 Paul Ponec
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

package org.ujoframework.extensions;

import java.util.NoSuchElementException;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;

/**
 * A property metadata of Unified Data Object.
 * @author Pavel Ponec
 */
public interface UjoPropertyIterator<UJO extends Ujo, ITEM> extends UjoProperty<UJO, UjoIterator> {
    
    /** Returns a class of the property. */
    public Class<ITEM> getItemType();
    
    /** Returns a count of Items. If the property is null, method returns 0. */
    public int getItemCount(UJO ujo);
    
    /** Returns a value of property. The result is the same, like Ujo#readValue(UjoPropertyList). */
    public ITEM getNextItem(UJO ujo) throws NoSuchElementException;

    /** Returns a value of property. The result is the same, like Ujo#readValue(UjoPropertyList). */
    public boolean hasNextItem(UJO ujo);
    
}
