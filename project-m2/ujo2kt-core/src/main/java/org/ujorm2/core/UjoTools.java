/*
 *  Copyright 2007-2014 Pavel Ponec
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

package org.ujorm2.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import org.ujorm2.CompositeKey;
import org.ujorm2.Key;
import org.ujorm2.KeyList;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm2.validator.ValidationError;
import org.ujorm2.validator.ValidatorUtils;

/**
 * Common Ujo static tools
 * @author Pavel Ponec

 */
public abstract class UjoTools implements Comparator<Key> {

    /** Simple space */
    public static final char SPACE = ' ';

    /** Requested modifier of key definitions. */
    public static final int PROPERTY_MODIFIER = 0;

    /** Returns a reversed order of objects. */
    public static void revertArray(Object[] array) {
        for (int left=0, right=array.length-1; left<right; left++, right--) {
            Object temp  = array[left];
            array[left]  = array[right];
            array[right] = temp;
        }
    }

    /** Returns true, if the class is abstract. */
    protected boolean isAbstract(Class type) {
        final boolean result = Modifier.isAbstract(type.getModifiers() );
        return result;
    }


    /**
     * Test if Object o1 equalsUjo o2.
     *
     * @param o1 First parameter
     * @param o2 Second parameter
     * @return Returns true, if objects are the same.
     */
    public static boolean equals(Object o1, Object o2)  {
        if (o1==o2) { return true; }
        if (o1==null || o2==null) { return false; }
        if (o1.getClass().isArray()) {
            return equalsArray(o1, o2);
        }
        return o1.equals(o2);
    }

    /**
     * Test if obj1 equalsUjo obj2. If obj1 object is Array, method call an equalsArray() method, else use en method equalsUjo().
     *
     * @param u1 First parameter
     * @param u2 Optional parameter
     * @return Returns true, if objects are the same.
     */
    public static boolean equalsUjo(final Object u1, final Object u2)  {
            throw new UnsupportedOperationException("TODO");

    }

