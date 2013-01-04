/*
 *  Copyright 2007-2009 Pavel Ponec
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

package org.ujorm.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.extensions.UjoTextable;

/**
 * A Core of a UjoService.
 * @author Pavel Ponec
 */
abstract public class UjoService<UJO extends Ujo> {
    
    /** Undefined text VALUE */
    public static final String UNDEFINED = new String("U");
    
    /** Charset UTF-8 */
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    
    /** Basic UJO Class */
    final private Class<UJO> ujoClass;
    /** Keys */
    private Key[] keys;
    /** Is ujoClass textable */
    final private boolean textable;
    
    /** Special UjoManager. Value null means a DEFAULT value */
    private UjoManager ujoManager = UjoManager.getInstance();
    
    /** Creates a new instance of UjoService */
    public UjoService(Class<UJO> ujoClass) {
        this(ujoClass, (Key[]) null);
    }
    
    /** Creates a new instance of UjoService */
    public UjoService(Class<UJO> ujoClass, Key ... keys) {
        this.ujoClass   = ujoClass;
        this.keys = keys;
        this.textable   = UjoTextable.class.isAssignableFrom(ujoClass);
    }
    
    /** Returns BASIC class */
    final public Class<UJO> getUjoClass() {
        return ujoClass;
    }
    
    /** Returns a TRUE value if attribute <strong>ujoClass</strong> is textable. */
    final public boolean isTextable() {
        return textable;
    }
    
    public final UjoManager getUjoManager() {
        return ujoManager;
    }
    
    public UjoService setUjoManager(UjoManager ujoManager) {
        this.ujoManager = ujoManager;
        return this;
    }
    
    /** Get required keys */
    public Key[] getProperties() {
        if (keys==null) {
            keys = ujoManager.readProperties(getUjoClass()).toArray();
        }
        return keys;
    }
    
    /** Returns TEXT */
    public String getText(final UJO ujo, final Key<? super Ujo,?> prop, final Object value, final UjoAction action) {
        final String result = textable
        ? ((UjoTextable)ujo).readValueString(prop, action)
        : ujoManager.encodeValue(value!=UNDEFINED ? value : prop.of(ujo), false)
        ;
        return result;
    }
    
    /** Returns TEXT */
    public void setText(final UJO ujo, final Key prop, final Class type, final String value, final UjoAction action) {
        if (textable) {
            ((UjoTextable)ujo).writeValueString(prop, value, type, action);
        } else {
            final Object o = ujoManager.decodeValue(prop, value, type);
            UjoManager.setValue(ujo, prop, o);
        }
    }
    
    /** Create a Buffered Output Stream. */
    protected OutputStream getOutputStream(File file) throws FileNotFoundException {
        final OutputStream result = new BufferedOutputStream(new FileOutputStream(file));
        return result;
    }
    
    /** Create a Buffered Input Stream. */
    protected InputStream getInputStream(File file) throws FileNotFoundException {
        final InputStream result = new BufferedInputStream(new FileInputStream(file));
        return result;
    }
    
}
