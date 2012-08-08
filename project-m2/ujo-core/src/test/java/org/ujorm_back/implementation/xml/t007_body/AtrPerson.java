/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm_back.implementation.xml.t007_body;

import java.util.Date;
import org.ujorm.core.annot.XmlElementBody;
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.map.MapUjo;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class AtrPerson extends MapUjo  {
    
    public static final Property<AtrPerson, String>  NAME = newProperty("Name", String.class );
    @XmlElementBody
    public static final Property<AtrPerson, Boolean> MALE = newProperty("Male", Boolean.class);
    public static final Property<AtrPerson, Date>   BIRTH = newProperty("Birth", Date.class  );
    public static final ListProperty<AtrPerson, AtrPerson> CHILDS = newListProperty("Child", AtrPerson.class);
     
}
