/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2013, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.quick.domains;

import org.ujorm.Key;
import org.ujorm.core.KeyFactory;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class SmartUjoCompany extends SmartUjoImpl {
    private static final KeyFactory<SmartUjoCompany> f = newFactory(SmartUjoCompany.class);
    
    public static final Key<SmartUjoCompany, SmartUjoChild> DIRECTOR = f.newKey("director");
    
    // --- Mandatory initializaton ---
    static {
        f.lock();
    }
    
}
