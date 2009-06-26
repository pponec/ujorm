/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t005_attrib2;

import org.ujoframework.UjoProperty;
import org.ujoframework.core.annot.XmlAttribute;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.extensions.UjoAction;
import org.ujoframework.implementation.map.MapUjo;
import static org.ujoframework.extensions.UjoAction.*;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class AtrPersonMap extends MapUjo  {


    public static final UjoProperty<AtrPersonMap, String> NAME_ELEM = newProperty("name", String.class);
    @XmlAttribute
    public static final UjoProperty<AtrPersonMap, String> NAME_ATTR = newProperty("name", String.class);
    public static final ListProperty<AtrPersonMap, AtrPersonMap> CHILDS = newListProperty("child", AtrPersonMap.class);
    
    
    @SuppressWarnings("deprecation")
    public boolean readAuthorization(final UjoAction action, final UjoProperty property, final Object value) {
        
        switch(action.getType()) {
            case ACTION_XML_ELEMENT:
                return property!=NAME_ATTR;
            default:
                return super.readAuthorization(action, property, value);
        }
    }
    
}
