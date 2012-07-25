/*
 *  Copyright 2007-2010 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */   

package org.ujorm.swing;

import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.UjoPropertyList;
import org.ujorm.core.UjoActionImpl;
import org.ujorm.core.UjoManager;
import org.ujorm.UjoAction;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.UjoTextable;
import org.ujorm.implementation.map.MapUjoExt;

/**
 * An implementation of TableModel for List of Ujo objects.
 * <br>An typical usage is an preview of Key list of the one Ujo object include values.
 * 
 * @author Pavel Ponec
 */
@SuppressWarnings("unchecked")
public class UjoPropertyRow extends MapUjoExt<UjoPropertyRow> {
    
    /** Index of property */
    public static final Key<UjoPropertyRow,Integer> P_INDEX   = newProperty("Index", Integer.class);
    /** Name of property */
    public static final Key<UjoPropertyRow,String> P_NAME     = newProperty("Name" , String.class);
    /** Type of property */
    public static final Key<UjoPropertyRow,Class>  P_TYPE     = newProperty("Class", Class.class );
    /** Class name without packages */
    public static final Key<UjoPropertyRow,String> P_TYPENAME = newProperty("Type" , String.class);
    /** Value */
    public static final Key<UjoPropertyRow,Object> P_VALUE    = newProperty("Value", Object.class);
    /** Text Value */
    public static final Key<UjoPropertyRow,String> P_TEXT     = newProperty("Text" , String.class);
    /** Default Value */
    public static final Key<UjoPropertyRow,Object> P_DEFAULT  = newProperty("Default", Object.class);
    /** A user column can be used in table renderer for any purpose */
    public static final Key<UjoPropertyRow,Object> P_USER1    = newProperty("User1", Object.class);
    /** A user column can be used in table renderer for any purpose */
    public static final Key<UjoPropertyRow,Object> P_USER2    = newProperty("User2", Object.class);
    
    final protected Ujo content;
    final protected Key property;
    
    public UjoPropertyRow(Ujo content, Key property) {
        this.content = content;
        this.property = property;
    }
    
    /** Write value */
    @Override
    public void writeValue(Key aProperty, Object value) {
        if (aProperty==P_VALUE) {
            content.writeValue(property, value);
        } else if (aProperty==P_TEXT) {
            UjoManager.getInstance().setText(content, property, (String) value, null, new UjoActionImpl(this));
        } else {
            throw new UnsupportedOperationException("Can't write property " + property);
        }
    }
    
    /** Write a text value. */
    @Override
    public void writeValueString(Key aProperty, String value, Class subtype, UjoAction action) {
        if (aProperty==P_VALUE) {
            if (content instanceof UjoTextable) {
                ((UjoTextable) content).writeValueString(property, value, subtype, action);
            } else {
                final Object objValue = readUjoManager().decodeValue(property, value, subtype);
                content.writeValue(property, objValue);
            }
        } else {
            throw new UnsupportedOperationException("Can't write property " + property);
        }
    }
    
    /** Read Value */
    @Override
    public Object readValue(final Key aProperty) {
        if (aProperty==P_INDEX)   { return property.getIndex(); }
        if (aProperty==P_NAME)    { return property.getName(); }
        if (aProperty==P_TYPE)    { return property.getType(); }
        if (aProperty==P_DEFAULT) { return property.getDefault(); }
        if (aProperty==P_VALUE)   { return property.getValue(content); }
        if (aProperty==P_TEXT)    { return UjoManager.getInstance().getText(content, property, new UjoActionImpl(this)); }
        if (aProperty==P_TYPENAME){
            final String result = property.getType().getName();
            final int i = 1 + result.lastIndexOf('.');
            return result.substring(i);
        }
        throw new UnsupportedOperationException("Can't read property " + property);
    }
    
    /** Returns an assigned property (a parameter e.g.) */
    public final Key getProperty() {
        return property;
    }
    
    /** Property name + value */
    @Override
    public String toString() {
       final String result = property.getName() + ":" + property.of(content);
       return result;
    }

}

