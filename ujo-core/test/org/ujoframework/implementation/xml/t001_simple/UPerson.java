/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. èerven 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t001_simple;

import java.util.Date;
import org.ujoframework.implementation.map.MapUjo;
import org.ujoframework.implementation.map.MapProperty;


/**
 * An UnifiedDataObject Imlpementation
 * @author pavel
 */
public class UPerson extends MapUjo  {

    public static final MapProperty<UPerson,String>  NAME = newProperty("Name", String.class );
    public static final MapProperty<UPerson,Boolean> MALE = newProperty("Male", Boolean.class);
    public static final MapProperty<UPerson,Date>   BIRTH = newProperty("Birth", Date.class  );    
}
