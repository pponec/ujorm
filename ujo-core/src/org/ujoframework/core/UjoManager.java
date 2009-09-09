/*
 *  Copyright 2007 Paul Ponec
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

package org.ujoframework.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.UjoPropertyList;
import org.ujoframework.core.annot.Transient;
import org.ujoframework.core.annot.XmlAttribute;
import org.ujoframework.core.annot.XmlElementBody;
import org.ujoframework.extensions.*;
import org.ujoframework.implementation.array.ArrayUjo;
import org.ujoframework.swing.UjoPropertyRow;
import static org.ujoframework.extensions.UjoAction.*;

/**
 * General Ujo Manager
 * @author Pavel Ponec
 * @composed 1 - 1 UjoCoder
 */
public class UjoManager implements Comparator<UjoProperty> {

    /** Requested modifier of property definitions. */
    public static final int PROPERTY_MODIFIER = Modifier.STATIC|Modifier.PUBLIC|Modifier.FINAL;
       
    /** UjoManager instance */
    protected static UjoManager instance = new UjoManager();
    
    /** A properties cache. */
    final private HashMap<Class, UjoPropertyList> propertiesCache;

    /** A XML <strong>element body</strong> cache */
    private HashMap<Class, UjoProperty> xmlBodyCache;
    
    /** A XML <strong>attribute</strong> cache. */
    private HashSet<UjoProperty> attributesCache = null;

    /** A transient <strong>attribute</strong> cache. */
    private HashSet<UjoProperty> transientCache = null;
    
    /** Are properties reversed? */
    private Boolean arePropertiesReversed = null;
    
    private UjoCoder coder;
    
    /** Constructor. */
    public UjoManager() {
        this.propertiesCache = new HashMap<Class, UjoPropertyList>();
        this.coder = new UjoCoder();
    }
    
