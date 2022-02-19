/*
 *  Copyright 2007-2022 Pavel Ponec
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

package org.ujorm.implementation.bean;

import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.core.BeanManager;
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.ValueAgent;
import static org.ujorm.extensions.PropertyModifier.*;

/**
 * Property List implementation. There is used an List collection.
 * @see BeanProperty
 * @author Pavel Ponec  
 */
public class BeanPropertyList<UJO extends Ujo, ITEM> 
extends ListProperty<UJO, ITEM>
implements ValueAgent<Object,Object>, ListKey<UJO, ITEM>
{

    /** Bean Manager instance */
    private final BeanManager<Object,Object> beanManager;
    
    /** Constructor */
    @SuppressWarnings("unchecked")
    public BeanPropertyList(String name, Class<ITEM> itemType, int index) {
        super(itemType);
        init(INDEX, index);
        init(NAME, name);
        beanManager = BeanManager.getInstance(this);
    }
    
    /** WARNING: There is recommended to call the method from the method Ujo.writeProperty(...) only.
     * <br>A direct call can bypass a important actions implemented in the writeProperty(method).
     */
    @Override
    public void writeValue(Object bean, Object value) throws IllegalArgumentException {
        beanManager.writeValue(bean, value);
    }

    /** WARNING: There is recommended to call the method from the method <code>Ujo.readProperty(...)</code> only.
     * <br>A direct call can bypass a important actions implemented in the <code>readProperty(method)</code>.
     */
    @Override
    public Object readValue(Object bean) {
        return beanManager.readValue(bean);
    }

}
