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
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.UjoPropertyList;
import org.ujorm.core.KeyFactory;
import org.ujorm.ListKey;
import org.ujorm.core.UjoPropertyListImpl;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class ImplUjoBase extends AbstractyUjoBase {

    /** Factory */
    protected static final KeyFactory<ImplUjoBase> f = KeyFactory.Builder.get(ImplUjoBase.class);
    
    public static final Key <ImplUjoBase, Long>    PRO_P5 = f.newKey("P5");
    public static final Key <ImplUjoBase, Integer> PRO_P6 = f.newKey("P6");
    public static final Key <ImplUjoBase, String>  PRO_P7 = f.newKey("P7");
    public static final Key <ImplUjoBase, Date>    PRO_P8 = f.newKey("P8");
    public static final ListKey<ImplUjoBase,Float> PRO_P9 = f.newListKey("P9");

    // Lock the Key factory
    static { f.lock(); }

    /** An optional method for a better performance.
     * @return Return all direct Keys (An implementation from hhe Ujo API)
     */
    @Override
    public KeyList<?> readKeys() {
        return f.getKeyList();
    }

    public UjoPropertyList readProperties() {
        return new UjoPropertyListImpl(readKeys());
    }


}
