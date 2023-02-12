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
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public interface UniUjoInterfaceChild extends UniUjoInterface {
    /** The factory will be an immutable object after locking */
    KeyFactory<UniUjoInterfaceChild> $factory2
            = KeyFactory.CamelBuilder.get(UniUjoInterfaceChild.class);

    Key <UniUjoInterfaceChild, Long>    PRO_P5 = $factory2.newKey();
    Key <UniUjoInterfaceChild, Integer> PRO_P6 = $factory2.newKey();
    Key <UniUjoInterfaceChild, String>  PRO_P7 = $factory2.newKey();
    Key <UniUjoInterfaceChild, Date>    PRO_P8 = $factory2.newKey();
    ListKey<UniUjoInterfaceChild,Float> PRO_P9 = $factory2.newListKey();

    /** Size of the all keys and lock internal factory. */
    int KEY_SIZE = $factory2.lockAndSize();

}
