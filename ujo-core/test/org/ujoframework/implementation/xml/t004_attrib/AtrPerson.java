/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. èerven 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t004_attrib;

import java.util.Date;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.UjoAction;
import org.ujoframework.implementation.map.MapPropertyList;
import org.ujoframework.implementation.map.MapUjo;
import org.ujoframework.implementation.map.MapProperty;
import static org.ujoframework.extensions.UjoAction.*;


/**
 * An UnifiedDataObject Imlpementation
 * @author pavel
 */
public class AtrPerson extends MapUjo  {
    
    public static final MapProperty<AtrPerson, String>  NAME = newProperty("Name", String.class );
    public static final MapProperty<AtrPerson, Boolean> MALE = newProperty("Male", Boolean.class);
    public static final MapProperty<AtrPerson, Date>   BIRTH = newProperty("Birth", Date.class  );
    public static final MapPropertyList<AtrPerson, AtrPerson> CHILDS = newPropertyList("Child", AtrPerson.class);
    
    //@Override
    public boolean XXreadAuthorization(final UjoAction action, final UjoProperty property, final Object value) {
        
        boolean attribute = true;
        
        switch(action.getType()) {
            case ACTION_XML_ELEMENT:
                return property==MALE ? (!attribute)
                : property==BIRTH ? (!attribute)
                : true;
            default:
                return super.readAuthorization(action, property, value);
        }
    }
    
    @Override
    public boolean readAuthorization(UjoAction action, UjoProperty property, Object value) {
        switch(action.getType()) {
            case ACTION_XML_ELEMENT: 
                return property!=MALE;
            default: return super.readAuthorization(action, property, value);
        }
    }
    
}
