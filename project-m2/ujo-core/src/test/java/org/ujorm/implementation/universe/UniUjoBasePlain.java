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
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class UniUjoBasePlain implements Ujo {

    /** Factory */
    protected static final KeyFactory<UniUjoBasePlain> pf = KeyFactory.Builder.get(UniUjoBasePlain.class);

    /** Properties */
    public static final Key<UniUjoBasePlain,Long>    PRO_P0 = (Property<UniUjoBasePlain,Long>) (Object) pf.newKey();
    public static final Key<UniUjoBasePlain,Integer> PRO_P1 = pf.newKey();
    public static final Key<UniUjoBasePlain,String>  PRO_P2 = pf.newKey();
    public static final Key<UniUjoBasePlain,Date>    PRO_P3 = pf.newKey();
    public static final Key<UniUjoBasePlain,Float>   PRO_P4 = pf.newKey();

    /** Data */
    protected Object[] data;

    @Override
    public KeyList<?> readProperties() {
        return pf.getPropertyList();
    }

    public Object readValue(Key property) {
        return data==null ? data : data[property.getIndex()];
    }

    public void writeValue(Key property, Object value) {
        if (data==null) {
            data = new Object[readProperties().size()];
        }
        data[property.getIndex()] = value;
    }

    public boolean readAuthorization(UjoAction action, Key property, Object value) {
        return true;
    }



}
