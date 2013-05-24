/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm_back.implementation.xml.t001_simple;

import java.util.Date;
import java.util.HashMap;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.UjoProperty;
import org.ujorm.core.UjoManager;
import org.ujorm.UjoPropertyList;
import org.ujorm.extensions.Property;
import org.ujorm.UjoAction;
import org.ujorm.extensions.UjoTextable;


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

    public Object readValue(Key property) {
        return data.get(property);
    }

    public void writeValue(Key property, Object value) {
        data.put(property, value);
    }

    public UjoPropertyList readProperties() {
        return UjoManager.getInstance().readProperties(getClass());
    }

    public boolean readAuthorization(UjoAction action, Key property, Object value) {
        return true;
    }

    public void writeValueString(final Key property, final String value, final Class type, final UjoAction action) {
        final Object valueObj = UjoManager.getInstance().decodeValue(type!=null ? type : property.getType(), value);
        writeValue(property, valueObj);
    }

    @SuppressWarnings("unchecked")
    public String readValueString(final Key property, final UjoAction action) {
        final Object value  = readValue(property);
        final String result = UjoManager.getInstance().encodeValue(value, false);
        return result;
    }

    public KeyList readKeys() {
        return readProperties();
    }



}
