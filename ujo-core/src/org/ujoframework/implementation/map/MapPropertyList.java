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

import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.extensions.AbstractPropertyList;

/**
 * Property List implementation. There is used an List collection.
 * @see MapUjo
 * @author Pavel Ponec
 */
public class MapPropertyList<UJO extends Ujo, ITEM> 
    extends AbstractPropertyList<UJO, List<ITEM>, ITEM>
    implements ListProperty<UJO, ITEM>
    {
    
    /** Constructor */
    @SuppressWarnings("unchecked")
    public MapPropertyList(String name, Class<ITEM> itemType) {
        super(name, (Class<List<ITEM>>) (Class) List.class, itemType, -1);
    }

    /**
     * Constuctor
     * @param name Property name
     * @param itemType Property type
     * @param index An order of property.
     */
    @SuppressWarnings("unchecked")
    public MapPropertyList(String name, Class<ITEM> itemType, int index) {
        super(name, (Class<List<ITEM>>) (Class) List.class, itemType, index);
    }

    // --------- STATIC METHODS -------------------

    /** A PropertyList Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo, ITEM> MapPropertyList<UJO,ITEM> newInstance(String name, Class<ITEM> type, int index) {
        return new MapPropertyList<UJO,ITEM> (name, type, index);
    }


}
