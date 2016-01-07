/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml.t006_attrib3;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.core.annot.*;
import org.ujorm.implementation.quick.QuickUjo;


/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class AtrPerson extends QuickUjo  {

    public static final Key<AtrPerson, String>  NAME = newKey("Name");
    @XmlAttribute
    public static final Key<AtrPerson, Boolean> MALE = newKey("Male");
    @XmlAttribute
    public static final Key<AtrPerson, Date>   BIRTH = newKey("Birth");
    public static final ListKey<AtrPerson, AtrPerson> CHILDREN = newListKey("Child");
    
    
}
