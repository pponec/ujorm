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

import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
import org.ujorm.ListKey;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.CompositeKey;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.core.annot.Transient;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.core.annot.XmlElementBody;
import org.ujorm.extensions.*;
import org.ujorm.implementation.array.ArrayUjo;
import org.ujorm.swing.UjoPropertyRow;
import static org.ujorm.UjoAction.*;

/**
 * General Ujo Manager
 * @author Pavel Ponec
 * @composed 1 - 1 UjoCoder
 */
public class UjoManager implements Comparator<Key> {

    /** Requested modifier of property definitions. */
    public static final int PROPERTY_MODIFIER = KeyFactory.PROPERTY_MODIFIER;
       
    /** UjoManager instance */
    protected static UjoManager instance = new UjoManager();
    
    /** A keys cache. */
    final private HashMap<Class, KeyList> propertiesCache;

    /** A XML <strong>element body</strong> cache */
    private HashMap<Class, Key> xmlBodyCache;
    
    /** A XML <strong>attribute</strong> cache. */
    private HashSet<Key> attributesCache = null;

    /** A transient <strong>attribute</strong> cache. */
    private HashSet<Key> transientCache = null;
    
    /** Are keys reversed? */
    private Boolean arePropertiesReversed = null;
    
    private UjoCoder coder;
    
    /** Constructor. */
    public UjoManager() {
        this.propertiesCache = new HashMap<Class, KeyList>();
        this.coder = new UjoCoder();
    }
    
    /** Get a default initialization */
    public static UjoManager getInstance() {
        return instance;
    }
    
    /** Returns a reversed order of objects. */
    public void revertArray(Object[] array) {
        for (int left=0, right=array.length-1; left<right; left++, right--) {
            Object temp  = array[left];
            array[left]  = array[right];
            array[right] = temp;
        }
    }
    
    /** Are keys reversed by default? */
    public boolean isPropertiesReversed() {
        if (arePropertiesReversed==null) {
            arePropertiesReversed = new DummyUjo().isPropertiesReversed().booleanValue();
        }
        return arePropertiesReversed;
    }
    
    /** Returns true, if the class is abstract. */
    protected boolean isAbstract(Class type) {
        final boolean result = Modifier.isAbstract(type.getModifiers() );
        return result;
    }
    
