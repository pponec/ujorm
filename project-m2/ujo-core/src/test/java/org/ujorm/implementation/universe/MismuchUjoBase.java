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
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
import org.ujorm.core.PropertyFactory;
import org.ujorm.extensions.AbstractUjo;
import org.ujorm.extensions.ListUjoProperty;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class MismuchUjoBase extends AbstractUjo {

    private static Class<MismuchUjoBase> WRONG_CLASS = (Class<MismuchUjoBase>) (Object) UniUjoBaseTest.class;
    /** Factory */
    private static final PropertyFactory<MismuchUjoBase> pf = PropertyFactory.getInstance(WRONG_CLASS);
    
    public static final UjoProperty<MismuchUjoBase,Long>      PRO_P0 = pf.newProperty();
    public static final UjoProperty<MismuchUjoBase,Integer>   PRO_P1 = pf.newProperty();
    public static final UjoProperty<MismuchUjoBase,String>    PRO_P2 = pf.newProperty();
    public static final UjoProperty<MismuchUjoBase,Date>      PRO_P3 = pf.newProperty();
    public static final ListUjoProperty<MismuchUjoBase,Float> PRO_P4 = pf.newListProperty();

    static {
        pf.lock();
    }

    /** Data */
    protected Object[] data;

    @Override
    public UjoPropertyList<?> readProperties() {
        return pf.getPropertyList();
    }

    public Object readValue(UjoProperty property) {
        return data==null ? data : data[property.getIndex()];
    }

    public void writeValue(UjoProperty property, Object value) {
        if (data==null) {
            data = new Object[readProperties().size()];
        }
        data[property.getIndex()] = value;
    }

}
