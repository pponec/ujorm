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
import org.ujorm.extensions.Property;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class UniUjoBasePlain implements Ujo {

    /** Factory */
    protected static final KeyFactory<UniUjoBasePlain> pf = KeyFactory.Builder.get(UniUjoBasePlain.class);

    /** Keys */
    public static final Key<UniUjoBasePlain,Long>    PRO_P0 = (Property<UniUjoBasePlain,Long>) (Object) pf.newKey();
    public static final Key<UniUjoBasePlain,Integer> PRO_P1 = pf.newKey();
    public static final Key<UniUjoBasePlain,String>  PRO_P2 = pf.newKey();
    public static final Key<UniUjoBasePlain,Date>    PRO_P3 = pf.newKey();
    public static final Key<UniUjoBasePlain,Float>   PRO_P4 = pf.newKey();

    static {
        pf.lock();
    }

    /** Data */
    protected Object[] data;

    @Override
    public KeyList readKeys() {
        return pf.getKeys();
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
