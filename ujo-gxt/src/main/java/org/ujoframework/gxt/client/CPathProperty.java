/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujoframework.gxt.client;

import java.util.ArrayList;
import java.util.List;

/**
 * A <strong>PathProperty</strong> class is an composite of a CujoProperty objects.
 * The PathProperty class can be used wherever is used CujoProperty - with a one important <strong>exception</strong>:
 * do not send the PathProperty object to methods Ujo.readValue(...) and Ujo.writeValue(...) !!!
 * <p/>You can use the preferred methods UjoManager.setValue(...) / UjoManager.getValue(...) 
 * to write and read a value instead of or use some type safe solution by UjoExt or a method of CujoProperty.
 * <p/>Note that method isDirect() returns a false in this class. For this reason, the property is not included 
 * in the list returned by Ujo.readProperties().
 * 
 * @author Pavel Ponec
 * @since 0.81
 */
public class CPathProperty<UJO extends Cujo, VALUE> implements CujoProperty<UJO, VALUE> {

    private final List<CujoProperty> properties;
    private final Boolean ascending;

    public CPathProperty(Boolean ascending, List<CujoProperty> properties) {
        this.ascending = ascending;
        this.properties = properties;
    }

    public CPathProperty(CujoProperty... properties) {
        this(null, properties);
    }

    public CPathProperty(Boolean ascending, CujoProperty... properties) {
        this.ascending = ascending;
        this.properties = new ArrayList<CujoProperty>(properties.length + 1);
        for (CujoProperty p : properties) {
            this.properties.add(p);
        }
    }

    /** Get the last property of the current object. The result may not be the direct property. */
    @SuppressWarnings("unchecked")
    final public <UJO_IMPL extends Cujo> CujoProperty<UJO_IMPL, VALUE> getLastProperty() {
        return properties.get(properties.size() - 1);
    }

    /** Get a property from selected positon.. The result may not be the direct property. */
    final public CujoProperty getProperty(final int index) {
        return properties.get(index);
    }

    /** Returns a count of properties */
    final public int getPropertyCount() {
        return properties.size();
    }

    /** Full property name */
    public String getName() {
        StringBuilder result = new StringBuilder(32);
        for (CujoProperty p : properties) {
            if (result.length() > 0) {
                result.append('.');
            }
            result.append(p.getName());
        }
        return result.toString();
    }

    /** Property type */
    @SuppressWarnings("unchecked")
    public Class<VALUE> getType() {
        return getLastProperty().getType();
    }

    @Override
    public String getShortTypeName() {
        return getLastProperty().getShortTypeName();
    }

    /** Get a semifinal value from an Ujo object by a chain of properties.
     * If a value  (not getLastProperty) is null, then the result is null.
     */
    @SuppressWarnings("unchecked")
    public Cujo getSemifinalValue(Cujo ujo) {

        Cujo result = ujo;
        for (int i = 0; i < properties.size() - 1; i++) {
            if (result == null) {
                return result;
            }
            result = (Cujo) properties.get(i).getValue(result);
        }
        return result;
    }

    /** Get a value from an Ujo object by a chain of properties.
     * If a value  (not getLastProperty) is null, then the result is null.
     */
    @SuppressWarnings("unchecked")
    @Override
    public VALUE getValue(UJO ujo) {
        Cujo u = getSemifinalValue(ujo);
        return u != null ? getLastProperty().getValue(u) : null;
    }

    @Override
    public void setValue(UJO ujo, VALUE value) {
        final Cujo u = getSemifinalValue(ujo);
        getLastProperty().setValue(u, value);
    }

    @Override
    final public int getIndex() {
        return -1;
    }

    /** Returns a default value */
    @Override
    public VALUE getDefault() {
        return getLastProperty().getDefault();
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
     * @param ujo A basic Ujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    public boolean equals(final UJO ujo, final VALUE value) {
        Object myValue = getValue(ujo);
        if (myValue == value) {
            return true;
        }

        final boolean result = myValue != null && value != null && myValue.equals(value);
        return result;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Method returns a false because this is a property of the another UJO class.
     * The composite property is excluded from from function Ujo.readProperties() by default.
     */
    @Override
    public final boolean isDirect() {
        return false;
    }

    /** A flag for an ascending direction of order. For the result is significant only the last property.
     * @see org.ujoframework.core.UjoComparator
     */
    @Override
    public boolean isAscending() {
        return ascending!=null ? ascending : getLastProperty().isAscending();
    }

    /** Create a new instance of the property with a descending direction of order.
     * @see org.ujoframework.core.UjoComparator
     */
    @Override
    public CujoProperty<UJO, VALUE> descending() {
        return isAscending() ? new CPathProperty<UJO, VALUE>(true, this) : this;
    }

    /** Export all <string>direct</strong> properties to the list from parameter. */
    @SuppressWarnings("unchecked")
    public void exportProperties(List<CujoProperty> result) {
        for (CujoProperty p : properties) {
            if (p.isDirect()) {
                result.add(p);
            } else {
                ((CPathProperty) p).exportProperties(result);
            }
        }
    }

    /** Create new composite (indirect) instance.
     * @since 0.92
     */
    @SuppressWarnings("unchecked")
    @Override
    public <VALUE_PAR> CujoProperty<UJO, VALUE_PAR> add(CujoProperty<? extends VALUE, VALUE_PAR> property) {
        
        List<CujoProperty> props = new ArrayList<CujoProperty>(this.properties.size()+1);
        props.addAll(properties);
        props.add(property);
        return new CPathProperty(props.toArray(new CujoProperty[props.size()]));
    }

    // ================ STATIC ================
    /** Create new instance
     * @hidden 
     */
    public static final  <UJO extends Cujo, VALUE      > CPathProperty<UJO, VALUE> newInstance(final  CujoProperty<UJO, VALUE> property        ) {
        return new CPathProperty<UJO, VALUE>(property  );
    }

    /** Create new instance
     * @hidden 
     */
    public static final  <UJO1 extends Cujo, UJO2 extends Cujo, VALUE      > CPathProperty<UJO1, VALUE> newInstance(final  CujoProperty<UJO1, UJO2> property1,    final  CujoProperty<UJO2, VALUE> property2
            ) {
        return new CPathProperty<UJO1, VALUE>(property1, property2  );
    }

    /** Create new instance
     * @hidden 
     */
    public static final  <UJO1 extends Cujo, UJO2      extends Cujo ,
          UJO3
          extends Cujo, VALUE> CPathProperty<UJO1, VALUE> newInstance
        ( final CujoProperty<UJO1, UJO2> property1
        , final CujoProperty<UJO2, UJO3> property2
        , final CujoProperty<UJO3, VALUE> property3
        ) {
        return new CPathProperty<UJO1, VALUE>(property1, property2, property3);
    }

    /** Create new instance
     * @hidden 
     */
    public static final <UJO1 extends Cujo, UJO2 extends Cujo, UJO3 extends Cujo, UJO4 extends Cujo, VALUE> CPathProperty<UJO1, VALUE> newInstance
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
    public static final <UJO extends Cujo, VALUE> CPathProperty<UJO, VALUE> create(CujoProperty<UJO, ? extends Object>... properties) {
        return new CPathProperty(properties);
    }

    @Override
    public String getCammelName() {
        return getLastProperty().getCammelName();
    }
}
