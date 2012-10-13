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
public interface UniUjoInterfaceChild extends UniUjoInterface {

    /** Factory */
    public static final KeyFactory<UniUjoInterfaceChild> $factory2
            = KeyFactory.CamelBuilder.get(UniUjoInterfaceChild.class);
    
    public static final Key <UniUjoInterfaceChild, Long>    PRO_P5 = $factory2.newKey();
    public static final Key <UniUjoInterfaceChild, Integer> PRO_P6 = $factory2.newKey();
    public static final Key <UniUjoInterfaceChild, String>  PRO_P7 = $factory2.newKey();
    public static final Key <UniUjoInterfaceChild, Date>    PRO_P8 = $factory2.newKey();
    public static final ListKey<UniUjoInterfaceChild,Float> PRO_P9 = $factory2.newListKey();
    
    /** Size of the all keys and lock internal factory. */
    public static final int KEY_SIZE = $factory2.lockAndSize();

}
