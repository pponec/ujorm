/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml.t001_simple;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.implementation.map.MapUjo;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class UPerson extends MapUjo  {

    public static final Key<UPerson,String>  NAME = newProperty("Name", String.class );
    public static final Key<UPerson,Boolean> MALE = newProperty("Male", Boolean.class);
    public static final Key<UPerson,Date>   BIRTH = newProperty("Birth", Date.class  );
}
