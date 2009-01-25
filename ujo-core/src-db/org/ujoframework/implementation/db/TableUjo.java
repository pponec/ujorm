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

package org.ujoframework.implementation.db;

import java.beans.Expression;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.beans.EventRegistrar;
import org.ujoframework.beans.UjoPropertyChangeSupport;
import org.ujoframework.beans.UjoPropertyChangeListener;
import org.ujoframework.implementation.map.MapUjo;

/**
 * An implementation for an ORM solution.
 * @author Ponec
 */
public class TableUjo<UJO extends Ujo> extends MapUjo implements EventRegistrar<UJO> {
    
    final private UjoPropertyChangeSupport eventRegistrar = new UjoPropertyChangeSupport(this, null);
    final private DbHandler handler;

    public TableUjo() {
        handler = DbHandler.getInstance();
        //handler.registerPropertis(getClass(), readProperties());
    }



    @Override
    public void writeValue(UjoProperty property, Object value) {
        Object oldValue = readValue(property);
        eventRegistrar.firePropertyChange(property, oldValue, value, true);
        super.writeValue(property, value);
        eventRegistrar.firePropertyChange(property, oldValue, value, false);
    }

    @Override
    public Object readValue(UjoProperty property) {
        Object result = super.readValue(property);
        if (property instanceof UjoRelative
        &&  handler.isPersistent(property)
        ){
            // Don't save the result!
            result = handler.getSession().iterate(property);
        }
        return result;
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


    // --------- STATIC METHODS -------------------

    /** A PropertyIterator Factory
     * @hidden
     */
    protected static <UJO extends TableUjo, ITEM extends TableUjo> UjoRelative<UJO,ITEM> newRelation(String name, Class<ITEM> type) {
        return new UjoRelative<UJO,ITEM> (name, type);
    }

}
