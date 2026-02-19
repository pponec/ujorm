/*
 *  Copyright 2008-2026 Pavel Ponec
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

package org.ujorm.implementation.registrar;

import org.ujorm.Key;
import org.ujorm.implementation.map.MapUjoExt;
import org.ujorm.listener.EventRegistrar;
import org.ujorm.listener.UjoPropertyChangeListener;
import org.ujorm.listener.UjoPropertyChangeSupport;

/**
 * A MapUjoExt implementation with a Property change listener support.
 * <br>There is possible to implement the EventRegistrar interface very easy to any Ujo implementation by sample:
 * <pre class="pre"><span class="keyword-directive">public</span> <span class="keyword-directive">class</span> RegistrarUjoExt&lt;UJO <span class="keyword-directive">extends</span> RegistrarUjoExt&gt; <span class="keyword-directive">extends</span> MapUjoExt&lt;UJO&gt; <span class="keyword-directive">implements</span> EventRegistrar&lt;UJO&gt; {
 *
 *  <span class="keyword-directive">final</span> <span class="keyword-directive">private</span> UjoPropertyChangeSupport eventRegistrar = <span class="keyword-directive">new</span> UjoPropertyChangeSupport(<span class="keyword-directive">this</span>);
 *
 *  <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> writeValue(Key key, Object value) {
 *    Object oldValue = readValue(key);
 *    eventRegistrar.firePropertyChange(key, oldValue, value, <span class="keyword-directive">true</span>);
 *    <span class="keyword-directive">super</span>.writeValue(key, value);
 *    eventRegistrar.firePropertyChange(key, oldValue, value, <span class="keyword-directive">false</span>);
 *  }
 *
 *  <span class="keyword-directive">public</span> <span class="keyword-directive">boolean</span> addPropertyChangeListener
 *    ( Key&lt;UJO,?&gt; key
 *    , Boolean before
 *    , UjoPropertyChangeListener listener
 *    ) {
 *    <span class="keyword-directive">return</span> eventRegistrar.addPropertyChangeListener(key, before, listener);
 *  }
 *
 *  <span class="keyword-directive">public</span> <span class="keyword-directive">boolean</span> removePropertyChangeListener
 *    ( Key&lt;UJO,?&gt; key
 *    , Boolean before
 *    , UjoPropertyChangeListener listener) {
 *    <span class="keyword-directive">return</span> eventRegistrar.removePropertyChangeListener(key, before, listener);
 *  }
 *}</pre>
 * @since ujo-tool
 * @author Pavel Ponec
 */
@SuppressWarnings("deprecation")
public class RegistrarUjoExt<UJO extends RegistrarUjoExt> extends MapUjoExt<UJO> implements EventRegistrar<UJO> {

    final private UjoPropertyChangeSupport eventRegistrar = new UjoPropertyChangeSupport(this);

    @Override
    public void writeValue(Key key, Object value) {
        @SuppressWarnings("unchecked")
        Object oldValue = key.of(this);
        eventRegistrar.firePropertyChange(key, oldValue, value, true);
        super.writeValue(key, value);
        eventRegistrar.firePropertyChange(key, oldValue, value, false);
    }

    /** Add key Listener */
    @Override
    public boolean addPropertyChangeListener
        ( Key<? super UJO,?> key
        , Boolean before
        , UjoPropertyChangeListener listener
        ) {
        return eventRegistrar.addPropertyChangeListener(key, before, listener);
    }

    /** Remove key Listener */
    @Override
    public boolean removePropertyChangeListener
        ( Key<? super UJO,?> key
        , Boolean before
        , UjoPropertyChangeListener listener) {
        return eventRegistrar.removePropertyChangeListener(key, before, listener);
    }
 }
