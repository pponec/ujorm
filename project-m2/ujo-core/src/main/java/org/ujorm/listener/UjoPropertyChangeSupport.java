/*
 *  Copyright 2008-2010 Pavel Ponec
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

package org.ujorm.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.ujorm.Ujo;
import org.ujorm.Key;

/**
 * A Property Change Support for the UJO objects.
 * @since ujo-tool
 * @author Pavel Ponec
 */
public class UjoPropertyChangeSupport /*<Ujo extends Ujo> implements EventRegistrar<Ujo>*/ {
    
    /** Source */
    final private Ujo source;

    /** Supported actions: Before, After or both. */
    final private Boolean before;
    
    /** Property Listeners Before */
    private HashMap<Key,List<UjoPropertyChangeListener>> listenerMapBefore;

    /** Property Listeners After */
    private HashMap<Key,List<UjoPropertyChangeListener>> listenerMapAfter;
    
    /** Constructor */
    public UjoPropertyChangeSupport(Ujo source) {
        this(source, null);
    }
    
    /**
     * Constructor 
     * @param source The source object.
     * @param before The parameter can create a restriction for a listener type
     * <ul>
     *   <li>TRUE - allows to register the listeners before writting value only</li>
     *   <li>FALSE - allows to register the listeners after awritting value only</li>
     *   <li>NULL - allows to register both type of listeners</li>
     * </ul>
     */
    public UjoPropertyChangeSupport(Ujo source, Boolean before) {
        this.source = source;
        this.before = before;
    }
    
    /** Get a not null listener list for the required property. 
     * <br>The method creates an empty list if the one was not found.
     */
    private final List<UjoPropertyChangeListener> getListeners
        ( final Key property
        , final boolean before
        ){
        
        HashMap<Key,List<UjoPropertyChangeListener>> listenerMap = before
            ? listenerMapBefore
            : listenerMapAfter
            ;
        
        if (listenerMap==null) {
            listenerMap = new HashMap<Key,List<UjoPropertyChangeListener>>();
            if (before) {
                listenerMapBefore = listenerMap;
            } else {
                listenerMapAfter  = listenerMap;
            }
        }
        
        List<UjoPropertyChangeListener> result = listenerMap.get(property);
        
        if (result==null) {
            result = new ArrayList<UjoPropertyChangeListener>(1);
            listenerMap.put(property, result);
        }
        return result;
    }
    
    /** Add listener */
    public boolean addPropertyChangeListener
        ( final Key property
        , final Boolean before
        , final UjoPropertyChangeListener listener
        ){
        testSupport(before);
        
        if (before==null) {
            boolean b1 = getListeners(property, true ).add(listener);
            boolean b2 = getListeners(property, false).add(listener);
            return  b1 && b2;
        } else {
            return getListeners(property, before).add(listener);
        }
    }

    /** Remove listener */
    public boolean removePropertyChangeListener
        ( final Key property
        , final Boolean before
        , final UjoPropertyChangeListener listener
        ){
        testSupport(before);
        
        if (before==null) {
            boolean b1 = getListeners(property, true ).remove(listener);
            boolean b2 = getListeners(property, false).remove(listener);
            return  b1 && b2;
        } else {
            return getListeners(property, before).remove(listener);
        }
    }
    
    /** Fire event for the property */
    public void firePropertyChange
        ( final Key property
        , final Object oldValue
        , final Object newValue
        , final boolean beforeChange
        ){
        
          HashMap<Key,List<UjoPropertyChangeListener>> listenerMap = beforeChange
              ? listenerMapBefore
              : listenerMapAfter
              ;
          
          if (listenerMap==null) { return; }
        
          List<UjoPropertyChangeListener> listeners = listenerMap.get(property);
        
          if (listeners!=null) {
             UjoPropertyChangeEvent event = new UjoPropertyChangeEvent(source, property, oldValue, newValue, beforeChange);
            
             for (UjoPropertyChangeListener listener : listeners) {
                 listener.propertyChange(event);
             }
          }
    }
    
    /** Test support. In case the unsupported operation throws an exception. */
    private void testSupport(final Boolean before) {
        if (this.before==null
        ||  this.before==before
        ||  this.before.equals(before)
        ){
            return;
        } else {
            String action = before == null ? "before or after" : before==true ? "before" : "after" ;
            throw new UnsupportedOperationException("Action is not supported : " + action);
        }
    }

    /**
     * Returns a type of support.
     * @return TRUE means before support, FALSE means after support, NULL means a both action support.
     */
    public Boolean getBefore() {
        return before;
    }
    
    

}
