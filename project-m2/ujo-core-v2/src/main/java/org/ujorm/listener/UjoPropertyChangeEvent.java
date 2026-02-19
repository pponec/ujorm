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

package org.ujorm.listener;

import java.beans.PropertyChangeEvent;
import org.ujorm.Key;

/**
 * A "PropertyChange" event gets delivered whenever a bean changes a "bound"
 * or "constrained" key.  A PropertyChangeEvent object is sent as an
 * argument to the PropertyChangeListener and VetoableChangeListener methods.
 * <P>See more information in a PropertyChangeEvent.
 * @since ujo-tool
 * @author Pavel Ponec
 */
public class UjoPropertyChangeEvent extends PropertyChangeEvent {
    
    final Key key;
    final boolean beforeChange;

    public UjoPropertyChangeEvent
        ( final Object source
        , final Key key
        , final Object oldValue
        , final Object newValue
        , final boolean beforeChange
        ){
        super(source, key.getName(), oldValue, newValue);
        this.key = key;
        this.beforeChange = beforeChange;
    }

    /** Returns key */
    public Key getProperty() {
        return key;
    }

    /** Before change return true else returns false */
    public boolean isBeforeChange() {
        return beforeChange;
    }
    

}
