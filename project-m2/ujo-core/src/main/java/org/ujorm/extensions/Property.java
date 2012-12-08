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

package org.ujorm.extensions;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.ujorm.CompositeKey;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.UjoProperty;
import org.ujorm.core.annot.Immutable;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.criterion.ValueCriterion;

/**
 * The main implementation of the interface Key.
 * @see AbstractUjo
 * @author Pavel Ponec
 */
@Immutable
public class Property<UJO extends Ujo,VALUE> implements UjoProperty<UJO,VALUE> {

    /** Property Separator character */
    public static final char PROPERTY_SEPARATOR = '.';
    /** Unefined index value */
    public static final int UNDEFINED_INDEX = -1;

    /** Property name */
    private String name;
    /** Property index, there are exist three indext ranges
     * <ul>
     *     <li>index == UNDEFINED_INDEX 
     *     : an undefine index or a signal for auto-index action</li>
     *     <li>index &lt; UNDEFINED_INDEX
     *      : the discontinuous and ascending series of numbers that is generated using a special method</li>
     *     <li>index &gt; UNDEFINED_INDEX 
     *     : the continuous and ascending series of numbers usable as a pointer to an array. This is a final state</li>
     * </ul>
     */
    private int index;
    /** Property type (class) */
    private Class<VALUE> type;
    /** Doman type type (class) */
    private Class<UJO> domainType;
    /** Property default value */
    private VALUE defaultValue;
    /** Lock the property after initialization */
    private boolean lock;


    /** A property seqeuncer for an index attribute
     * @see #_nextSequence()
     */
    private static int _sequencer = Integer.MIN_VALUE;

    /** Returns a next property index by a synchronized method.
     * The UJO property indexed by this method may not be in continuous series
     * however numbers have the <strong>upward direction</strong> always.
     */
    protected static synchronized int _nextRawSequence() {
        return _sequencer++;
    }

    /** Protected constructor */
    protected Property(int index) {
        this.index = index==UNDEFINED_INDEX ? _nextRawSequence() : index ;
    }

    /**
     * Property initialization.
     * @param name Replace the Name of property if the one is NULL.
     * @param index Replace index always, the value -1 invoke a next number from the internal sequencer.
     * @param type Replace the Type of property if the one is NULL.
     * @param defaultValue Replace the Optional default value if the one is NULL.
     * @param lock Lock the property.
     */
    @SuppressWarnings("unchecked")
    protected final Property<UJO,VALUE> init
    ( final String name
    , Class<VALUE> type
    , Class<UJO> domainType
    , final VALUE defaultValue
    , final int index
    , final boolean lock
    ) {
        checkLock();

        if (this.index < 0 && index >= 0) {
            this.index = index ;
        }
        if (this.name==null) {
            setName(name);
        }
        if (this.defaultValue == null) {
            this.defaultValue = defaultValue;
        }
        if (this.type == null) {
            this.type = type;
        }
        if (this.domainType == null) {
            this.domainType = domainType;
        }
        if (lock) {
            lock();
            checkValidity();
        }
        return this;
    }

    /** Lock the Property */
    protected void lock() {
        this.lock = true;
    }
    /** Check an internal log and throw an {@code IllegalStateException} if the object is locked. */
    protected final void checkLock() throws IllegalStateException {
        if (this.lock) {
            throw new IllegalArgumentException("The property is already initialized: " + this);
        }
    }

    /** The Name must not contain any dot character */
    private void setName(String name) throws IllegalArgumentException{
        if (name==null) {
            return;
        }
        if (name.length()==0) {
            final String msg = String.format("Property name '%s' must not be empty"
                    , name);
            throw new IllegalArgumentException(msg);
        }
        if (name.indexOf(PROPERTY_SEPARATOR)>0) {
            final String msg = String.format("Property name '%s' must not contain a dot character '%c'."
                    , name
                    , PROPERTY_SEPARATOR);
            throw new IllegalArgumentException(msg);
        }
        this.name = name;
    }

    /** Is the property Locked? */
    @PackagePrivate final boolean isLock() {
        return lock;
    }

