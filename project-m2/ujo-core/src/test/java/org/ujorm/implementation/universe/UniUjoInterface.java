/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.universe;

import java.util.Date;
import org.ujorm.*;
import org.ujorm.core.KeyFactory;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public interface UniUjoInterface extends Ujo {

    /** Factory */
    public static final KeyFactory<UniUjoInterface> $factory
            = KeyFactory.CamelBuilder.get(UniUjoInterface.class);
    
    public static final Key<UniUjoInterface,Long>      PRO_P0 = $factory.newKey();
    public static final Key<UniUjoInterface,Integer>   PRO_P1 = $factory.newKey();
    public static final Key<UniUjoInterface,String>    PRO_P2 = $factory.newKey();
    public static final Key<UniUjoInterface,Date>      PRO_P3 = $factory.newKey();
    public static final ListKey<UniUjoInterface,Float> PRO_P4 = $factory.newListKey();
    
    /** Size of the all keys and lock internal factory. */
    public static final int KEY_SIZE = $factory.lockAndSize();

}
