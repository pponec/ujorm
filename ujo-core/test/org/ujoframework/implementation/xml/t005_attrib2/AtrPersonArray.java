/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. èerven 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t005_attrib2;

import org.ujoframework.UjoProperty;
import org.ujoframework.core.annot.XmlAttribute;
import org.ujoframework.extensions.UjoAction;
import org.ujoframework.implementation.array.ArrayProperty;
import org.ujoframework.implementation.array.ArrayPropertyList;
import org.ujoframework.implementation.array.ArrayUjo;
import static org.ujoframework.extensions.UjoAction.*;


/**
 * An UnifiedDataObject Imlpementation
 * @author pavel
 */
public class AtrPersonArray extends ArrayUjo  {


    protected static int propertyCount = ArrayUjo.propertyCount;

    public static final ArrayProperty<AtrPersonArray, String> NAME_ELEM = newProperty("name", String.class, propertyCount++);
    @XmlAttribute
    public static final ArrayProperty<AtrPersonArray, String> NAME_ATTR = newProperty("name", String.class, propertyCount++);
    public static final ArrayPropertyList<AtrPersonArray, AtrPersonArray> CHILDS = newPropertyList("child", AtrPersonArray.class, propertyCount++);
    
    @Override
    public int readPropertyCount() {
        return propertyCount;
    }    
    
    
}
