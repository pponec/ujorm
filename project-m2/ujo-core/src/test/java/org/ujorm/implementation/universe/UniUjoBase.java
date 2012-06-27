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
import org.ujorm.core.PropertyFactory;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class UniUjoBase implements Ujo {

    /** Factory */
    private static final PropertyFactory<UniUjoBase> factory
            = PropertyFactory.CamelBuilder.get(UniUjoBase.class);
    
    public static final UjoProperty<UniUjoBase,Long>      PRO_P0 = factory.newProperty();
    public static final UjoProperty<UniUjoBase,Integer>   PRO_P1 = factory.newProperty();
    public static final UjoProperty<UniUjoBase,String>    PRO_P2 = factory.newProperty();
    public static final UjoProperty<UniUjoBase,Date>      PRO_P3 = factory.newProperty();
    public static final ListUjoProperty<UniUjoBase,Float> PRO_P4 = factory.newListProperty();

    static {
        factory.lock();
    }

    /** Data */
    protected Object[] data;

    @Override
    public UjoPropertyList<?> readProperties() {
        return factory.getPropertyList();
    }

    @Override
    public Object readValue(UjoProperty property) {
        return data==null ? data : data[property.getIndex()];
    }

    @Override
    public void writeValue(UjoProperty property, Object value) {
        if (data==null) {
            data = new Object[readProperties().size()];
        }
        data[property.getIndex()] = value;
    }

    @Override
    public boolean readAuthorization(UjoAction action, UjoProperty property, Object value) {
        return true;
    }



}
