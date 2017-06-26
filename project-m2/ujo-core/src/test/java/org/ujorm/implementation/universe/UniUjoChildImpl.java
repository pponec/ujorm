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

    private final Object[] data = new Object[KEY_SIZE];

    @Override
    public Object readValue(Key key) {
        return data[key.getIndex()];
    }

    @Override
    public void writeValue(Key key, Object value) {
        data[key.getIndex()] = value;
    }

    @Override
    public KeyList readKeys() {
        return $factory2.getKeys();
    }

    @Override
    public boolean readAuthorization(UjoAction action, Key key, Object value) {
        return true;
    }



}