    /** Read an KeyList instance. The first result is cached. */
    public UjoPropertyList readProperties(Class type) {
        KeyList result = propertiesCache.get(type);
        if (result==null) {
            final Key[] ps = readPropertiesNocache(type, true);
            result = ps.length==0 
                    ? KeyRing.of(type, ps)
                    : KeyRing.of(ps);
            
            // Save the result into buffer:
            propertiesCache.put(type, result);
        }
        return new UjoPropertyListImpl(result);
    }
    
    
    /**
     * Returns all direct keys (see an method Key.isDirect() for more information).
     * @param type Ujo class
     * @param sorted I want to sortd the result by a natural order.
     * @return Array of Keys
     */
    @SuppressWarnings("unchecked")
    public Key[] readPropertiesNocache(Class type, boolean sorted) throws IllegalStateException {
        Key[] result;
        ArrayList<Key> propertyList = new ArrayList<Key>(32);
        Field field = null;
        
        synchronized(type) {
            try {
                final Field[] fields = type.getFields();
                for (int j=0; j<fields.length; j++) {
                    field = fields[j];
                    if (field.getModifiers()==UjoManager.PROPERTY_MODIFIER
                    &&  Key.class.isAssignableFrom(field.getType())
                    ){
                        Key ujoProp = (Key) field.get(null);
                        if (ujoProp==null) {
                            final String msg = "The field '"
                                + field
                                + "' of the '"
                                + type
                                + "' is not initialized properly yet. Try to call the current method later."
                                ;
                            throw new IllegalStateException(msg);
                        }
                        if (ujoProp.isDirect()) {
                           propertyList.add(ujoProp);

                            if (ujoProp instanceof Property) {
                                if (ujoProp.getName() == null) {
                                    PropertyModifier.setName(field.getName(), (Property) ujoProp);
                                }
                                if (ujoProp.getType() == null) {
                                    PropertyModifier.setType(KeyFactory.getGenericClass(field, true), (Property) ujoProp);
                                }
                                if (ujoProp.getDomainType() == null) {
                                    PropertyModifier.setDomainType(KeyFactory.getGenericClass(field, false), (Property) ujoProp);
                                }
                                if (ujoProp instanceof AbstractCollectionProperty) {
                                    final AbstractCollectionProperty lp = (AbstractCollectionProperty) ujoProp;
                                    if (lp.getItemType() == null) {
                                        PropertyModifier.setItemType(KeyFactory.getGenericClass(field, true), lp);
                                    }
                                }
                            }
                        }

                        // set the transient cache:
                        final Transient tr = field.getAnnotation(Transient.class);
                        // set the xml attribute cache:
                        final XmlAttribute xa = field.getAnnotation(XmlAttribute.class);
                        // set the xml element body cache:
                        final XmlElementBody xb = field.getAnnotation(XmlElementBody.class);

                        if      (tr!=null) { cacheTransientAttribute(ujoProp)  ; }
                        else if (xa!=null) { cacheXmlAttribute(ujoProp)        ; }
                        else if (xb!=null) { cacheXmlElementBody(type, ujoProp); }

                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException(String.valueOf(field), e);
            }
            
            result = propertyList.toArray( new Key[propertyList.size()] );

            if (sorted) {
                // Reverse order:
                if (isPropertiesReversed()) {
                    revertArray(result);
                }
                Arrays.sort(result, this);

                // Asssign new indexes:
                for (int i=0; i<result.length; i++) {
                    Key p = result[i];
                    if (p.getIndex()!=i && p instanceof Property) {
                        PropertyModifier.setIndex(i, (Property)p);
                    }
                }
            }
        }        
        return result;
    }

    /** Register new Property list to the internal cache. */
    @PackagePrivate void register (KeyList list, KeyFactory.InnerDataStore data) {
        this.propertiesCache.put(data.getDomainType(), list);

        final Iterable<Key<?,?>> it = data.getKeys();
        for (Key ujoProp : it) {

            // set the transient cache:
            final Transient tr = (Transient) data.getAnnotation(ujoProp, Transient.class);
            // set the xml attribute cache:
            final XmlAttribute xa = (XmlAttribute) data.getAnnotation(ujoProp, XmlAttribute.class);
            // set the xml element body cache:
            final XmlElementBody xb = (XmlElementBody) data.getAnnotation(ujoProp, XmlElementBody.class);

            if      (tr!=null) { cacheTransientAttribute(ujoProp)  ; }
            else if (xa!=null) { cacheXmlAttribute(ujoProp)        ; }
            else if (xb!=null) { cacheXmlElementBody(data.getDomainType(), ujoProp); }
        }
    }
    
    /** Compare Ujo keys by index. An undefined property indexes (-1 are sorted to the end. */
    public int compare(final Key p1, final Key p2) {
        final int i1 = p1.getIndex()>=0 ? p1.getIndex() : Integer.MAX_VALUE;
        final int i2 = p2.getIndex()>=0 ? p2.getIndex() : Integer.MAX_VALUE;

        return i1<i2 ? -1
        :      i1>i2 ? 1
        :              0
        ;
    }

    /** Sort keys. */
    protected void sortProperties(final Class type, final Key[] keys) {
        if (keys.length>0) {
            if (ArrayUjo.class.isAssignableFrom(type)) {
                Arrays.sort(keys, this);
            } else if(isPropertiesReversed()) {
                revertArray(keys);
            }
        }
    }
    
    /** Calculate a Hash Code. */
    public int getHash(Ujo ujo) {
        return getHash(ujo, ujo.readKeys());
    }
    
    /** Calculate a Hash Code. */
    @SuppressWarnings("unchecked")
    public int getHash(Ujo ujo, KeyList<?> keys) {
        int result = 0;
        if (ujo!=null) for (Key prop : keys) {
            Object obj = prop.of(ujo);
            if (obj!=null) {
                result = (result>>>3) + obj.hashCode();
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
    public boolean equals(Object o1, Object o2)  {
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
    public boolean equalsUjo(final Ujo u1, final Ujo u2)  {
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
    public boolean equalsUjo(final Ujo u1, final Ujo u2, KeyList keys)  {
        if (u1==u2) {
            return true;
        }
        if (u1==null || u2==null) {
            return false;
        }
        if (u1.getClass().equals(u2.getClass())) {
            for (int i=keys.size()-1; i>=0; i--) {
                Key property = keys.get(i);
                final Object o1 = property.of(u1);
                final Object o2 = property.of(u2);
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
    public boolean equalsArray(Object array1, Object array2)  {
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
     *
     * @param ujo An Ujo with no parameter constructor.
     * @param depth A depth of the cloning.
     * @param context Context of the action.
     * @return A clone
     * @see UjoAction#ACTION_CLONE
     * @throws java.lang.IllegalStateException
     */
    @SuppressWarnings("unchecked")
    public Ujo clone(Ujo ujo, int depth, Object context) throws IllegalStateException {
        final UjoAction action = new UjoActionImpl(UjoAction.ACTION_CLONE, context);
        if (--depth < 0
        || ujo==null
        ){
            return ujo;
        }
        try {
            Ujo result = (Ujo) ujo.getClass().newInstance();
            for (Key property : ujo.readKeys()) {
                Object value = ujo.readValue(property);
                if (ujo.readAuthorization(action, property, value)) {
                    
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
                    result.writeValue(property, value);
                }
            }
            return result;
            
        } catch (InstantiationException ex) { throw new IllegalStateException(ex);
        } catch (IllegalAccessException ex) { throw new IllegalStateException(ex);
        }
    }
    
    /**
     * Find a property by property name from parameter. Use rather the KeyList.findPropety(...).
     * @param ujo An Ujo object
     * @param name A property name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @deprecated Use KeyList.findPropety(...)
     * @see KeyList#findDirectKey(org.ujorm.Ujo, java.lang.String, boolean)
     *
     */
    public Key findProperty
    ( final Ujo ujo
    , final String name
    , final boolean throwException
    ) throws IllegalArgumentException
    {
        return ujo.readKeys().findDirectKey(ujo, name, UjoAction.DUMMY, true, throwException);
    }
    
    /**
     * Find a property by property name from parameter. Use rather the KeyList.findPropety(...).
     * @param ujo An Ujo object
     * @param name A property name.
     * @param action Action type UjoAction.ACTION_* .
     * @param result Required result of action.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @see KeyList#findDirectKey(java.lang.String, boolean)
     */
    @SuppressWarnings("deprecation")
    public Key findProperty
    ( final Ujo ujo
    , final String name
    , final UjoAction action
    , final boolean result
    , final boolean throwException
    ) throws IllegalArgumentException
    {
        return ujo.readKeys().findDirectKey(ujo, name, action, result, throwException);
    }

    /** Find <strong>indirect</strong> property by the name */
    @SuppressWarnings("unchecked")
    public Key findIndirectProperty(Class ujoType, String names) {
        return findIndirectProperty(ujoType, names, true);
    }

    /** Find <strong>indirect</strong> property by the name. Empty result can trhow NULL value if parameter throwException==false. */
    @SuppressWarnings("unchecked")
    public Key findIndirectProperty(Class ujoType, String names, boolean throwException) {
        return readProperties(ujoType).find(names, throwException);
    }

    /** Print a String representation */
    @SuppressWarnings("unchecked")
    public String toString(Ujo ujo) {
        return toString(ujo, ujo.readKeys());
    }

    /** Print a String representation. <br>
     * Note: Very long property values are truncated to the 128 characters. */
    @SuppressWarnings("unchecked")
    public String toString(Ujo ujo, KeyList keys) {
        final StringBuilder result = new StringBuilder(32);
        result.append(ujo.getClass().getSimpleName());
        final UjoAction action = new UjoActionImpl(ACTION_TO_STRING, this);

        for(int i=0, max = keys.size(); i<max; ++i) {
            final Key property = keys.get(i);
            if (!ujo.readAuthorization(action, property, this)) { continue; }

            // If the parameter is the List or another Ujo, then show a detail information:
            boolean showInfo = property instanceof ListKey;
            String textSeparator = "";
            
            String value;
            try {
                final Object objVal = property.of(ujo);
                textSeparator = objVal instanceof CharSequence ? "\"" : "" ;

                if (showInfo) {
                    final int itemCount = ((ListKey)property).getItemCount(ujo);
                    value = String.valueOf(itemCount);
                } else if (objVal instanceof Ujo) {
                    value = getFirstValue((Ujo)objVal, 10);
                    showInfo = true;
                } else {
                    value = coder.encodeValue(objVal, false);
                }
            } catch (Throwable e) {
                value = e.getClass().getSimpleName();
            }

            // Very long property value is truncated to the 128 characters.
            if (value!=null && value.length()>128) {
                value = value.substring(0, 128-3) + "...";
            }

            result.append(i==0 ? "[" : ", ");
            result.append(property.getName());
            result.append(showInfo ? "[" : "=");
            result.append(textSeparator);
            result.append(value);
            result.append(textSeparator);
            if (showInfo) {
                result.append("]");
            }
        }

        if (keys.size()>0) {
            result.append("]");
        }
        return result.toString();
    }

    /** Get a value of the first Key */
    private String getFirstValue(final Ujo ujo, int deep) {
        if (ujo==null) {
            return "null";
        }
        final KeyList props = ujo.readKeys();
        if (props.isEmpty()) {
            return "hash:" + ujo.hashCode();
        }
        final Key p = props.get(0);
        Object result = p.of(ujo);
        if (result instanceof Ujo) {
            if (--deep>0) {
                return getFirstValue((Ujo)result, deep);
            } else {
                return "hash:" + ujo.hashCode();
            }
        } else {
            return result==null
                ? "hash:" + ujo.hashCode()
                : (p + "=" + coder.encodeValue(result, false));
        }
    }
    
    /** Returns true, if text is not null and is not empty. */
    public static boolean isFilled(final CharSequence text) {
        return text!=null && text.length()>0;
    }
    
    /** Returns a Element body of the class or the null if no property was found. */
    public final Key getXmlElementBody(final Class type) {

        if (propertiesCache.get(type)==null) {
            readProperties(type); // Load cache;
        }

        final Key result
            = xmlBodyCache!=null
            ? xmlBodyCache.get(type)
            : null
            ;
        return result;
    }

    /** Is the property an XML attribute? */
    public final boolean isXmlAttribute(final Key property) {
        final boolean result 
            =  attributesCache!=null
            && attributesCache.contains(property)
            ;
        return result;
    }

    /** Is the property an Transient? */
    public final boolean isTransientProperty(final Key property) {
        final boolean result = transientCache!=null && transientCache.contains(property);
        return result;
    }

    /**
     * Decode text value
     * @param property Property is used for a result class
     * @param aValue Text value to decode.
     * @param type Optional subtype class of the property type.
     * @return Instance of new result.
     */
    public final Object decodeValue(final Key property, final String aValue, Class type) {
        return coder.decodeValue(property, aValue, type);
    }

    /**
     * Decode text value
     * @param property Property is used for a result class
     * @param aValue Text value to decode.
     * @return Instance of new result.
     */
    public final Object decodeValue(final Key property, final String aValue) {
        return coder.decodeValue(property, aValue, null);
    }

    /**
     * Decode text value
     * @param type Parameter is used for a result class.
     * @param aValue Text value to decode.
     * @return Instance of new result.
     */
    public final Object decodeValue(final Class type, final String aValue) {
        return coder.decodeValue(type, aValue);
    }

    /** Convert value to a String representation. */
    public final String encodeValue(final Object value, final boolean regenerationTest) {
        return coder.encodeValue(value, regenerationTest);
    }

    /** Mark a property to XML attribute in a cache. */
    @SuppressWarnings("unchecked")
    private void cacheXmlAttribute(final Key attribute) {
        if (attribute.isTypeOf(Ujo.class)
        ||  attribute instanceof ListKey
        ){
            return;
        }
        if (attributesCache==null) {
            attributesCache = new HashSet<Key>();
        }
        attributesCache.add(attribute);
    }

    /** Mark a property to XML element in a cache. */
    @SuppressWarnings("unchecked")
    private void cacheXmlElementBody(final Class type, final Key property) {

        if (property.isTypeOf(Ujo.class)
        ||  property instanceof ListKey
        ){
            return;
        }
        if (xmlBodyCache==null) {
            xmlBodyCache = new HashMap<Class, Key>(4);
        }
        Key old = xmlBodyCache.get(type);
        if (old==null || old.getIndex()<property.getIndex()) {
            xmlBodyCache.put(type, property);
        }
    }

    /** Mark a property to transient attribute in a cache. */
    private void cacheTransientAttribute(final Key attribute) {
        if (transientCache==null) {
            transientCache = new HashSet<Key>();
        }
        transientCache.add(attribute);
    }

    /** An assignable test. */
    static public boolean assertDirectAssign(final Key property, final Object value) throws IllegalArgumentException {
        return assertDirect(property, value) 
            && assertAssign(property, value)
            ;
    }

    /** An assignable test. */
    static public boolean assertDirect(final Key property, final Object value) throws IllegalArgumentException {
        if (!property.isDirect()) {
            final String msg
            = "The property \"" 
            + property
            + "\" type of \""
            + property.getType().getName()
            + "\" is not a direct type."
            ;
            throw new IllegalArgumentException(msg);            
        }
        return true;
    }
    
    
    /** An assignable test. */
    static public boolean assertAssign(final Key property, final Object value) throws IllegalArgumentException {
        final boolean result 
            =  value==null
            || value instanceof NoCheck
            || property.getType().isInstance(value)
            ;
        if (!result) {
            final String msg
            = "The value \""
            + value
            + "\""
            + (value!=null ? " (" + value.getClass().getName() + ')' : "")
            + " can't be assiged to property \""
            + property
            + "\" type of \""
            + property.getType().getName()
            + "\"."
            ;
            throw new IllegalArgumentException(msg);
        }
        return result;
    }
    
    // ---------------------------------------------------------------------

    /** Set a value to an Ujo object by a selected keys. */
    @SuppressWarnings("unchecked")
    public static void setValue(final Ujo ujo, final Key prop, final Object value) {
        if (prop.isDirect()) {
            ujo.writeValue(prop, value);
        } else {
            prop.setValue(ujo,value);
        }
    }
    
    
    /** Set a value to an Ujo object by a chain of keys. 
     * <br>Type of value is checked in the runtime.
     */
    @SuppressWarnings("unchecked")
    public Ujo setValue(Ujo ujo, KeyList props, Object value) throws IllegalArgumentException {
        final int last = props.size() - 1;
        Key lastProp = props.get(last);
        assertAssign(lastProp, value);
        for (int i = 0; i<last; i++) {
            Key p = props.get(i);
            ujo = (Ujo) p.of(ujo);
        }
        setValue(ujo, lastProp, value);
        return ujo;
    }    
    
    
    /** Get a value from an Ujo object by a selected property.
     * If a not getLastPartialProperty value is null, then is throwded a NullPointe exception.
     * @deprecated Use a expression <code>prop.of(ujo)</code> rather.
     */
    @Deprecated  @SuppressWarnings("unchecked")
    public static Object getValue(final Ujo ujo, final Key prop) {
        return prop.of(ujo);
    } 
    
    /** Get a value from an Ujo object by a chain of keys. 
     * If a not getLastPartialProperty value is null, then is throwded a NullPointe exception.
     */
    @SuppressWarnings("unchecked")
    public <VALUE> VALUE getValue(Ujo ujo, Key... props) {
        Object result = ujo;
        for (Key p : props) {
            result = p.of((Ujo) result);
        }
        return (VALUE) result;
    }    

    
    /** UjoCoder */
    public UjoCoder getCoder() {
         return coder;
    }

    /** UjoCoder */
    public void setCoder(UjoCoder ujoCoder) {
         coder = ujoCoder;
    }

    /** Get a text value from property */
    @SuppressWarnings("unchecked")
    public String getText(Ujo ujo, Key property, UjoAction action) {

        if (property.isDirect()) {
            final String result = (ujo instanceof UjoTextable)
                ? ((UjoTextable) ujo).readValueString(property, action)
                : encodeValue(property.of(ujo), false);
            return result;
        } else {
            final CompositeKey pathProperty = (CompositeKey) property;
            final Key p = pathProperty.getLastKey();
            final Ujo         u = pathProperty.getSemifinalValue(ujo);
            return getText(u, p, action);
        }
    }

    /**
     * Set a text value by property. The method recognise an UjoTextable object.
     * @param ujo Ujo
     * @param property Direct property
     * @param value Value
     * @param type Subtype of value
     * @param action Context action
     */
    @SuppressWarnings("unchecked")
    public void setText(Ujo ujo, Key property, String value, Class type, UjoAction action) {

        if (property.isDirect()) {
            if (ujo instanceof UjoTextable) {
                ((UjoTextable) ujo).writeValueString(property, value, type, action!=null ? action : UjoAction.DUMMY);
            } else {
                final Object o = decodeValue(property, value, type);
                setValue(ujo, property, o);
            }
        } else {
            final CompositeKey pathProperty = (CompositeKey) property;
            final Key p = pathProperty.getLastKey();
            final Ujo         u = pathProperty.getSemifinalValue(ujo);
            setText(u, p, value, type, action);
        }
    }
    
    /** Create a list of KeyList */
    @SuppressWarnings("unchecked")
    public List<UjoPropertyRow> createPropertyList(Ujo content, UjoAction action) {
        KeyList<?> props = content.readKeys();
        ArrayList<UjoPropertyRow> result = new ArrayList<UjoPropertyRow>(props.size());
        for (Key prop : props) {
            final Object  value   = prop.of(content);
            final boolean enabled = content.readAuthorization(action, prop, value);
            if (enabled) {
                final UjoPropertyRow ujoContentRow = new UjoPropertyRow(content, prop);
                result.add(ujoContentRow);
            }
        }
        return result;
    }
    
    /**
     * Copy selected keys from source to target.
     * @param source Source UJO
     * @param target Target UJO
     * @param action An action of source.
     * @param keys If the value is null, then all keys of source will be used.
     */    
    public void copy(Ujo source, Ujo target, UjoAction action, Key... keys) {
        if (keys==null) {
            keys = source.readKeys().toArray();
        }
        for(Key p : keys) {
            Object value = source.readValue(p);
            final boolean enabled = source.readAuthorization(action, p, value);
            if (enabled) {
                setValue(target, p, value);
            }
        }
    }

    /**
     * Copy selected keys of the source to target. An action is ACTION_COPY, an context is UjoManager.
     * @param source Source UJO
     * @param target Target UJO
     * @param keys If the value is null, then all keys of source will be used.
     */    
    public void copy(Ujo source, Ujo target, Key... keys) {
        copy(source, target, new UjoActionImpl(UjoAction.ACTION_COPY, this), keys);
    }
    
    
    /**
     * Copy ALL keys of the source to target. An action is ACTION_COPY, an context is UjoManager.
     * @param source Source UJO
     * @param target Target UJO
     */    
    public void copy(Ujo source, Ujo target) {
        copy(source, target, (Key[]) null);
    }

    /**
     * Get a Key field.
     * @param ujo Type of the Ujo object (Nonnull)
     * @param property Required property (Nullable)
     * @return Returns null in case that result was not found
     * @throws IllegalAccessException Can't get fields.
     */
    public Field getPropertyField(Ujo ujo, Key property) throws IllegalStateException {
        return getPropertyField(ujo.getClass(), property, false);
    }

    /**
     * Get a Key field.
     * @param type Type of the Ujo object (Nonnull)
     * @param property Required property (Nullable)
     * @return Returns null in case that result was not found
     * @throws IllegalAccessException Can't get fields.
     * @throws IllegalArgumentException The property 'property' is not found in the class 'type'.
     */
    public Field getPropertyField(Class<?> type, Key property) throws IllegalStateException {
        return getPropertyField(type, property, false);
    }
    
    /**
     * Get a Key field.
     * @param type Type of the Ujo object (Nonnull)
     * @param property Required property (Nullable)
     * @param throwException in case the result is {@code null} than throw the exception {@link IllegalArgumentException}.
     * @return Nonnull value always.
     * @throws IllegalAccessException Can't get fields.
     * @throws IllegalArgumentException The property 'property' is not found in the class 'type'.
     */
    public Field getPropertyField(Class<?> type, Key property, boolean throwException) throws IllegalStateException, IllegalArgumentException {
        for (Field result : type.getFields()) {
            if (result.getModifiers() == UjoManager.PROPERTY_MODIFIER
            && Key.class.isAssignableFrom(result.getType())) {
                try {
                    final Object p = result.get(null);
                    if (p == property) {
                        return result;
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(String.valueOf(result), e);
                }
            }
        }
        if (throwException) {
            final String msg = String.format
                    ( "The property '%s' was not found in the class '%s'."
                    , String.valueOf(property)
                    , type.getName());
            throw new IllegalArgumentException(msg);
        }
        return null;
    }

    /** Check ujo keys to a unique name.
     * There is recommended to calll the method from static block after Key initialization.
     * The beneficial side effect is loading a property cache.
     * @throws java.lang.IllegalStateException If an duplicity is found than an exception is throwed.
     */
    protected void checkUniqueProperties(final Class<? extends Ujo> type, final boolean enabled) throws IllegalStateException {
        final HashSet<String> names = new HashSet<String>(16);
        if (enabled) for (UjoProperty property : readProperties(type)) {
            //final UjoProperty property = (UjoProperty) _property;
            if (!names.add(property.getName())) {
                throw new IllegalStateException
                    ( "Property '"
                    + property
                    + "' is duplicate in the "
                    + type
                    );
            }
        }
    }

    /** Check ujo keys to a unique name.
     * There is recommended to calll the method from static block after Key initialization.
     * The beneficial side effect is loading a property cache.
     * @throws java.lang.IllegalStateException If an duplicity is found than an exception is throwed.
     */
    public void checkUniqueProperties(final Class<? extends Ujo> type) throws IllegalStateException {
         getInstance().checkUniqueProperties(type, true);
    }
    
    /** Regurns information about current library. */
    public static String projectInfo() {
        final String URL = "http://ujorm.org/";
        final Package p  = Ujo.class.getPackage();
        final String CR  = System.getProperty("line.separator");

        String result
        = p.getSpecificationTitle()
        + CR
        + "version "    
        + projectVersion()
        + CR
        + URL    
        + CR    
        ;    
        return result;
    }

    /** Regurns information about current library. */
    public static String projectVersion() {
        String result = Ujo.class.getPackage().getSpecificationVersion();
        return result!=null ? result : "UNDEFINED" ;
    }


    /** Show an information about the framework */
    public static void main(String[] args) {
        System.out.println(projectInfo());
    }
    
}
