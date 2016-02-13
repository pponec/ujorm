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

package org.ujorm.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.extensions.*;
import org.ujorm.validator.ValidationError;
import org.ujorm.validator.ValidatorUtils;

/**
 * Common Ujo static tools
 * @author Pavel Ponec

 */
public abstract class UjoTools implements Comparator<Key> {

    /** Simple space */
    public static final char SPACE = ' ';

    /** Requested modifier of key definitions. */
    public static final int PROPERTY_MODIFIER = KeyFactory.PROPERTY_MODIFIER;

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

    /** Calculate a Hash Code. */
    public int getHash(Ujo ujo) {
        return getHash(ujo, ujo.readKeys());
    }

    /** Calculate a Hash Code. */
    @SuppressWarnings("unchecked")
    public int getHash(Ujo ujo, KeyList<?> keys) {
        int result = 7;
        if (ujo != null) {
            for (Key key : keys) {
                final Object value = key.of(ujo);
                result = 11 * result + (value != null ? value.hashCode() : 0);
            }
        }
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
    public static boolean equalsUjo(final Ujo u1, final Ujo u2)  {
        return equalsUjo(u1, u2, u1!=null ? u1.readKeys() : null);
    }

    /**
     * Test if obj1 equalsUjo obj2. If obj1 object is Array, method call an equalsArray() method, else use en method equalsUjo().
     *
     * @param u1 First parameter
     * @param u2 Optional parameter
     * @return Returns true, if objects are the same.
     */
    @SuppressWarnings("unchecked")
    public static boolean equalsUjo(final Ujo u1, final Ujo u2, KeyList keys)  {
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
     * <ul>
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
    public static Ujo clone(Ujo ujo, int depth, Object context) throws IllegalStateException {
        final UjoAction action = new UjoActionImpl(UjoAction.ACTION_CLONE, context);
        if (--depth < 0
        || ujo==null
        ){
            return ujo;
        }
        try {
            Ujo result = ujo.getClass().newInstance();
            for (Key<Ujo,?> key : ujo.readKeys()) {
                Object value = ujo.readValue(key);
                if (ujo.readAuthorization(action, key, value)) {

                    if (value instanceof UjoCloneable) {
                        value = ((UjoCloneable)value).clone(depth, context);
                    } else if (value instanceof List) {
                        List list = (List) value.getClass().newInstance();
                        for (Object item : (List) value ) {
                            Object c
                            = (item instanceof Ujo) ? clone( (Ujo) item, depth, context )
                            : (item instanceof UjoCloneable) ? ((UjoCloneable)item).clone(depth, context)
                            :  item
                            ;
                            list.add(c);
                        }
                        value = list;
                    } else if (value!=null && value.getClass().isArray()) {
                        Object array = Array.newInstance(value.getClass(), Array.getLength(value));
                        System.arraycopy(value, 0, array, 0, Array.getLength(value));
                        value = array;
                    }
                    result.writeValue(key, value);
                }
            }
            return result;

        } catch (InstantiationException ex) { throw new IllegalStateException(ex);
        } catch (IllegalAccessException ex) { throw new IllegalStateException(ex);
        }
    }

    /**
     * Find a key annotation by the required type.
     * @param key The key must be a <strong>public static final</strong> field of the related Ujo class.
     * @param annotation Annotation type
     * @return  An annotation instance or the {@code null} value
     */
    public static <T extends Annotation> T findAnnotation(Key<?,?> key, Class<T> annotation) {
        if (key instanceof CompositeKey) {
            key = ((CompositeKey) key).getFirstKey();
        }
        try {
            for (Field field : key.getDomainType().getFields()) {
                if (field.getModifiers()==UjoTools.PROPERTY_MODIFIER
                &&  field.get(null) == key) {
                    return (T) field.getAnnotation(annotation);
                }
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Illegal state for: " + key, e);
        }
        return null;
    }

    /** Returns true, if the text is not null and is not empty. */
    public static boolean isFilled(final CharSequence text) {
        return text!=null && text.length()>0;
    }

    /** Returns true, if the list is not null and is not empty. */
    public static boolean isFilled(final Collection<?> list) {
        return list!=null && !list.isEmpty();
    }

    /** Validate the argument using all keys from the object. */
    public static List<ValidationError> validate(final Ujo ujo) {
        return ValidatorUtils.validate(ujo);
    }

    /** Validate the argument using all keys from the collection. */
    public static List<ValidationError> validate(final Collection<Ujo> ujos) {
        return ValidatorUtils.validate(ujos);
    }


    /** An assignable test. */
    public static boolean assertDirectAssign(final Key key, final Object value, final Ujo ujo) throws IllegalArgumentException {
        return assertDirect(key, value)
            && assertAssign(key, value)
            && assertUjoType(key, ujo);
    }

    /** An assignable test. */
    public static boolean assertDirect(final Key key, final Object value) throws IllegalArgumentException {
        if (key.isComposite()) {
            final String msg
            = "The key \""
            + key
            + "\" type of \""
            + key.getType().getName()
            + "\" is not a direct type."
            ;
            throw new IllegalArgumentException(msg);
        }
        return true;
    }

    /** An assignable test. */
    public static boolean assertAssign(final Key key, final Object value) throws IllegalArgumentException {
        final boolean result
            =  value == null
            || key.getType().isInstance(value)
            ;
        if (!result) {
            final String msg
            = "The value \""
            + value
            + "\""
            + (value!=null ? " (" + value.getClass().getName() + ')' : "")
            + " can't be assiged to key \""
            + key.getFullName()
            + "\" type of \""
            + key.getType().getName()
            + "\"."
            ;
            throw new IllegalArgumentException(msg);
        }
        return result;
    }

    /** An assignable test. */
    public static boolean assertUjoType(final Key key, final Ujo ujo) throws IllegalArgumentException {
        final Class type = key.getDomainType();
        final boolean result = type==null || type.isInstance(ujo);

        if (!result) {
            final String msg
            = "The ujo \""
            + ujo.getClass().getName()
            + "\""
            + " must by type of \""
            + key.getDomainType()
            + "\"."
            ;
            throw new IllegalArgumentException(msg);
        }
        return result;
    }

    /**
     * Get a Key field.
     * @param ujo Type of the Ujo object (Nonnull)
     * @param key Required key (Nullable)
     * @return Returns null in case that result was not found
     * @throws IllegalAccessException Can't get fields.
     */
    public static Field getPropertyField(Ujo ujo, Key key) throws IllegalStateException {
        return getPropertyField(ujo.getClass(), key, false);
    }

    /**
     * Get a Key field.
     * @param type Type of the Ujo object (Nonnull)
     * @param key Required key (Nullable)
     * @return Returns null in case that result was not found
     * @throws IllegalAccessException Can't get fields.
     * @throws IllegalArgumentException The 'key' is not found in the class 'type'.
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
     * @throws IllegalAccessException Can't get fields.
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
                } catch (Exception e) {
                    throw new IllegalStateException(String.valueOf(result), e);
                }
            }
        }
        if (throwException) {
            final String msg = String.format
                    ( "The key '%s' was not found in the class '%s'."
                    , String.valueOf(key)
                    , type.getName());
            throw new IllegalArgumentException(msg);
        }
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
        } catch (/*ReflectiveOperationException*/ Exception e) {
            throw new IllegalStateException("New instance failed for the : " + type, e);
        }
    }

    /** Returns information about current library. */
    public static String projectInfo() {
        final String URL = "http://ujorm.org/";
        final Package p  = Ujo.class.getPackage();
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
        final Package packge = Ujo.class.getPackage();
        final String result = packge!=null ? packge.getSpecificationVersion() : null;
        return result!=null ? result : "UNDEFINED" ;
    }

}
