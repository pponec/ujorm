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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;
import org.ujorm.Key;
import org.ujorm.ListKey;

/**
 * Manager of <code>BeanUjo</code>
 * @author Pavel Ponec
 */
public class BeanManager<UJO,VALUE> {

    private final Key key;

    /** An empty array of classes. */
    private Method setter;
    private Method getter;

    public BeanManager(Key key) {
        this.key = key;
    }

    /** WARNING: There is recommended to call the method from the method Ujo.writeProperty(...) only.
     * <br>A direct call can bypass a important actions implemented in the writeProperty(method).
     */
    public void writeValue(final UJO bean, final VALUE value) throws IllegalArgumentException {
        try {
            getMethod(bean, true).invoke(bean, value);
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalUjormException("BeanProperty:"+key.getName()+"="+value, e);
        }
    }

    /** WARNING: There is recommended to call the method from the method <code>Ujo.readProperty(...)</code> only.
     * <br>A direct call can bypass a important actions implemented in the <code>readProperty(method)</code>.
     */
    public Object readValue(final UJO bean) throws IllegalArgumentException {
        try {
            return getMethod(bean, false).invoke(bean);
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalUjormException("BeanProperty:"+key.getName(), e);
        }
    }


    /**
     * Get or create a setter or getter.
     * @param ujo UJO object.
     * @param set The result method can be a setter (TRUE) or a getter (FALSE).
     * @throws java.lang.IllegalArgumentException Method can't find usable method.
     */
    public Method getMethod(final UJO ujo, final boolean set) throws IllegalArgumentException {
        Method result = set ? setter : getter ;
        if (result==null) {
            String methodName = getMethodName(set);
            Exception ex = null ;
            try {
                Class ujoClass = ujo.getClass();
                Class type = set ? key.getType() : null;

                result = getMethodPlain(ujoClass, type, methodName);
                if (result==null) {
                    if (key instanceof ListKey && !methodName.endsWith("s")) {
                        // TAG name is a singular:
                        result = getMethodPlain(ujoClass, type, methodName+'s');
                    } else if ((type=getPrimitive(type))!=null) {
                        // Try a primitive type:
                        result = getMethodPlain(ujoClass, type, methodName);
                    }
                }
                if (set) {
                    setter = result;
                } else {
                    getter = result;
                }
            } catch (SecurityException e) {
                ex = e;
            }
            if (result==null) {
                throw new IllegalUjormException("Can't find method: " + methodName+'('+key.getType().getName()+')', ex);
            }
        }
        return result;
    }

    /** Returns a primitive type if can or a null value. */
    @Nullable
    protected Class getPrimitive(Class objClass) {

        if (objClass!=null && !objClass.isPrimitive()) try {
            Field field = objClass.getField("TYPE");
            if (field.getType()==Class.class) {
                final Object result = field.get(null);
                return (Class) result;
            }
        } catch (RuntimeException | ReflectiveOperationException e) {
            return null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Method getMethodPlain
    ( final Class ujoClass
    , final Class type
    , final String methodName
    ) throws SecurityException {

        try {
            final Class[] types = type!=null ? new Class[]{type} : null ;
            final Method result = ujoClass.getMethod(methodName, types);
            return result;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /** Returns a method name by a name */
    protected String getMethodName(final boolean set) {
        String result = (set ? "set" : Boolean.class.equals(key.getType()) ? "is" : "get")
        + Character.toUpperCase(key.getName().charAt(0))
        + key.getName().substring(1)
        ;
        return result;
    }

    /** Create new instance of BeanManager */
    public static <UJO,VALUE> BeanManager<UJO,VALUE> getInstance(Key key) {
        return new BeanManager<UJO,VALUE>(key);
    }

    @Override
    public String toString() {
        return key.getName();
    }


}
