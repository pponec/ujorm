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
 * @author Ponec
 */
public class CProperty<UJO extends Cujo, VALUE> implements CujoProperty<UJO, VALUE> {

    private final String name;
    private final Class type;
    private final VALUE defaultValue;
    private final int index;

    public CProperty(String name, Class type, VALUE defaultValue, int index) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.index = index;
    }

    @Override
    public VALUE getValue(UJO ujo) {
        VALUE result = ujo.get(this);
        return result!=null ? result : defaultValue ;
    }

    @Override
    public void setValue(UJO ujo, VALUE value) {
        ujo.set(this, value);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public VALUE getDefault() {
        return defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Get a cammel case from the property Name
     * @deprecated Use: {@link #getLabel()}
     * @see #getLabel()
     */
    @Override
    public String getCammelName() {
        String result =
            Character.toUpperCase(name.charAt(0)) +
            name.substring(1);
        return result;
    }

    /** Returns a Label derived from the Name */
    @Override
    public String getLabel() {
        final int length = name.length();
        final StringBuilder sb = new StringBuilder(length + 3);

        boolean cLow = false;
        for (int i = 0; i < length; ++i) {
            final char c = name.charAt(i);
            if (i==0) {
                 sb.append(Character.toUpperCase(c));
            } else  {
                boolean cUp = Character.isUpperCase(c);
                if (cLow && cUp) {
                    sb.append(' ');
                }
                cLow = !cUp;
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public Class getType() {
        return type;
    }

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    @Override
    public void copy(final UJO from, final UJO to) {
        to.set(name, from.get(name));
    }

    /** Returns a short type name. */
    @Override
    public String getShortTypeName() {
        String result = type.getName();
        result = result.substring(1 + result.lastIndexOf('.'));
        return result;
    }


    /** Returns true if the property type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isTypeOf(final Class aType) {
        boolean result = this.type.equals(aType);
        if (result) {
            return result;
        }
        else if(Number.class.equals(aType)) {
            result = Short.class.equals(type) ||
                Integer.class.equals(type) ||
                Long.class.equals(type) ||
                Float.class.equals(type) ||
                Double.class.equals(type) //
                // BigInt.class.equals(type)
                // BigDecimal.class.equals(type)
                ;
        }
        else if (CEnum.class.equals(aType)) {
            result = this.getClass().equals(CPropertyEnum.class);
        }
        else if (java.util.Date.class.equals(aType)) {
            result = java.sql.Date.class.equals(type);
        }
        return result;
    }

    @Override
    public boolean isDirect() {
        return true;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean isAscending() {
        return true;
    }

    /** Create a new instance of the <strong>indirect</strong> property with a descending direction of order.
     * @see #isAscending()
     */
    @Override
    public CujoProperty<UJO, VALUE> descending() {
        return descending(true);
    }

    /** Create a new instance of the <strong>indirect</strong> property with a descending direction of order.
     * @since 1.21
     * @see #isAscending()
     */
    @Override
    public CujoProperty<UJO, VALUE> descending(boolean descending) {
        return CPathProperty.sort(this, !descending);
    }

    @Override
    public <VALUE_PAR> CujoProperty<UJO, VALUE_PAR> add(CujoProperty<? extends VALUE, VALUE_PAR> property) {
        return CPathProperty.newInstance((CujoProperty)this, property);
    }

    @Override
    public boolean equals(final UJO ujo, final VALUE value) {
        Object myValue = ujo.get(this);
        if (myValue==value) { return true; }

        final boolean result
        =  myValue!=null
        && value  !=null
        && myValue.equals(value)
        ;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return this.name.equals(obj.toString())
            && this.type.equals(((CujoProperty)obj).getType())
            ;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(CujoProperty p) {
        return index < p.getIndex() ? -1
             : index > p.getIndex() ?  1   
             : name.compareTo(p.getName())
             ;
    }
}
