/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml.t007_body;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.core.annot.XmlElementBody;
import org.ujorm.implementation.map.MapUjo;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class AtrPerson extends MapUjo  {
    
    public static final Key<AtrPerson, String>  NAME = newKey("Name");
    @XmlElementBody
    public static final Key<AtrPerson, Boolean> MALE = newKey("Male");
    public static final Key<AtrPerson, Date>   BIRTH = newKey("Birth");
    public static final ListKey<AtrPerson, AtrPerson> CHILDS = newListKey("Child");
     
}
