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

package org.ujoframework.implementation.registrar;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.listener.EventRegistrar;
import org.ujoframework.listener.UjoPropertyChangeSupport;
import org.ujoframework.listener.UjoPropertyChangeListener;
import org.ujoframework.implementation.map.MapUjo;

/**
 * A MapUjo implementation with a Property change listener support.
 * <br>There is possible to implement the EventRegistrar interface very easy to any Ujo implementation by sample:
 * <pre class="pre"><span class="keyword-directive">public</span> <span class="keyword-directive">class</span> RegistrarUjo&lt;UJO <span class="keyword-directive">extends</span> Ujo&gt; <span class="keyword-directive">extends</span> MapUjo <span class="keyword-directive">implements</span> EventRegistrar&lt;UJO&gt; {
 *     
 *   <span class="keyword-directive">final</span> <span class="keyword-directive">private</span> UjoPropertyChangeSupport eventRegistrar = <span class="keyword-directive">new</span> UjoPropertyChangeSupport(<span class="keyword-directive">this</span>, <span class="keyword-directive">null</span>);
 * 
 *   <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> writeValue(UjoProperty property, Object value) {
 *     Object oldValue = readValue(property);
 *     eventRegistrar.firePropertyChange(property, oldValue, value, <span class="keyword-directive">true</span>);
 *     <span class="keyword-directive">super</span>.writeValue(property, value);
 *     eventRegistrar.firePropertyChange(property, oldValue, value, <span class="keyword-directive">false</span>);
 *   }
 * 
 *   <span class="keyword-directive">public</span> <span class="keyword-directive">boolean</span> addPropertyChangeListener
 *     ( UjoProperty&lt;UJO,?&gt; property
 *     , Boolean before
 *     , UjoPropertyChangeListener listener
 *     ) {
 *     <span class="keyword-directive">return</span> eventRegistrar.addPropertyChangeListener(property, before, listener);
 *   }
 * 
 *   <span class="keyword-directive">public</span> <span class="keyword-directive">boolean</span> removePropertyChangeListener
 *     ( UjoProperty&lt;UJO,?&gt; property
 *     , Boolean before
 *     , UjoPropertyChangeListener listener
 *     ) {
 *      <span class="keyword-directive">return</span> eventRegistrar.removePropertyChangeListener(property, before, listener);
 *   }
 * }</pre>
 * @since ujo-tool
 * @author Pavel Ponec
 */
public class RegistrarUjo<UJO extends Ujo> extends MapUjo implements EventRegistrar<UJO> {
    
    transient final private UjoPropertyChangeSupport eventRegistrar = new UjoPropertyChangeSupport(this, null);

    @Override
    public void writeValue(UjoProperty property, Object value) {
        @SuppressWarnings("unchecked")
        Object oldValue = property.of(this);
        eventRegistrar.firePropertyChange(property, oldValue, value, true);
        super.writeValue(property, value);
        eventRegistrar.firePropertyChange(property, oldValue, value, false);
    }

    /** Add property Listener */
    public boolean addPropertyChangeListener
        ( UjoProperty<UJO,?> property
        , Boolean before
        , UjoPropertyChangeListener listener
        ) {
        return eventRegistrar.addPropertyChangeListener(property, before, listener);
    }

    /** Remove property Listener */
    public boolean removePropertyChangeListener
        ( UjoProperty<UJO,?> property
        , Boolean before
        , UjoPropertyChangeListener listener
        ) {
         return eventRegistrar.removePropertyChangeListener(property, before, listener);
    }
}
