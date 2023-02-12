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
public interface UniUjoInterface extends Ujo {
    /** The factory will be an immutable object after locking */
    KeyFactory<UniUjoInterface> $factory
            = KeyFactory.CamelBuilder.get(UniUjoInterface.class);

    Key<UniUjoInterface,Long>      PRO_P0 = $factory.newKey();
    Key<UniUjoInterface,Integer>   PRO_P1 = $factory.newKey();
    Key<UniUjoInterface,String>    PRO_P2 = $factory.newKey();
    Key<UniUjoInterface,Date>      PRO_P3 = $factory.newKey();
    ListKey<UniUjoInterface,Float> PRO_P4 = $factory.newListKey();

    /** Size of the all keys and lock internal factory. */
    int KEY_SIZE = $factory.lockAndSize();

}
