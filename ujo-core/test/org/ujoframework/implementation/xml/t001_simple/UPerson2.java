/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t001_simple;

import java.util.Date;
import java.util.HashMap;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.extensions.Property;
import org.ujoframework.extensions.UjoAction;
import org.ujoframework.extensions.UjoTextable;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
@SuppressWarnings("unchecked")
public class UPerson2 implements Ujo, UjoTextable  {

    public static final Property<UPerson2,String>  NAME = Property.newInstance("Name", String.class );
    public static final Property<UPerson2,Boolean> MALE = Property.newInstance("Male", Boolean.class);
    public static final Property<UPerson2,Date>   BIRTH = Property.newInstance("Birth", Date.class  );

    private HashMap data = new HashMap();

    public Object readValue(UjoProperty property) {
        Object result = data.get(property);
        return result!=null ? result : property.getDefault() ;
    }

    public void writeValue(UjoProperty property, Object value) {
        data.put(property, value);
    }

    public UjoProperty[] readProperties() {
        return UjoManager.getInstance().readProperties(getClass());
    }

    public boolean readAuthorization(UjoAction action, UjoProperty property, Object value) {
        return true;
    }

    public void writeValueString(final UjoProperty property, final String value, final Class type, final UjoAction action) {
        final Object valueObj = UjoManager.getInstance().decodeValue(type!=null ? type : property.getType(), value);
        writeValue(property, valueObj);
    }

    @SuppressWarnings("unchecked")
    public String readValueString(final UjoProperty property, final UjoAction action) {
        final Object value  = readValue(property);
        final String result = UjoManager.getInstance().encodeValue(value, false);
        return result;
    }

}
