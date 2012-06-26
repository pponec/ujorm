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
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
import org.ujorm.core.PropertyFactory;
import org.ujorm.extensions.ListUjoProperty;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class ImplUjoBase extends AbstractyUjoBase {

    /** Factory */
    protected static final PropertyFactory<ImplUjoBase> pf = PropertyFactory.Builder.get(ImplUjoBase.class);
    
    public static final UjoProperty <ImplUjoBase, Long>    PRO_P5 = pf.newProperty("P5");
    public static final UjoProperty <ImplUjoBase, Integer> PRO_P6 = pf.newProperty("P6");
    public static final UjoProperty <ImplUjoBase, String>  PRO_P7 = pf.newProperty("P7");
    public static final UjoProperty <ImplUjoBase, Date>    PRO_P8 = pf.newProperty("P8");
    public static final ListUjoProperty<ImplUjoBase,Float> PRO_P9 = pf.newListProperty("P9");

    static {
        pf.lock();
    }

    @Override
    public UjoPropertyList<?> readProperties() {
        return pf.getPropertyList();
    }


}