    /** Check validity of keys */
    protected void checkValidity() throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null for property index: #" + index);
        }
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null in the " + this);
        }
        if (defaultValue != null && !type.isInstance(defaultValue)) {
            throw new IllegalArgumentException("Default value have not properly type in the " + this);
        }
        if (this.domainType==null) {
            throw new IllegalArgumentException("Domain type is missing for the property: " + name);
        }
    }
      
    /** Name of Property */
    @Override
    final public String getName() {
        return name;
    }
    
    /** Type of Property */
    @Override
    final public Class<VALUE> getType() {
        return type;
    }

    /** Type of Property */
    @Override
    final public Class<UJO> getDomainType() {
        return domainType;
    }

    /** Index of Property */
    @Override
    final public int getIndex() {
        return index;
    }
    
    /**
     * It is a basic method for setting an appropriate type safe value to an MapUjo object. 
     * <br>For the setting value is used internally a method 
     *     {@link AbstractUjo#writeValue(org.ujorm.Key, java.lang.Object) }
     * @see AbstractUjo#writeValue(org.ujorm.Key, java.lang.Object)
     */
    @Override
    final public void setValue(final UJO ujo, final VALUE value) {
        ujo.writeValue(this, value);
    }
    
    /**
     * A shortcut for the method {@link #of(org.ujorm.Ujo)}.
     * @see #of(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final VALUE getValue(final UJO ujo) {
        return of(ujo);
    }
    
    /**
     * It is a basic method for getting an appropriate type safe value from an Ujo object.
     * <br>For the getting value is used internally a method
     *     {@link AbstractUjo#readValue(org.ujorm.Key)}
     * </a>.
     * <br>Note: this method replaces the value of <strong>null</strong> for default
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the ujo object.
     * @see AbstractUjo#readValue(Key)
     */
    @SuppressWarnings("unchecked")
    @Override
    public VALUE of(final UJO ujo) {
        final Object result = ujo.readValue(this);
        return result!= null ? (VALUE) result : defaultValue;
    }
    
    /** Returns a Default property value. The value replace the <code>null<code> value in the method Ujo.readValue(...). 
     * If the default value is not modified, returns the <code>null<code>.
     */
    @Override
    public VALUE getDefault() {
        return defaultValue;
    }

    /** Assign a Default value. The default value may be modified after locking - at your own risk.
     * <br />WARNING: the change of the default value modifies all values in all instances with the null value of the current property!
     */
    @SuppressWarnings("unchecked")
    public <PROPERTY extends Property> PROPERTY writeDefault(VALUE value) {
        defaultValue = value;
        if (lock) checkValidity();
        return (PROPERTY) this;
    }
    
    /** Assing a value from the default value. */
    public void setValueFromDefault(UJO ujo) {
        setValue(ujo, defaultValue);
    }
        
    /** Indicates whether a parameter value of the ujo "equal to" this default value. */
    @Override
    public boolean isDefault(UJO ujo) {
        VALUE value = of(ujo);
        final boolean result
        =  value==defaultValue
        || (defaultValue!=null && defaultValue.equals(value))
        ;
        return result;
    }
    
    /**
     * Returns a true value, if the property contains more keys.
     * The composite property is excluded from from function Ujo.readKeys() by default.
     */
    @Override
    public final boolean isDirect() {
        return true;
    }

    /** A flag for a direction of sorting. This method returns true always.
     * @since 0.85
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public boolean isAscending() {
        return true;
    }

    /** Create a new instance of the <strong>indirect</strong> property with a descending direction of order.
     * @since 0.85
     * @see #isAscending()
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public UjoProperty<UJO, VALUE> descending() {
        return descending(true);
    }

    /** Create a new instance of the <strong>indirect</strong> property with a descending direction of order.
     * @since 1.21
     * @see #isAscending()
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public UjoProperty<UJO, VALUE> descending(boolean descending) {
        return PathProperty.sort(this, !descending);
    }

    /** Create new composite (indirect) instance.
     * @since 0.92
     */
    @SuppressWarnings("unchecked")
    @Override
    public <VALUE_PAR> CompositeKey<UJO, VALUE_PAR> add(final Key<? extends VALUE, VALUE_PAR> property) {
        return PathProperty.newInstance((Key)this, property);
    }

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    @Override
    public void copy(final UJO from, final UJO to) {
        to.writeValue(this, from.readValue(this));
    }

    /** Returns true if the property type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isTypeOf(final Class type) {
        return type.isAssignableFrom(this.type);
    }

    /**
     * Returns true, if the property value equals to a parameter value. The property value can be null.
     * 
     * @param ujo A basic Ujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    @Override
    public boolean equals(final UJO ujo, final VALUE value) {
        final Object myValue = of(ujo);
        if (myValue==value) { return true; }
        
        final boolean result
        =  myValue!=null
        && value  !=null
        && myValue.equals(value)
        ;
        return result;
    }

    /**
     * Returns true, if the property name equals to the parameter value.
     */
    @Override
    public boolean equalsName(final CharSequence name) {
        return name!=null && name.toString().equals(this.name);
    }

    /** Compare to another Key object by the index and name of the property.
     * @since 1.20
     */
    public int compareTo(final Key p) {
        return index<p.getIndex() ? -1
             : index>p.getIndex() ?  1
             : name.compareTo(p.getName()) 
             ;
    }

    /** A char from Name */
    @Override
    public char charAt(int index) {
        return name.charAt(index);
    }

    /** Length of the Name */
    @Override
    public int length() {
        return name.length();
    }

    /** Sub sequence from the Name */
    @Override
    public CharSequence subSequence(int start, int end) {
        return name.subSequence(start, end);
    }
    
    /** Returns a name of Property */
    @Override
    public final String toString() {
        return name;
    }

    /** Returns the name of the Key including a simple domain class. Example: Person.id */
    @Override
    public final String toStringFull() {
        return domainType!=null
             ? domainType.getSimpleName() + '.' + name
             : name ;
    }

    // --------- STATIC METHODS -------------------

    /** Returns a new instance of property where the default value is null.
     * The method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newInstance(String name, Class<VALUE> type, VALUE value, Integer index, boolean lock) {
        return new Property<UJO,VALUE>(index).init(name, type, null, value, index, lock);
    }


    /** Returns a new instance of property where the default value is null.
     * The method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newInstance(String name, Class<VALUE> type, Class<UJO> domainType, int index) {
        final boolean lock = type!=null
                    && domainType!=null;
        return new Property<UJO,VALUE>(index).init(name, type, domainType, null, index, lock);
    }

    /** Returns a new instance of property where the default value is null.
     * The method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newInstance(String name, Class<VALUE> type) {
        final Class<UJO> domainType = null;
        return newInstance(name, type, domainType, Property.UNDEFINED_INDEX);
    }

    /** Returns a new instance of property where the default value is null.
     * The method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newInstance(String name, Class<VALUE> type, Class<UJO> domainType) {
        return newInstance(name, type, domainType, Property.UNDEFINED_INDEX);
    }

    /** A Property Factory where a property type is related from from default value.
     * Method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newInstance(String name, VALUE value, int index) {
        @SuppressWarnings("unchecked")
        Class<VALUE> type = (Class) value.getClass();
        return new Property<UJO, VALUE>(index).init(name, type, null, value, index, false);
    }

    /** A Property Factory where a property type is related from from default value.
     * Method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newInstance(String name, VALUE value) {
         return newInstance(name, value, UNDEFINED_INDEX);
    }


    /** A Property Factory where a property type is related from from default value.
     * Method assigns a next property index.
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newInstance(Key p, int index) {
         return newInstance(p.getName(), p.getType(), p.getDefault(), index, true);
    }


    /** A Property Factory where a property type is related from from default value.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, VALUE> Key<UJO, VALUE> newInstance(Key p) {
         return newInstance(p.getName(), p.getType(), p.getDefault(), UNDEFINED_INDEX, false);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> where(Operator operator, VALUE value) {
        return Criterion.where(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> where(Operator operator, Key<?, VALUE> value) {
        return Criterion.where(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereEq(VALUE value) {
        return Criterion.where(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereIn(Collection<VALUE> list) {
        return Criterion.whereIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNotIn(Collection<VALUE> list) {
        return Criterion.whereNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereIn(VALUE... list) {
        return Criterion.whereIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNotIn(VALUE... list) {
        return Criterion.whereNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereEq(Key<UJO, VALUE> value) {
        return Criterion.where(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNull() {
        return Criterion.whereNull(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNotNull() {
        return Criterion.whereNotNull(this);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Criterion<UJO> whereFilled() {
        final Criterion<UJO> result = whereNotNull()
            .and(Criterion.where(this, Operator.NOT_EQ, (VALUE) getEmptyValue()))
                ;
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Criterion<UJO> whereNotFilled(){
        final Criterion<UJO> result = whereNull()
            .or(new ValueCriterion(this, Operator.EQ, getEmptyValue()))
                ;
        return result;
    }

    /** Returns an empty value */
    private Object getEmptyValue() {
        if (CharSequence.class.isAssignableFrom(type)) {
            return "";
        }
        if (type.isArray()) {
            return Array.newInstance(type, 0);
        }
        if (List.class.isAssignableFrom(type)) {
            return Collections.EMPTY_LIST;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNeq(VALUE value) {
        return Criterion.where(this, Operator.NOT_EQ, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereGt(VALUE value) {
        return Criterion.where(this, Operator.GT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereGe(VALUE value) {
        return Criterion.where(this, Operator.GE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereLt(VALUE value) {
        return Criterion.where(this, Operator.LT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereLe(VALUE value) {
        return Criterion.where(this, Operator.LE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> forSql(String sqlCondition) {
        return Criterion.forSql(this, sqlCondition);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> forAll() {
        return Criterion.forAll(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> forNone() {
        return Criterion.forNone(this);
    }

}
