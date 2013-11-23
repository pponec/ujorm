/*
 * UnifiedDataObjectImlp.java
 *
 * Created on year 2012
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.universe;

import org.ujorm.*;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class UniUjoChildImpl implements UniUjoInterfaceChild {
    
    private Object[] data = new Object[KEY_SIZE];

    public Object readValue(Key property) {
        return data[property.getIndex()];
    }

    public void writeValue(Key property, Object value) {
        data[property.getIndex()] = value;
    }

    public KeyList readKeys() {
        return $factory2.getKeys();
    }

    public boolean readAuthorization(UjoAction action, Key property, Object value) {
        return true;
    }



}
