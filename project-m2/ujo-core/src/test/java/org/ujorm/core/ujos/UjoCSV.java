/*
 * UjoCSV.java
 *
 * Created on 3. May 2008, 21:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.core.ujos;

import org.ujorm.extensions.Property;
import org.ujorm.implementation.map.MapUjo;

/**
 *
 * @author Pavel Ponec
 */
public class UjoCSV extends MapUjo{

    public static final Property<UjoCSV, String> P1 = newProperty("P1", String.class);
    public static final Property<UjoCSV, String> P2 = newProperty("P2", String.class);
    public static final Property<UjoCSV, String> P3 = newProperty("P3", String.class);
    
    // ---------------------------
    
    
    private boolean onZeroManager = true;
    
    public boolean isOnZeroManager() {
        return onZeroManager;
    }
    
    public void setOnZeroManager(boolean onZeroManager) {
        this.onZeroManager = onZeroManager;
    }
    
    //public boolean readAuthorization(ACTION_ZERO_REPLACE, property, result, null) {}
    
   
    
}
