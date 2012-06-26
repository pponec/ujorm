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
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
import org.ujorm.core.PropertyFactory;
import org.ujorm.extensions.ListUjoProperty;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
abstract public class AbstractyUjoBase implements Ujo {

    /** Factory */
    private static final PropertyFactory<AbstractyUjoBase> APF = PropertyFactory.Builder.get(AbstractyUjoBase.class);

    public static final UjoProperty<AbstractyUjoBase,Long>      PRO_P0 = APF.newProperty();
    public static final UjoProperty<AbstractyUjoBase,Integer>   PRO_P1 = APF.newProperty();
    public static final UjoProperty<AbstractyUjoBase,String>    PRO_P2 = APF.newProperty();
    public static final UjoProperty<AbstractyUjoBase,Date>      PRO_P3 = APF.newProperty();
    public static final ListUjoProperty<AbstractyUjoBase,Float> PRO_P4 = APF.newListProperty();

    static {
        APF.lock();
    }

    /** Data */
    protected Object[] data;

    @Override
    public UjoPropertyList<?> readProperties() {
        return APF.getPropertyList();
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

    public boolean readAuthorization(UjoAction action, UjoProperty property, Object value) {
        return true;
    }



}