    /** Get a default initialization */
    public static final UjoManager getInstance() {
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
    
    /** Are properties reversed by default? */
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
    
    /** Read all properties. The first result is cached. */
    public UjoPropertyList readProperties(Class type) {
        UjoPropertyList result = propertiesCache.get(type);
        if (result==null) {
            result = new UjoPropertyListImpl(type, readPropertiesNocache(type, true));
            
            // Save the result into buffer:
            propertiesCache.put(type, result);
        }
        return result;
    }
    
    
    /**
     * Returns all direct properties (see an method UjoProperty.isDirect() for more information).
     * @param type Ujo class
     * @param sorted I want to sortd the result by a natural order.
     * @return Array of Properties
     */
    @SuppressWarnings("unchecked")
    public UjoProperty[] readPropertiesNocache(Class type, boolean sorted) throws IllegalStateException {
        UjoProperty[] result;
        ArrayList<UjoProperty> propertyList = new ArrayList<UjoProperty>(32);
        Field field = null;
        
        synchronized(type) {
            try {
                final Field[] fields = type.getFields();
                for (int j=0; j<fields.length; j++) {
                    field = fields[j];
                    if (field.getModifiers()==UjoManager.PROPERTY_MODIFIER
                    &&  UjoProperty.class.isAssignableFrom(field.getType())
                    ){
                        UjoProperty ujoProp = (UjoProperty) field.get(null);
                        if (ujoProp.isDirect()) {
                           propertyList.add(ujoProp);

                            if (ujoProp.getName()==null && ujoProp instanceof Property) {
                                PropertyModifier.setName(field.getName(), (Property)ujoProp);
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
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(String.valueOf(field), e);
            }
            
            result = propertyList.toArray( new UjoProperty[propertyList.size()] );

            if (sorted) {
                // Reverse order:
                if (isPropertiesReversed()) {
                    revertArray(result);
                }
                Arrays.sort(result, this);

                // Asssign new indexes:
                for (int i=0; i<result.length; i++) {
                    UjoProperty p = result[i];
                    if (p.getIndex()!=i && p instanceof Property) {
                        PropertyModifier.setIndex(i, (Property)p);
                    }
                }
            }
        }
        
        return result;
    }
    
    /** Compare Ujo properties. An undefined property indexes (-1 are sorted to the end. */
    public int compare(final UjoProperty p1, final UjoProperty p2) {
        int i1 = p1.getIndex()>=0 ? p1.getIndex() : Integer.MAX_VALUE;
        int i2 = p2.getIndex()>=0 ? p2.getIndex() : Integer.MAX_VALUE;

        return i1>i2 ?  1
        :      i2<i2 ? -1
        :               0
        ;
    }

    /** Sort properties. */
    protected void sortProperties(final Class type, final UjoProperty[] properties) {
        if (properties.length>0) {
            if (ArrayUjo.class.isAssignableFrom(type)) {
                Arrays.sort(properties);
            } else if(isPropertiesReversed()) {
                revertArray(properties);
            }
        }
    }
    
    /** Calculate a Hash Code. */
    public int getHash(Ujo ujo) {
        return getHash(ujo, ujo.readProperties());
    }
    
    /** Calculate a Hash Code. */
    public int getHash(Ujo ujo, UjoPropertyList properties) {
        int result = 0;
        if (ujo!=null) for (UjoProperty prop : properties) {
            Object obj = getValue(ujo, prop);
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
        return equalsUjo(u1, u2, u1!=null ? u1.readProperties() : null);
    }
    
    /**
     * Test if obj1 equalsUjo obj2. If obj1 object is Array, method call an equalsArray() method, else use en method equalsUjo().
     *
     * @param u1 First parameter
     * @param u2 Optional parameter
     * @return Returns true, if objects are the same.
     */
    public boolean equalsUjo(final Ujo u1, final Ujo u2, UjoPropertyList properties)  {
        if (u1==u2) {
            return true;
        }
        if (u1==null || u2==null) {
            return false;
        }
        if (u1.getClass().equals(u2.getClass())) {
            for (int i=properties.size()-1; i>=0; i--) {
                UjoProperty property = properties.get(i);
                final Object o1 = getValue(u1, property);
                final Object o2 = getValue(u2, property);
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
            for (UjoProperty property : ujo.readProperties()) {
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
     * Find a property by property name from parameter. Use rather the UjoPropertyList.findPropety(...).
     * @param ujo An Ujo object
     * @param name A property name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @deprecated Use UjoPropertyList.findPropety(...)
     * @see UjoPropertyList#find(org.ujoframework.Ujo, java.lang.String, boolean)
     *
     */
    public UjoProperty findProperty
    ( final Ujo ujo
    , final String name
    , final boolean throwException
    ) throws IllegalArgumentException
    {
        return ujo.readProperties().find(ujo, name, UjoAction.DUMMY, true, throwException);
    }
    
    /**
     * Find a property by property name from parameter. Use rather the UjoPropertyList.findPropety(...).
     * @param ujo An Ujo object
     * @param name A property name.
     * @param action Action type UjoAction.ACTION_* .
     * @param result Required result of action.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @see UjoPropertyList#find(org.ujoframework.Ujo, java.lang.String, org.ujoframework.extensions.UjoAction, boolean, boolean)
     */
    @SuppressWarnings("deprecation")
    public UjoProperty findProperty
    ( final Ujo ujo
    , final String name
    , final UjoAction action
    , final boolean result
    , final boolean throwException
    ) throws IllegalArgumentException
    {
        return ujo.readProperties().find(ujo, name, action, result, throwException);
    }

    /** Print a String representation */
    @SuppressWarnings("unchecked")
    public String toString(Ujo ujo) {
        return toString(ujo, ujo.readProperties());
    }

    /** Print a String representation */
    @SuppressWarnings("unchecked")
    public String toString(Ujo ujo, UjoPropertyList properties) {
        StringBuilder result = new StringBuilder(32);
        result.append(ujo.getClass().getSimpleName());
        UjoAction action = new UjoActionImpl(ACTION_TO_STRING, this);

        int length = properties.size();
        for(int i=0; i<length; ++i) {
            UjoProperty property = properties.get(i);
            if (!ujo.readAuthorization(action, property, this)) { continue; }

            boolean list = property instanceof ListUjoProperty;
            String textSeparator = "";
            
            String value;
            try {
                Object objVal = ujo.readValue(property);
                textSeparator = objVal instanceof CharSequence ? "\"" : "" ;
                
                value
                = list ? ((ListUjoProperty)property).getItemCount(ujo) + "]"
                : objVal instanceof Ujo ? "UJO:" + objVal.hashCode()
                : ujo    instanceof UjoTextable ? ((UjoTextable)ujo).readValueString(property, action)
                : coder.encodeValue(ujo, false)
                ;
            } catch (Throwable e) {
                value = e.getClass().getSimpleName();
            }
            result.append(i==0 ? "[" : ", ");
            result.append(property.getName());
            result.append(list ? "[" : "=");
            result.append(textSeparator);
            result.append(value);
            result.append(textSeparator);
        }

        if (properties.size()>0) {
            result.append("]");
        }
        return result.toString();
    }
    
    
    /** Returns true, if text is not null and is not empty. */
    public static boolean isUsable(final CharSequence text) {
        return text!=null && text.length()>0;
    }
    
    /** Returns a Element body of the class or the null if no property was found. */
    public final UjoProperty getXmlElementBody(final Class type) {

        if (propertiesCache.get(type)==null) {
            readProperties(type); // Load cache;
        }

        final UjoProperty result
            = xmlBodyCache!=null
            ? xmlBodyCache.get(type)
            : null
            ;
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

    /** Is the property an Transient? */
    public final boolean isTransientProperty(final UjoProperty property) {
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
    public final Object decodeValue(final UjoProperty property, final String aValue, Class type) {
        return coder.decodeValue(property, aValue, type);
    }

    /**
     * Decode text value
     * @param property Property is used for a result class
     * @param aValue Text value to decode.
     * @return Instance of new result.
     */
    public final Object decodeValue(final UjoProperty property, final String aValue) {
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
    private void cacheXmlAttribute(final UjoProperty attribute) {
        if (attribute.isTypeOf(Ujo.class)
        ||  attribute instanceof ListUjoProperty
        ){
            return;
        }
        if (attributesCache==null) {
            attributesCache = new HashSet<UjoProperty>();
        }
        attributesCache.add(attribute);
    }

    /** Mark a property to XML element in a cache. */
    @SuppressWarnings("unchecked")
    private void cacheXmlElementBody(final Class type, final UjoProperty property) {

        if (property.isTypeOf(Ujo.class)
        ||  property instanceof ListUjoProperty
        ){
            return;
        }
        if (xmlBodyCache==null) {
            xmlBodyCache = new HashMap<Class, UjoProperty>(4);
        }
        UjoProperty old = xmlBodyCache.get(type);
        if (old==null || old.getIndex()<property.getIndex()) {
            xmlBodyCache.put(type, property);
        }
    }

    /** Mark a property to transient attribute in a cache. */
    private void cacheTransientAttribute(final UjoProperty attribute) {
        if (transientCache==null) {
            transientCache = new HashSet<UjoProperty>();
        }
        transientCache.add(attribute);
    }

    /** An assignable test. */
    public boolean assertDirectAssign(final UjoProperty property, final Object value) throws IllegalArgumentException {
        return assertDirect(property, value) 
            && assertAssign(property, value)
            ;
    }

    /** An assignable test. */
    public boolean assertDirect(final UjoProperty property, final Object value) throws IllegalArgumentException {
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
    public boolean assertAssign(final UjoProperty property, final Object value) throws IllegalArgumentException {
        final boolean result = value==null || property.getType().isInstance(value);
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

    /** Set a value to an Ujo object by a selected properties. */
    @SuppressWarnings("unchecked")
    public static void setValue(final Ujo ujo, final UjoProperty prop, final Object value) {
        if (prop.isDirect()) {
            ujo.writeValue(prop, value);
        } else {
            prop.setValue(ujo,value);
        }
    }
    
    
    /** Set a value to an Ujo object by a chain of properties. 
     * <br>Type of value is checked in the runtime.
     */
    public Ujo setValue(Ujo ujo, UjoPropertyList props, Object value) throws IllegalArgumentException {
        final int last = props.size() - 1;
        UjoProperty lastProp = props.get(last);
        assertAssign(lastProp, value);
        for (int i = 0; i<last; i++) {
            UjoProperty p = props.get(i);
            ujo = (Ujo) getValue(ujo,p);
        }
        setValue(ujo, lastProp, value);
        return ujo;
    }    
    
    
    /** Get a value from an Ujo object by a selected property.
     * If a not getLastProperty value is null, then is throwded a NullPointe exception.
     */
    @SuppressWarnings("unchecked")
    public static Object getValue(final Ujo ujo, final UjoProperty prop) {
        final Object result = prop.isDirect()
        ? ujo.readValue(prop)
        : prop.getValue(ujo)
        ;
        return result;
    }    

    
    /** Get a value from an Ujo object by a chain of properties. 
     * If a not getLastProperty value is null, then is throwded a NullPointe exception.
     */
    @SuppressWarnings("unchecked")
    public <VALUE> VALUE getValue(Ujo ujo, UjoProperty... props) {
        Object result = ujo;
        for (UjoProperty p : props) {
            result = getValue((Ujo)result, p);
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
    public String getText(Ujo ujo, UjoProperty property, UjoAction action) {

        if (property.isDirect()) {
            final String result = (ujo instanceof UjoTextable)
                ? ((UjoTextable) ujo).readValueString(property, action)
                : encodeValue(getValue(ujo, property), false);
            return result;
        } else {
            final PathProperty pathProperty = (PathProperty) property;
            final UjoProperty p = pathProperty.getLastProperty();
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
    public void setText(Ujo ujo, UjoProperty property, String value, Class type, UjoAction action) {

        if (property.isDirect()) {
            if (ujo instanceof UjoTextable) {
                ((UjoTextable) ujo).writeValueString(property, value, type, action!=null ? action : UjoAction.DUMMY);
            } else {
                final Object o = decodeValue(property, value, type);
                setValue(ujo, property, o);
            }
        } else {
            final PathProperty pathProperty = (PathProperty) property;
            final UjoProperty p = pathProperty.getLastProperty();
            final Ujo         u = pathProperty.getSemifinalValue(ujo);
            setText(u, p, value, type, action);
        }
    }
    
    /** Create a list of UjoPropertyList */
    @SuppressWarnings("unchecked")
    public List<UjoPropertyRow> createPropertyList(Ujo content, UjoAction action) {
        UjoPropertyList props = content.readProperties();
        ArrayList<UjoPropertyRow> result = new ArrayList<UjoPropertyRow>(props.size());
        for (UjoProperty prop : props) {
            final Object  value   = content.readValue(prop);
            final boolean enabled = content.readAuthorization(action, prop, value);
            if (enabled) {
                final UjoPropertyRow ujoContentRow = new UjoPropertyRow(content, prop);
                result.add(ujoContentRow);
            }
        }
        return result;
    }
    
    /**
     * Copy selected properties from source to target.
     * @param source Source UJO
     * @param target Target UJO
     * @param action An action of source.
     * @param properties If the value is null, then all properties of source will be used.
     */    
    public void copy(Ujo source, Ujo target, UjoAction action, UjoProperty... properties) {
        if (properties==null) {
            properties = source.readProperties().toArray();
        }
        for(UjoProperty p : properties) {
            Object value = source.readValue(p);
            final boolean enabled = source.readAuthorization(action, p, value);
            if (enabled) {
                setValue(target, p, value);
            }
        }
    }

    /**
     * Copy selected properties of the source to target. An action is ACTION_COPY, an context is UjoManager.
     * @param source Source UJO
     * @param target Target UJO
     * @param properties If the value is null, then all properties of source will be used.
     */    
    public void copy(Ujo source, Ujo target, UjoProperty... properties) {
        copy(source, target, new UjoActionImpl(UjoAction.ACTION_COPY, this), properties);
    }
    
    
    /**
     * Copy ALL properties of the source to target. An action is ACTION_COPY, an context is UjoManager.
     * @param source Source UJO
     * @param target Target UJO
     */    
    public void copy(Ujo source, Ujo target) {
        copy(source, target, (UjoProperty[]) null);
    }

    /** Get a UjoProperty field. */
    public Field getPropertyField(Ujo ujo, UjoProperty property) {
        return getPropertyField(ujo.getClass(), property);
    }
    
    /** Get a UjoProperty field. */
    public Field getPropertyField(Class/*<? extends Ujo>*/ type, UjoProperty property) {
        Field result = null;
        try {
            final Field[] fields = type.getFields();
            for (int j=0; j<fields.length; j++) {
                result = fields[j];
                if (result.getModifiers()==UjoManager.PROPERTY_MODIFIER
                &&  UjoProperty.class.isAssignableFrom(result.getType())
                ){
                    final Object p = result.get(null);
                    if (p==property) {
                        break;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(String.valueOf(result), e);
        }
        return result;
    }

    /** Check ujo properties to a unique name.
     * There is recommended to calll the method from static block after UjoProperty initialization.
     * The beneficial side effect is loading a property cache.
     * @throws java.lang.IllegalStateException If an duplicity is found than an exception is throwed.
     */
    protected void checkUniqueProperties(final Class<? extends Ujo> type, final boolean enabled) throws IllegalStateException {
        final HashSet<String> names = new HashSet<String>(16);
        if (enabled) for (UjoProperty property : readProperties(type)) {
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

    /** Check ujo properties to a unique name.
     * There is recommended to calll the method from static block after UjoProperty initialization.
     * The beneficial side effect is loading a property cache.
     * @throws java.lang.IllegalStateException If an duplicity is found than an exception is throwed.
     */
    public void checkUniqueProperties(final Class<? extends Ujo> type) throws IllegalStateException {
         getInstance().checkUniqueProperties(type, true);
    }


    
    /** Regurns information about current library. */
    public static String projectInfo() {
        final String URL = "http://ujoframework.org/";
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
