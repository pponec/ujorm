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

package org.version2.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.ujorm.Ujo;

/**
 * Generic POJO to UJO converter
 * @author Pavel Ponec
 */
public class UjoPojoConverter<U extends Ujo> extends XmlAdapter<U, Object> {

    private final String packag;
    private final String prefix;
    private final String suffix;

    public UjoPojoConverter() {
        this("generated", "$", "");
    }

    public UjoPojoConverter(String packag, String prefix, String suffix) {
        this.packag = packag;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public U marshal(final Object v) throws IllegalStateException {
        if (v == null) {
            return null;
        }
        try {
            // Required converter from an Annotation:
            final UjoConverter converter = v.getClass().getAnnotation(UjoConverter.class);
            if (converter != null) {
                @SuppressWarnings("unchecked")
                final UjoPojoConverter<U> newConverter = (UjoPojoConverter<U>) converter.value().newInstance();
                if (getClass() != newConverter.getClass()) {
                    return newConverter.marshal(v);
                }
            }

            // Generic converter:
            final String path = v.getClass().getPackage().getName();
            final String name = v.getClass().getSimpleName();
            final Class clazz = Class.forName(path + '.' + packag + '.' + prefix + name + suffix);
            @SuppressWarnings("unchecked")
            final Constructor<U> constructor = clazz.getConstructor(v.getClass());

            return constructor.newInstance(v);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Object unmarshal(final U v) throws IllegalStateException {
        if (v == null) {
            return null;
        }
        try {
            final Method method = v.getClass().getMethod("original");
            return method.invoke(v);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

}
