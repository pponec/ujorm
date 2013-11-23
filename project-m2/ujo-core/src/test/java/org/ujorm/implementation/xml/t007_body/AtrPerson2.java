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
import org.ujorm.core.annot.XmlElementBody;
import org.ujorm.implementation.quick.QuickUjo;


/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class AtrPerson2 extends QuickUjo  {
    
    @XmlElementBody
    public static final Key<AtrPerson2, String>  NAME = newKey("Name");
    public static final Key<AtrPerson2, Boolean> MALE = newKey("Male");
    public static final Key<AtrPerson2, Date>   BIRTH = newKey("Birth");

}
