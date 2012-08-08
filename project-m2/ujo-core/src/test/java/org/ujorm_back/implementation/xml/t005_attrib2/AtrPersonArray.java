/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm_back.implementation.xml.t005_attrib2;

import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.array.ArrayUjo;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class AtrPersonArray extends ArrayUjo  {


    protected static int propertyCount = ArrayUjo.propertyCount;

    public static final Property<AtrPersonArray, String> NAME_ELEM = newProperty("name", String.class, propertyCount++);
    @XmlAttribute
    public static final Property<AtrPersonArray, String> NAME_ATTR = newProperty("name", String.class, propertyCount++);
    public static final ListProperty<AtrPersonArray, AtrPersonArray> CHILDS = newListProperty("child", AtrPersonArray.class, propertyCount++);
    
    @Override
    public int readPropertyCount() {
        return propertyCount;
    }    
    
    
}
