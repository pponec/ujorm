/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujorm.gxt.client;

import java.util.ArrayList;
import java.util.List;

/**
 * A <strong>CPathProperty</strong> class is an composite of a CujoProperty objects.
 * The CPathProperty class can be used wherever is used CujoProperty - with a one important <strong>exception</strong>:
 * do not send the CPathProperty object to methods Cujo.readValue(...) and Cujo.writeValue(...) !!!
 * <p/>Note that method isDirect() returns a false in this class. For this reason, the property is not included 
 * in the list returned by Cujo.readProperties().
 * 
 * @author Pavel Ponec
 * @since 0.81
 */
final public class CPathProperty<UJO extends Cujo, VALUE> implements CujoProperty<UJO, VALUE> {

    /** Array of <strong>direct</strong> properties */
    private final CujoProperty[] properties;
    /** Is property ascending / descending */
    private final boolean ascending;
    private String name;

    public CPathProperty(List<CujoProperty> properties) {
        this(properties.toArray(new CujoProperty[properties.size()]));
    }

    /** Main constructor */
    public CPathProperty(CujoProperty... properties) {
        this(null, properties);
    }

    /** Main constructor */
    public CPathProperty(Boolean ascending, CujoProperty... properties) {
        final ArrayList<CujoProperty> list = new ArrayList<CujoProperty>(properties.length + 3);
        for (CujoProperty property : properties) {
            if (property.isDirect()) {
                list.add(property);
            } else {
                ((CPathProperty)property).exportProperties(list);
            }
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Argument must not be empty");
        }
        this.ascending = ascending!=null ? ascending : properties[properties.length-1].isAscending();
        this.properties = list.toArray(new CujoProperty[list.size()]);
    }

    /** Constructor for internal use only */
    private CPathProperty(CujoProperty[] properties, boolean ascending) {
        this.properties = properties;
        this.ascending = ascending;
    }

    /** Get the last property of the current object. The result may not be the direct property. */
    @SuppressWarnings("unchecked")
    public final <UJO_IMPL extends Cujo> CujoProperty<UJO_IMPL, VALUE> getLastPartialProperty() {
        return properties[properties.length - 1];
    }

    /** Get the first property of the current object. The result is direct property always. */
    @SuppressWarnings("unchecked")
    final public <UJO_IMPL extends Cujo> CujoProperty<UJO_IMPL, VALUE> getLastProperty() {
        CujoProperty result = properties[properties.length - 1];
        return result.isDirect()
            ? result
            : ((CPathProperty)result).getLastProperty()
            ;
    }

    /** Get the first property of the current object. The result is direct property always. */
    @SuppressWarnings("unchecked")
    final public <UJO_IMPL extends Cujo> CujoProperty<UJO_IMPL, VALUE> getFirstProperty() {
        CujoProperty result = properties[0];
        return result.isDirect()
            ? result
            : ((CPathProperty)result).getFirstProperty()
            ;
    }

    /** Full property name */
    @Override
    final public String getName() {
        if (name==null) {
            StringBuilder result = new StringBuilder(32);
            for (CujoProperty p : properties) {
                if (result.length() > 0) {
                    result.append('.');
                }
                result.append(p.getName());
            }
            name = result.toString();
        }
        return name;
    }

    /** Property type */
    @Override
    public Class<VALUE> getType() {
        return getLastPartialProperty().getType();
    }

    @Override
    public String getShortTypeName() {
        return getLastPartialProperty().getShortTypeName();
    }

    /** Get a semifinal value from an Cujo object by a chain of properties.
     * If any value (not getLastPartialProperty) is null, then the result is null.
     */
    @SuppressWarnings("unchecked")
    public Cujo getSemifinalValue(UJO ujo) {

        Cujo result = ujo;
        for (int i=0; i<properties.length-1; i++) {
            if (result==null) { return result; }
            result = (Cujo) properties[i].getValue(result);
        }
        return result;
    }

