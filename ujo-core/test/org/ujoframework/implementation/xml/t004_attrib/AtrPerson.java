/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t004_attrib;

import java.util.Date;
import javax.xml.bind.annotation.XmlAttribute;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.UjoAction;
import org.ujoframework.implementation.map.MapPropertyList;
import org.ujoframework.implementation.map.MapUjo;
import org.ujoframework.implementation.map.MapProperty;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class AtrPerson extends MapUjo  {
    
    public static final MapProperty<AtrPerson, String>  NAME = newProperty("Name", String.class );
    @XmlAttribute
    public static final MapProperty<AtrPerson, Boolean> MALE = newProperty("Male", Boolean.class);
    public static final MapProperty<AtrPerson, Date>   BIRTH = newProperty("Birth", Date.class  );
    public static final MapPropertyList<AtrPerson, AtrPerson> CHILDS = newPropertyList("Child", AtrPerson.class);
     
}
