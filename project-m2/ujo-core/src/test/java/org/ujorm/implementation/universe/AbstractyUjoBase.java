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
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.KeyFactory;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
abstract public class AbstractyUjoBase implements Ujo {

    /** Factory */
    private static final KeyFactory<AbstractyUjoBase> f = KeyFactory.Builder.get(AbstractyUjoBase.class);

    public static final Key<AbstractyUjoBase,Long>      PRO_P0 = f.newKey();
    public static final Key<AbstractyUjoBase,Integer>   PRO_P1 = f.newKey();
    public static final Key<AbstractyUjoBase,String>    PRO_P2 = f.newKey();
    public static final Key<AbstractyUjoBase,Date>      PRO_P3 = f.newKey();
    public static final ListKey<AbstractyUjoBase,Float> PRO_P4 = f.newListKey();

    /** Create a list of key */
    private static final KeyList keys = f.getKeys();

    /** Data */
    protected Object[] data;

    /** Return all direct Keys (an implementation from hhe Ujo API) */
    @Override
    public KeyList readKeys() {
        return keys;
    }

    @Override
    public Object readValue(Key key) {
        return data==null ? data : data[key.getIndex()];
    }

    @Override
    public void writeValue(Key key, Object value) {
        if (data==null) {
            data = new Object[readKeys().size()];
        }
        data[key.getIndex()] = value;
    }

    @Override
    public boolean readAuthorization(UjoAction action, Key key, Object value) {
        return true;
    }



}
