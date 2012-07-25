/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujorm.gxt.client;


/**
 * Client Ujo Property.
 * The object is not serializable.
 * @author Pavel Ponec
 */
public interface CujoProperty<UJO extends Cujo, VALUE> extends Comparable<CujoProperty>, CCriterionProvider<UJO,VALUE> {

    public VALUE getValue(UJO ujo);

    public void setValue(UJO ujo, VALUE value);

    public int getIndex();

    public VALUE getDefault();

    /** Returns the property name */
    public String getName();

    /**
     * Get a cammel case from the property Name
     * @deprecated Use: {@link #getLabel()}
     * @see #getLabel()
     */
    public String getCammelName();

    /** Returns a Label derived from the Name */
    public String getLabel();

    public Class getType();

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    public void copy(final UJO from, final UJO to);

    /** Returns a short type name. */
    public String getShortTypeName();

    /** Returns true if the property type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    public boolean isTypeOf(final Class type);

    public boolean isDirect();

    public boolean isAscending();

    public CujoProperty<UJO, VALUE> descending();

    public CujoProperty<UJO, VALUE> descending(boolean descending);

    public <VALUE_PAR> CujoProperty<UJO, VALUE_PAR> add(CujoProperty<? extends VALUE, VALUE_PAR> property);

    public boolean equals(UJO ujo, VALUE value);

    /** Compare names of keys */
    @Override
    public int compareTo(CujoProperty o);

    @Override
    public String toString();


}
