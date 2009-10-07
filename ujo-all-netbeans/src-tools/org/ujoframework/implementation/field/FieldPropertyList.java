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

package org.ujoframework.implementation.field;

import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.extensions.ListUjoProperty;
import org.ujoframework.extensions.ValueAgent;
import org.ujoframework.implementation.bean.BeanProperty;

/**
 * Property List implementation. There is used an List collection.
 * @see BeanProperty
 * @since ujo-tool
 * @author Pavel Ponec  
 */
public class FieldPropertyList<UJO extends Ujo, ITEM> 
    extends ListProperty<UJO, ITEM>
    implements ValueAgent<UJO,  List<ITEM>>, ListUjoProperty<UJO, ITEM>
{
    
    /** Bean Manager instance */
    private final ValueAgent<UJO, List<ITEM>> agent;
    
    /** Constructor */
    @SuppressWarnings("unchecked")
    public FieldPropertyList(String name, Class<ITEM> itemType, int index, ValueAgent<UJO, List<ITEM>> agent) {
        super(itemType);
        init(name, (Class) List.class, null, index, true);
        this.agent = agent;
    }
    
    /** WARNING: There is recommended to call the method from the method Ujo.writeProperty(...) only.
     * <br>A direct call can bypass a important actions implemented in the writeProperty(method).
     */
    public void writeValue(UJO ujo, List<ITEM> value) throws IllegalArgumentException {
        agent.writeValue(ujo, value);
    }

    /** WARNING: There is recommended to call the method from the method <code>Ujo.readProperty(...)</code> only.
     * <br>A direct call can bypass a important actions implemented in the <code>readProperty(method)</code>.
     */
    public List<ITEM> readValue(UJO ujo) throws IllegalArgumentException {
        return agent.readValue(ujo);
    }
    
}
