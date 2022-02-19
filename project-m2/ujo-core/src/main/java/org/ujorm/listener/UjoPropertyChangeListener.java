/*
 *  Copyright 2008-2022 Pavel Ponec
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

/**
 * A "PropertyChange" event gets fired whenever a bean changes a "bound" key.  
 * You can register a PropertyChangeListener with a source bean so as to be notified of any bound key updates.
 * @see java.beans.PropertyChangeListener
 * @since ujo-tool
 * @author Pavel Ponec
 */
public interface UjoPropertyChangeListener {
    
    /**
     * This method gets called when a bound key is changed.
     * @param evt A PropertyChangeEvent object describing the event source 
     * and the key that has changed.
     */
    void propertyChange(UjoPropertyChangeEvent evt); 

}
