/*
 *  Copyright 2007-2022 Pavel Ponec
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

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.KeyList;

/**
 * General Ujo Manager
 * @author Pavel Ponec
 */
public class UjoManagerCache {

    /** A keys cache. */
    final private HashMap<Class, KeyList> propertiesCache;

    /** A XML <strong>element body</strong> cache */
    private HashMap<Class, Key> xmlBodyCache;
    
    /** A XML <strong>attribute</strong> cache. */
    private HashSet<Key> attributesCache;

    /** A transient <strong>attribute</strong> cache. */
    private HashSet<Key> transientCache = null;
    
    /** Constructor. */
    public UjoManagerCache(boolean recursion, Ujo ... ujos) {
        this.propertiesCache = new HashMap<>();
    }
    
    /** Returns true, if the class is abstract. */
    protected boolean isAbstract(Class type) {
        final boolean result = Modifier.isAbstract(type.getModifiers() );
        return result;
    }

    /** Is the key an XML attribute? */
    public final boolean isXmlAttribute(final Key key) {
        final boolean result 
            =  attributesCache!=null
            && attributesCache.contains(key)
            ;
        return result;
    }

    /** Returns a Element body of the class or the null if no key was found. */
    public final Key getXmlElementBody(final Class type) {

        final Key result
            = xmlBodyCache!=null
            ? xmlBodyCache.get(type)
            : null
            ;
        return result;
    }

    /** Is the key an Transient? */
    public final boolean isTransientProperty(final Key key) {
        final boolean result = transientCache!=null && transientCache.contains(key);
        return result;
    }
}
