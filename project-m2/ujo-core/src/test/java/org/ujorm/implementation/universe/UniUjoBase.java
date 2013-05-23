/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.universe;

import java.util.Date;
import org.ujorm.*;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoPropertyListImpl;
import static org.ujorm.Validator.Build.*;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class UniUjoBase implements Ujo {

    /** Factory */
    private static final KeyFactory<UniUjoBase> f
            = KeyFactory.CamelBuilder.get(UniUjoBase.class);
    
    public static final Key<UniUjoBase,Long>      PRO_P0 = f.newKey(notNull(Long.class));
    public static final Key<UniUjoBase,Integer>   PRO_P1 = f.newKey(between(0, 10));
    public static final Key<UniUjoBase,String>    PRO_P2 = f.newKey(regexp("T.*T"));
    public static final Key<UniUjoBase,Date>      PRO_P3 = f.newKey(past());
    public static final ListKey<UniUjoBase,Float> PRO_P4 = f.newListKey();
    
    // Lock the Key factory
    static { f.lock(); }

    /** Data */
    protected Object[] data;

    /** Return all direct Keys (an implementation from hhe Ujo API) */
    @Override
    public KeyList<?> readKeys() {
        return f.getKeys();
    }

    @Override
    public Object readValue(Key property) {
        return data==null ? data : data[property.getIndex()];
    }

    @Override
    public void writeValue(Key property, Object value) {
        if (data==null) {
            data = new Object[readKeys().size()];
        }
        data[property.getIndex()] = value;
    }

    @Override
    public boolean readAuthorization(UjoAction action, Key property, Object value) {
        return true;
    }

    public UjoPropertyList readProperties() {
        return new UjoPropertyListImpl(readKeys());
    }



}
