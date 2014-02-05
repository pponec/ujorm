/*
 * UjoCSV.java
 *
 * Created on 3. May 2008, 21:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.core.ujos;

import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.quick.QuickUjoMid;

/**
 * UjoCSV
 * @author Pavel Ponec
 */
public class UjoName extends QuickUjoMid<UjoName> {

    private static final KeyFactory<UjoName> f = newFactory(UjoName.class);

    public static final Key<UjoName, UjoName> S1 = f.newKey();
    public static final Key<UjoName, UjoName> S2 = f.newKey();

    static {
        f.lock();
    }

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
