/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t007_body;

import java.util.Date;
import org.ujoframework.core.annot.XmlElementBody;
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.map.MapUjo;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class AtrPerson3 extends MapUjo  {
    
    public static final Property<AtrPerson3, String>  NAME = newProperty("Name", String.class );
    public static final Property<AtrPerson3, Boolean> MALE = newProperty("Male", Boolean.class);
    @XmlElementBody
    public static final Property<AtrPerson3, Date>   BIRTH = newProperty("Birth", Date.class  );
     
}
