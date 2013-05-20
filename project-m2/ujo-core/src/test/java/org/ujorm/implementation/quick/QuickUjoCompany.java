/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2013, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.quick;

import org.ujorm.Key;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class QuickUjoCompany extends QuickUjoImpl {
    
    public static final Key<QuickUjoCompany, QuickUjoImplChild> DIRECTOR = newKey("director");

    
    // --- Mandatory initializaton ---
    static {
        init(QuickUjoCompany.class);
    }
    
}
