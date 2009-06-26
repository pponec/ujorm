/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t005_attrib2;

import org.ujoframework.UjoProperty;
import org.ujoframework.core.annot.XmlAttribute;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.array.ArrayUjo;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class AtrPersonArray extends ArrayUjo  {


    protected static int propertyCount = ArrayUjo.propertyCount;

    public static final UjoProperty<AtrPersonArray, String> NAME_ELEM = newProperty("name", String.class, propertyCount++);
    @XmlAttribute
    public static final UjoProperty<AtrPersonArray, String> NAME_ATTR = newProperty("name", String.class, propertyCount++);
    public static final ListProperty<AtrPersonArray, AtrPersonArray> CHILDS = newListProperty("child", AtrPersonArray.class, propertyCount++);
    
    @Override
    public int readPropertyCount() {
        return propertyCount;
    }    
    
    
}