    /** Get a value from an Cujo object by a chain of properties.
     * If a value  (not getLastPartialProperty) is null, then the result is null.
     */
    @SuppressWarnings("unchecked")
    @Override
    public VALUE getValue(final UJO ujo) {
        final Cujo u = getSemifinalValue(ujo);
        return  u!=null ? getLastPartialProperty().getValue(u) : null ;
    }

    @Override
    public void setValue(final UJO ujo, final VALUE value) {
        final Cujo u = getSemifinalValue(ujo);
        getLastPartialProperty().setValue(u, value);
    }

    @Override
    final public int getIndex() {
        return -1;
    }

    /** Returns a default value */
    @Override
    public VALUE getDefault() {
        return getLastPartialProperty().getDefault();
    }

    /** Indicates whether a parameter value of the ujo "equal to" this default value. */
    public boolean isDefault(UJO ujo) {
        VALUE value = getValue(ujo);
        VALUE defaultValue = getDefault();
        final boolean result
        =  value==defaultValue
        || (defaultValue!=null && defaultValue.equals(value))
        ;
        return result;
    }

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    @Override
    public void copy(final UJO from, final UJO to) {
        final Cujo from2 = getSemifinalValue(from);
        final Cujo to2 = getSemifinalValue(to);
        getLastPartialProperty().copy(from2, to2);
    }

