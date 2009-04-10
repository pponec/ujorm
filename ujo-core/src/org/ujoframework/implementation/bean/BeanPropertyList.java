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

package org.ujoframework.implementation.bean;

import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.core.BeanManager;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.extensions.AbstractPropertyList;
import org.ujoframework.extensions.ValueAgent;

/**
 * Property List implementation. There is used an List collection.
 * @see BeanProperty
 * @author Pavel Ponec  
 */
public class BeanPropertyList<UJO extends Ujo, ITEM> 
    extends AbstractPropertyList<UJO, List<ITEM>, ITEM>
    implements ValueAgent<Object,Object>, ListProperty<UJO, ITEM>
    {
    
    /** Bean Manager instance */
    private final BeanManager<Object,Object> beanManager;
    
    /** Constructor */
    @SuppressWarnings("unchecked")
    public BeanPropertyList(String name, Class<ITEM> itemType, int index) {
        super(name, (Class<List<ITEM>>) (Class) List.class, itemType, index);
        beanManager = BeanManager.getInstance(this);
    }
    
    /** WARNING: There is recommended to call the method from the method Ujo.writeProperty(...) only.
     * <br>A direct call can bypass a important actions implemented in the writeProperty(method).
     */
    public void writeValue(Object bean, Object value) throws IllegalArgumentException {
        beanManager.writeValue(bean, value);
    }

    /** WARNING: There is recommended to call the method from the method <code>Ujo.readProperty(...)</code> only.
     * <br>A direct call can bypass a important actions implemented in the <code>readProperty(method)</code>.
     */
    public Object readValue(Object bean) {
        return beanManager.readValue(bean);
    }

    // --------- STATIC METHODS -------------------

    /** A PropertyList Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo, ITEM> BeanPropertyList<UJO,ITEM> newInstance(String name, Class<ITEM> type, int index) {
        return new BeanPropertyList<UJO,ITEM> (name, type, index);
    }


    
}
