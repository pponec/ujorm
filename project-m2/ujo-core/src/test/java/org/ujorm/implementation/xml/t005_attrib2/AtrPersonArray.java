/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml.t005_attrib2;

import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.implementation.quick.QuickUjo;


/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class AtrPersonArray extends QuickUjo  {

    /** Factory */
    private static final KeyFactory<AtrPersonArray> f = newFactory(AtrPersonArray.class);

    public static final Key<AtrPersonArray, String> NAME_ELEM = f.newKey("name");
    @XmlAttribute
    public static final Key<AtrPersonArray, String> NAME_ATTR = f.newKey("name");
    public static final ListKey<AtrPersonArray, AtrPersonArray> CHILDREN = f.newListKey("child");

    static {
        f.lock();
    }
    
}