    /** Returns true if the property type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isTypeOf(final Class type) {
        return getLastProperty().isTypeOf(type);
    }

    /**
     * Returns true, if the property value equals to a parameter value. The property value can be null.
     *
     * @param ujo A basic Cujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    @Override
    public boolean equals(final UJO ujo, final VALUE value) {
        Object myValue = getValue(ujo);
        if (myValue==value) { return true; }

        final boolean result
        =  myValue!=null
        && value  !=null
        && myValue.equals(value)
        ;
        return result;
    }

    /**
     * Returns true, if the property value equals to a parameter value. The property value can be null.
     *
     * @param property A basic CujoProperty.
     * @param value Null value is supported.
     * @return Accordance
     */
    @Override
    public boolean equals(Object property) {
        final String t1 = this.getName();
        final String t2 = property!=null ? property.toString() : null;
        return t1.equals(t2) && getType().equals(((CujoProperty)property).getType());
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    /**
     * A shortcut for the method getValue(Cujo) .
     * @see #getValue(Cujo)
     */
    final public VALUE of(final UJO ujo) {
        return getValue(ujo);
    }

    @Override
    public String toString() {
        return getName();
    }

    /** Length of the Name */
    public int length() {
        return getName().length();
    }

    /** A char from Name */
    public char charAt(int index) {
        return getName().charAt(index);
    }

    /** Sub sequence from the Name */
    public CharSequence subSequence(int start, int end) {
        return getName().subSequence(start, end);
    }

    /**
     * Method returns a false because this is a property of the another UJO class.
     * The composite property is excluded from from function Cujo.readProperties() by default.
     */
    @Override
    public final boolean isDirect() {
        return false;
    }

    /** A flag for an ascending direction of order. For the result is significant only the last property.
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public boolean isAscending() {
        return ascending;
    }

    /** Create a new instance of the property with a descending direction of order.
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public CujoProperty<UJO,VALUE> descending() {
        return descending(true);
    }

    /** Create a new instance of the property with a descending direction of order.
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public CujoProperty<UJO,VALUE> descending(boolean descending) {
        return isAscending()==descending
                ? new CPathProperty(properties, !descending)
                : this
                ;
    }

    /** Export all <string>direct</strong> properties to the list from parameter. */
    @SuppressWarnings("unchecked")
    public void exportProperties(List<CujoProperty> result) {
        for (CujoProperty p : properties) {
            if (p.isDirect()) {
                result.add(p);
            } else {
                ((CPathProperty)p).exportProperties(result);
            }
        }
    }

    /** Create new composite (indirect) instance.
     * @since 0.92
     */
    @SuppressWarnings("unchecked")
    @Override
    public <VALUE_PAR> CujoProperty<UJO, VALUE_PAR> add(final CujoProperty<? extends VALUE, VALUE_PAR> property) {

        CujoProperty[] props = new CujoProperty[properties.length+1];
        System.arraycopy(properties, 0, props, 0, properties.length);
        props[properties.length] = property;

        return new CPathProperty(props);
    }

    /** Compare to another CujoProperty object by the index and name of the property.
     * @since 1.20
     */
    @Override
    public int compareTo(final CujoProperty p) {
        return getIndex()<p.getIndex() ? -1
             : getIndex()>p.getIndex() ?  1
             : getName().compareTo(p.getName())
             ;
    }

    /**
     * Get a cammel case from the property Name
     * @deprecated Use: {@link #getLabel()}
     * @see #getLabel()
     */
    @Override
    public String getCammelName() {
        return getLastPartialProperty().getCammelName();
    }

    @Override
    public String getLabel() {
        return getLastPartialProperty().getLabel();
    }

    
    // ================ STATIC ================

    /** Create a new instance of property with a new sort attribute value.
     * @hidden
     */
    public static <UJO extends Cujo, VALUE> CPathProperty<UJO, VALUE> sort(final CujoProperty<UJO, VALUE> property, final boolean ascending) {
        return property.isDirect()
            ? new CPathProperty<UJO, VALUE>(new CujoProperty[]{property}, ascending)
            : new CPathProperty<UJO, VALUE>(ascending, property)
            ;
    }

    /** Create a new instance of property with a new sort attribute value.
     * This is an alias for the static method {@link  #sort(org.ujorm.gxt.client.CujoProperty, boolean)  sort(..)}.
     * @hidden
     * @see #sort(org.ujorm.gxt.client.CujoProperty, boolean)  sort(..)
     */
    public static <UJO extends Cujo, VALUE> CPathProperty<UJO, VALUE> newInstance(final CujoProperty<UJO, VALUE> property, final boolean ascending) {
        return sort(property, ascending);
    }

    /** Quick instance for the direct property.
     * @hidden
     */
    public static <UJO extends Cujo, VALUE> CPathProperty<UJO, VALUE> newInstance(final CujoProperty<UJO, VALUE> property) {
        return property.isDirect()
            ? new CPathProperty<UJO, VALUE>(new CujoProperty[]{property}, property.isAscending())
            : new CPathProperty<UJO, VALUE>(property.isAscending(), property)
            ;
    }

    /** Quick instance for the direct properrites
     * @hidden
     */
    public static <UJO1 extends Cujo, UJO2 extends Cujo, VALUE> CPathProperty<UJO1, VALUE> newInstance
        ( final CujoProperty<UJO1, UJO2> property1
        , final CujoProperty<UJO2, VALUE> property2
        ) {
        return property1.isDirect() && property2.isDirect()
            ? new CPathProperty<UJO1, VALUE>(new CujoProperty[]{property1,property2}, property2.isAscending())
            : new CPathProperty<UJO1, VALUE>(property2.isAscending(), property1, property2)
            ;
    }


    /** Create new instance
     * @hidden
     */
    public static <UJO1 extends Cujo, UJO2 extends Cujo, UJO3 extends Cujo, VALUE> CPathProperty<UJO1, VALUE> newInstance
        ( final CujoProperty<UJO1, UJO2> property1
        , final CujoProperty<UJO2, UJO3> property2
        , final CujoProperty<UJO3, VALUE> property3
        ) {
        return new CPathProperty<UJO1, VALUE>(property1, property2, property3);
    }

    /** Create new instance
     * @hidden
     */
    public static <UJO1 extends Cujo, UJO2 extends Cujo, UJO3 extends Cujo, UJO4 extends Cujo, VALUE> CPathProperty<UJO1, VALUE> newInstance
        ( final CujoProperty<UJO1, UJO2> property1
        , final CujoProperty<UJO2, UJO3> property2
        , final CujoProperty<UJO3, UJO4> property3
        , final CujoProperty<UJO4, VALUE> property4
        ) {
        return new CPathProperty<UJO1, VALUE>(property1, property2, property3, property4);
    }

    /** Create new instance
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Cujo, VALUE> CPathProperty<UJO, VALUE> create(CujoProperty<UJO, ? extends Object>... properties) {
        return new CPathProperty(properties);
    }
}
