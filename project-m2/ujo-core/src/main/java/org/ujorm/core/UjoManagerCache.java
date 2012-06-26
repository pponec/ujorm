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

package org.ujorm.core;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import org.ujorm.Ujo;
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;

/**
 * General Ujo Manager
 * @author Pavel Ponec
 */
public class UjoManagerCache {

    /** A properties cache. */
    final private HashMap<Class, UjoPropertyList> propertiesCache;

    /** A XML <strong>element body</strong> cache */
    private HashMap<Class, UjoProperty> xmlBodyCache;
    
    /** A XML <strong>attribute</strong> cache. */
    private HashSet<UjoProperty> attributesCache;

    /** A transient <strong>attribute</strong> cache. */
    private HashSet<UjoProperty> transientCache = null;
    
    /** Constructor. */
    public UjoManagerCache(boolean recursion, Ujo ... ujos) {
        this.propertiesCache = new HashMap<Class, UjoPropertyList>();
    }
    
    /** Returns true, if the class is abstract. */
    protected boolean isAbstract(Class type) {
        final boolean result = Modifier.isAbstract(type.getModifiers() );
        return result;
    }

    /** Is the property an XML attribute? */
    public final boolean isXmlAttribute(final UjoProperty property) {
        final boolean result 
            =  attributesCache!=null
            && attributesCache.contains(property)
            ;
        return result;
    }

    /** Returns a Element body of the class or the null if no property was found. */
    public final UjoProperty getXmlElementBody(final Class type) {

        final UjoProperty result
            = xmlBodyCache!=null
            ? xmlBodyCache.get(type)
            : null
            ;
        return result;
    }

    /** Is the property an Transient? */
    public final boolean isTransientProperty(final UjoProperty property) {
        final boolean result = transientCache!=null && transientCache.contains(property);
        return result;
    }
}