    /**
     * Test if obj1 equalsUjo obj2. If obj1 object is Array, method call an equalsArray() method, else use en method equalsUjo().
     *
     * @param u1 First parameter
     * @param u2 Optional parameter
     * @return Returns true, if objects are the same.
     */
    @SuppressWarnings("unchecked")
    public static boolean equalsUjo(final Object u1, final Object u2, KeyList keys)  {
        if (u1==u2) {
            return true;
        }
        if (u1==null || u2==null) {
            return false;
        }
        if (u1.getClass().equals(u2.getClass())) {
            for (int i=keys.size()-1; i>=0; i--) {
                Key key = keys.get(i);
                final Object o1 = key.of(u1);
                final Object o2 = key.of(u2);
                if (! equals(o1, o2)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Test if array1 equalsUjo to array2. There are supported types:
     * <ul>
     *    <li>byte[]</li>
     *    <li>char[]</li>
     * </ul>
     *
     * @param array1 Mandatory parameter
     * @param array2 Optional parameter
     * @return Returns true, if two objects are the same.
     */
    public static boolean equalsArray(Object array1, Object array2)  {
        if (array1==null) {
            return array2==null;
        }
        if (Byte.TYPE==array1.getClass().getComponentType()) {
            return Arrays.equals( (byte[])array1, (byte[])array2 );
        }
        if (Character.TYPE==array1.getClass().getComponentType()) {
            return Arrays.equals( (char[])array1, (char[])array2 );
        }
        return array1.equals(array2);
    }

    /**
     * Clone the UjoCloneable object. The Object and its items must have got a constructor with no parameters.
     * <br>Note: There are supported attributes
     * <ul>
     * <li>null value </li>
     * <li>Ujo</li>
     * <li>UjoCloneable</li>
     * <li>List</li>
     * <li>array of privitive values</li>
     * </ul>
     *
     * In other cases the same instance is used. The feature can be useful for a Final object like a String, Integer etc.
     *
     * @param ujo An Ujo with no parameter constructor.
     * @param depth A depth of the cloning.
     * @param context Context of the action.
     * @return A clone
     * @see UjoAction#ACTION_CLONE
     * @throws java.lang.IllegalStateException
     */
    @SuppressWarnings("unchecked")
    public static Object clone(Object ujo, int depth, Object context) throws IllegalStateException {
             throw new UnsupportedOperationException("TODO");

    }

    /**
     * Find a key annotation by the required type.
     * @param key The key must be a <strong>public static final</strong> field of the related Ujo class.
     * @param annotation Annotation type
     * @return  An annotation instance or the {@code null} value
     */
    @Nullable
    public static <T extends Annotation> T findAnnotation(Key<?,?> key, Class<T> annotation) {
        if (key instanceof CompositeKey) {
            key = ((CompositeKey) key).getKey(0);
        }
        try {
            for (Field field : key.getDomainClass().getFields()) {
                if (field.getModifiers()==UjoTools.PROPERTY_MODIFIER
                &&  field.get(null) == key) {
                    return (T) field.getAnnotation(annotation);
                }
            }
        } catch (RuntimeException | ReflectiveOperationException | OutOfMemoryError e) {
            throw new IllegalUjormException("Illegal state for: " + key, e);
        }
        return null;
    }

    /** Returns true, if the text is not null and is not empty. */
    @Deprecated
    public static boolean isFilled(final CharSequence text) {
        return Check.hasLength(text);
    }

    /** Returns true, if the list is not null and is not empty. */
    @Deprecated
    public static boolean isFilled(final Collection<?> list) {
        return Check.hasLength(list);
    }

    /** Validate the argument using all keys from the object. */
    public static List<ValidationError> validate(final Object ujo) {
        throw new UnsupportedOperationException("TODO");
//      return ValidatorUtils.validate(ujo);
    }

    /** Validate the argument using all keys from the collection. */
    public static List<ValidationError> validate(final Collection<Object> ujos) {
        return ValidatorUtils.validate(ujos);
    }


    /** An assignable test. */
    public static boolean assertDirectAssign(final Key key, final Object value, final Object ujo) throws IllegalArgumentException {
        return assertDirect(key, value)
            && assertAssign(key, value)
            && assertUjoType(key, ujo);
    }

    /** An assignable test. */
    public static boolean assertDirect(final Key key, final Object value) throws IllegalArgumentException {
        Assert.isFalse(key.isComposite(), "The key '{}' type of '{}' is not a direct type."
            , key
            , key.getValueClass().getName())
            ;
        return true;
    }

    /** An assignable test. */
    public static boolean assertAssign(final Key key, final Object value) throws IllegalArgumentException {
            throw new UnsupportedOperationException("TODO");

    }

    /** An assignable test. */
    public static boolean assertUjoType(final Key key, final Object ujo) throws IllegalArgumentException {
        final Class type = key.getDomainClass();
        final boolean result = type==null || type.isInstance(ujo);
        Assert.isTrue(result, "The ujo '{}' must by type of '{}'."
            , ujo.getClass().getName()
            , key.getDomainClass());
        return result;
    }

    /**
     * Get a Key field.
     * @param ujo Type of the Ujo object (Nonnull)
     * @param key Required key (Nullable)
     * @return Returns null in case that result was not found
     * @throws IllegalStateException Can't get fields.
     */
    public static Field getPropertyField(Object ujo, Key key) throws IllegalStateException {
        return getPropertyField(ujo.getClass(), key, false);
    }

    /**
     * Get a Key field.
     * @param type Type of the Ujo object (Nonnull)
     * @param key Required key (Nullable)
     * @return Returns null in case that result was not found
     * @throws IllegalStateException The 'key' is not found in the class 'type'.
     */
    public static Field getPropertyField(Class<?> type, Key key) throws IllegalStateException {
        return getPropertyField(type, key, false);
    }

    /**
     * Get a Key field.
     * @param type Type of the Ujo object (Nonnull)
     * @param key Required key (Nullable)
     * @param throwException in case the result is {@code null} than throw the exception {@link IllegalArgumentException}.
     * @return Nonnull value always.
     * @throws IllegalStateException Can't get fields.
     * @throws IllegalArgumentException The key 'key' is not found in the class 'type'.
     */
    public static Field getPropertyField(Class<?> type, Key key, boolean throwException) throws IllegalStateException, IllegalArgumentException {
        for (Field result : type.getFields()) {
            if (result.getModifiers() == UjoTools.PROPERTY_MODIFIER
            && Key.class.isAssignableFrom(result.getType())) {
                try {
                    final Object p = result.get(null);
                    if (p == key) {
                        return result;
                    }
                } catch (RuntimeException | ReflectiveOperationException e) {
                    throw new IllegalUjormException(String.valueOf(result), e);
                }
            }
        }
        Assert.isFalse(throwException, "The key '{}' was not found in the class '{}'."
                    , String.valueOf(key)
                    , type.getName());
        return null;
    }

    /** Is the argument an Composite Key ?
     * @param key any <strong>nullable</strong> object
     * @return Returns true, if the argument is type of {@link CompositeKey}
     * and its method {@link CompositeKey#isComposite()}.
     */
    public static boolean isCompositeKey(Object key) {
        return key instanceof CompositeKey && ((CompositeKey) key).isComposite();
    }

    /** Is the argument an Composite Key ?
     * @param key any <strong>nullable</strong> object
     * @return Returns true, if the argument is type of {@link CompositeKey}
     * and its method {@link CompositeKey#isComposite()}.
     */
    public static boolean isCompositeKey(Key key) {
        return key != null && key.isComposite();
    }



    /** Create a new instance of the Ujo and initialize all static fields */
    public static <T> T newInstance(Class<T> type) throws IllegalStateException {
        try {
            return Modifier.isAbstract(type.getModifiers())
                 ? null
                 : type.newInstance();
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalUjormException("New instance failed for the : " + type, e);
        }
    }

    /** Returns information about current library. */
    public static String projectInfo() {
        final String URL = "http://ujorm.org/";
        final Package p  = Key.class.getPackage();
        final String CR  = System.getProperty("line.separator");

        String result
        = p.getSpecificationTitle()
        + CR
        + "version "
        + version()
        + CR
        + URL
        + CR
        ;
        return result;
    }

    /** Returns information about current library. */
    public static String version() {
        final Package packge = Key.class.getPackage();
        final String result = packge!=null ? packge.getSpecificationVersion() : null;
        return result!=null ? result : "UNDEFINED" ;
    }

}
