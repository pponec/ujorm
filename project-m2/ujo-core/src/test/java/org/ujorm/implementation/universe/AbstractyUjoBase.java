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
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.core.KeyFactory;
import org.ujorm.ListKey;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
abstract public class AbstractyUjoBase implements Ujo {

    /** Factory */
    private static final KeyFactory<AbstractyUjoBase> APF = KeyFactory.Builder.get(AbstractyUjoBase.class);

    public static final Key<AbstractyUjoBase,Long>      PRO_P0 = APF.newKey();
    public static final Key<AbstractyUjoBase,Integer>   PRO_P1 = APF.newKey();
    public static final Key<AbstractyUjoBase,String>    PRO_P2 = APF.newKey();
    public static final Key<AbstractyUjoBase,Date>      PRO_P3 = APF.newKey();
    public static final ListKey<AbstractyUjoBase,Float> PRO_P4 = APF.newListProperty();

    static {
        APF.lock();
    }

    /** Data */
    protected Object[] data;

    @Override
    public KeyList<?> readKeys() {
        return APF.getKeyList();
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

    public boolean readAuthorization(UjoAction action, Key property, Object value) {
        return true;
    }



}
