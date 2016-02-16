/*
 *  Copyright 2016-2016 Pavel Ponec
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.annot.UjoConverter;

/**
 * Default generic UJO converter from POJO objects and back
 * @author Pavel Ponec
 */
public class DefaultUjoConverter<U extends Ujo> extends XmlAdapter<U, Object> {

    /** Name of the method for an original object */
    protected static final String ORIGINAL_METHOD = "original";

    /** Related package name */
    private final String packag;
    /** A prefix of the Ujo class */
    private final String prefix;
    /** A suffix of the Ujo class */
    private final String suffix;

    /** Default constructor */
    public DefaultUjoConverter() {
        this("generated", "$", "");
    }

    /**
     * Argument constructor
     * @param packag Related package name
     * @param prefix A prefix of the Ujo class
     * @param suffix A suffix of the Ujo class
     */
    public DefaultUjoConverter(String packag, String prefix, String suffix) {
        this.packag = packag;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /** Convert from POJO to UJO */
    @Override
    public U marshal(final Object v) throws IllegalStateException {
        if (v == null || v instanceof Ujo) {
            @SuppressWarnings("unchecked")
            final U result = (U) v;
            return result;
        }
        try {
            Class targetType = null;

            // Required converter from an Annotation:
            final UjoConverter aConverter = v.getClass().getAnnotation(UjoConverter.class);
            if (aConverter != null) {
                @SuppressWarnings("unchecked")
                final DefaultUjoConverter<U> newConverter = (DefaultUjoConverter<U>) aConverter.value().newInstance();
                if (getClass() != newConverter.getClass()) {
                    return newConverter.marshal(v);
                } else {
                    targetType = aConverter.target();
                }
            }

            if (targetType == null) {
                targetType = createTargetType(v.getClass());
            }
            @SuppressWarnings("unchecked")
            final Constructor<U> constructor = targetType.getConstructor(v.getClass());

            return constructor.newInstance(v);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    /** Generic converter of the target class */
    public <T extends U> Class<T> marshalType(final Class<?> pojoClass) {
        try {
            final UjoConverter aConverter = pojoClass.getAnnotation(UjoConverter.class);
            final Class<?> result = aConverter != null
                    ? aConverter.target()
                    : createTargetType(pojoClass);
            return (Class<T>) result;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    /** Generic converter of the target class */
    @SuppressWarnings("unchecked")
    protected Class<U> createTargetType(final Class<?> pojoClass) throws ClassNotFoundException {
        final String path = pojoClass.getPackage().getName();
        final String name = pojoClass.getSimpleName();
        return (Class<U>) Class.forName(path + '.' + packag + '.' + prefix + name + suffix);
    }

    /** Convert from POJO list to UJO list */
    public List<U> marshalList(final Collection<?> list) throws IllegalStateException {
        final List<U> result = new ArrayList<U>(list.size());
        for (Object pojo : list) {
            result.add(marshal(pojo));
        }
        return result;
    }

    /** Convert from UJO to POJO */
    @Override
    public Object unmarshal(final U v) throws IllegalStateException {
        if (v == null) {
            return null;
        }
        try {
            final Method method = v.getClass().getMethod(ORIGINAL_METHOD);
            return method.invoke(v);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    // ------ TOOLS --------


    /** Get value */
    public <U extends Ujo, V> V getValue(final Key<U,V> key, final Object domain) {
        return key.of((U) marshal(domain));
    }

    /** Get value */
    public <U extends Ujo, V> void setValue(final Key<U,V> key, final Object domain, final V value) {
        key.setValue((U) marshal(domain), value);
    }

}
