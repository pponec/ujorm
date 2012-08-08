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
import org.ujorm.extensions.Property;
import org.ujorm.implementation.map.MapUjo;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class UPerson extends MapUjo  {

    public static final Property<UPerson,String>  NAME = newProperty("Name", String.class );
    public static final Property<UPerson,Boolean> MALE = newProperty("Male", Boolean.class);
    public static final Property<UPerson,Date>   BIRTH = newProperty("Birth", Date.class  );
}
