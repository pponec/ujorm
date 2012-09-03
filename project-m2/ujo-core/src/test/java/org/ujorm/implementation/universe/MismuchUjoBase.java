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
import org.ujorm.core.KeyFactory;
import org.ujorm.extensions.AbstractUjo;
import org.ujorm.ListKey;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class MismuchUjoBase extends AbstractUjo {

    private static Class<MismuchUjoBase> WRONG_CLASS = (Class<MismuchUjoBase>) (Object) UniUjoBaseTest.class;
    /** Factory */
    private static final KeyFactory<MismuchUjoBase> f = KeyFactory.Builder.get(WRONG_CLASS);
    
    public static final Key<MismuchUjoBase,Long>      PRO_P0 = f.newKey();
    public static final Key<MismuchUjoBase,Integer>   PRO_P1 = f.newKey();
    public static final Key<MismuchUjoBase,String>    PRO_P2 = f.newKey();
    public static final Key<MismuchUjoBase,Date>      PRO_P3 = f.newKey();
    public static final ListKey<MismuchUjoBase,Float> PRO_P4 = f.newListKey();

    // Lock the Key factory
    static { f.lock(); }


    /** Data */
    protected Object[] data;

    @Override
    public KeyList<?> readKeys() {
        return f.getKeys();
    }

    public Object readValue(Key property) {
        return data==null ? data : data[property.getIndex()];
    }

    public void writeValue(Key property, Object value) {
        if (data==null) {
            data = new Object[readKeys().size()];
        }
        data[property.getIndex()] = value;
    }

}
