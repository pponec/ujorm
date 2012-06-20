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
import org.ujorm.extensions.AbstractUjo;
import org.ujorm.extensions.Property;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class UniUjoBasePlain implements Ujo {

    /** Factory */
    protected static final PropertyFactory<UniUjoBasePlain> pf = PropertyFactory.getInstance(UniUjoBasePlain.class);

    /** Properties */
    public static final Property<UniUjoBasePlain,Long>    PRO_P0 = (Property<UniUjoBasePlain,Long>) (Object) pf.newProperty();
    public static final Property<UniUjoBasePlain,Integer> PRO_P1 = pf.newProperty();
    public static final Property<UniUjoBasePlain,String>  PRO_P2 = pf.newProperty();
    public static final Property<UniUjoBasePlain,Date>    PRO_P3 = pf.newProperty();
    public static final Property<UniUjoBasePlain,Float>   PRO_P4 = pf.newProperty();

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

    public boolean readAuthorization(UjoAction action, UjoProperty property, Object value) {
        return true;
    }



}
