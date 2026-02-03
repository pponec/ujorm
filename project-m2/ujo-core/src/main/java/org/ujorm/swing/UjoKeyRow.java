/*
 *  Copyright 2007-2026 Pavel Ponec
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

import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoActionImpl;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.UjoTextable;
import org.ujorm.implementation.quick.SmartUjo;

/**
 * An implementation of TableModel for List of Ujo objects.
 * <br>An typical usage is an preview of Key list of the one Ujo object including its values.
 *
 * @author Pavel Ponec
 */
@SuppressWarnings("unchecked")
public class UjoKeyRow extends SmartUjo<UjoKeyRow> {

     private static final KeyFactory<UjoKeyRow> f = KeyFactory.CamelBuilder.get(UjoKeyRow.class);

    /** Index of key */
    public static final Key<UjoKeyRow,Integer> P_INDEX   = f.newKey("Index");
    /** Name of key */
    public static final Key<UjoKeyRow,String> P_NAME     = f.newKey("Name");
    /** Type of key */
    public static final Key<UjoKeyRow,Class>  P_TYPE     = f.newKey("Class");
    /** Class name without packages */
    public static final Key<UjoKeyRow,String> P_TYPENAME = f.newKey("Type");
    /** Value */
    public static final Key<UjoKeyRow,Object> P_VALUE    = f.newKey("Value");
    /** Text Value */
    public static final Key<UjoKeyRow,String> P_TEXT     = f.newKey("Text");
    /** Default Value */
    public static final Key<UjoKeyRow,Object> P_DEFAULT  = f.newKey("Default");
    /** A user column can be used in table renderer for any purpose */
    public static final Key<UjoKeyRow,Object> P_USER1    = f.newKey("User1");
    /** A user column can be used in table renderer for any purpose */
    public static final Key<UjoKeyRow,Object> P_USER2    = f.newKey("User2");

    static { f.lock(); }

    final protected Ujo content;
    final protected Key key;

    public UjoKeyRow(Ujo content, Key key) {
        this.content = content;
        this.key = key;
    }

    /** Write value */
    @Override
    public void writeValue(Key aProperty, Object value) {
        if (aProperty==P_VALUE) {
            content.writeValue(key, value);
        } else if (aProperty==P_TEXT) {
            UjoManager.getInstance().setText(content, key, (String) value, null, new UjoActionImpl(this));
        } else {
            throw new UnsupportedOperationException("Can't write key " + key);
        }
    }

    /** Write a text value. */
    @Override
    public void writeValueString(Key aProperty, String value, Class subtype, UjoAction action) {
        if (aProperty==P_VALUE) {
            if (content instanceof UjoTextable) {
                ((UjoTextable) content).writeValueString(key, value, subtype, action);
            } else {
                final Object objValue = readUjoManager().decodeValue(key, value, subtype);
                content.writeValue(key, objValue);
            }
        } else {
            throw new UnsupportedOperationException("Can't write key " + key);
        }
    }

    /** Read Value */
    @Override
    public Object readValue(final Key aProperty) {
        if (aProperty==P_INDEX)   { return key.getIndex(); }
        if (aProperty==P_NAME)    { return key.getName(); }
        if (aProperty==P_TYPE)    { return key.getType(); }
        if (aProperty==P_DEFAULT) { return key.getDefault(); }
        if (aProperty==P_VALUE)   { return key.of(content); }
        if (aProperty==P_TEXT)    { return UjoManager.getInstance().getText(content, key, new UjoActionImpl(this)); }
        if (aProperty==P_TYPENAME){
            final String result = key.getType().getName();
            final int i = 1 + result.lastIndexOf('.');
            return result.substring(i);
        }
        throw new UnsupportedOperationException("Can't read key " + key);
    }

    /** Returns an assigned key (a parameter e.g.) */
    public final Key getKey() {
        return key;
    }

    /** Returns an assigned key (a parameter e.g.) */
    @Deprecated
    public final Key getProperty() {
        return getKey();
    }

    /** Property name + value */
    @Override
    public String toString() {
       final String result = key.getName() + ":" + key.of(content);
       return result;
    }

}

