/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.cquery;

import org.ujorm.gxt.client.CPathProperty;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoManager;
import org.ujorm.gxt.client.CujoProperty;
import java.io.Serializable;
import org.ujorm.gxt.client.ClientObject;

/**
 * The value criterion implementation.
 * @since 0.90
 * @author Pavel Ponec
 */
public class CValueCriterion<UJO extends Cujo> extends CCriterion<UJO> implements Serializable {

    private static final long serialVersionUID = 42002L;
    /** True constant criterion */
    public static final CCriterion<Cujo> TRUE = new CValueCriterion<Cujo>(true);
    /** False constant criterion */
    public static final CCriterion<Cujo> FALSE = new CValueCriterion<Cujo>(false);
    transient private CujoProperty property;
    private COperator operator;
    transient protected Object value;
    //
    private String propertyName;
    private String valuePropertyName;
    private ClientObject valueObject;

    protected CValueCriterion() {
    }

    /** Creante an CCriterion constant */
    public CValueCriterion(boolean value) {
        this(null, COperator.XFIXED, value);
    }

    /** An undefined operator (null) is replaced by EQ. */
    public CValueCriterion(CujoProperty<UJO, ? extends Object> property, COperator operator, CujoProperty<UJO, Object> value) {
        this(property, operator, (Object) value);
    }

    /** An undefined operator (null) is replaced by EQ. */
    public CValueCriterion(CujoProperty<UJO, ? extends Object> property, COperator operator, Object value) {

        if (property == null) {
            value = (Boolean) value; // Type test for the CriterionConstant.
        }
        if (operator == null) {
            operator = COperator.EQ;  // The default operator.
        }

        // A validation test:
        switch (operator) {
            case EQUALS_CASE_INSENSITIVE:
            case STARTS:
            case STARTS_CASE_INSENSITIVE:
            case ENDS:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS:
            case CONTAINS_CASE_INSENSITIVE:
                makeCharSequenceTest(property);
                makeCharSequenceTest(value);
                break;
        }

        this.property = property;
        this.value = value;
        this.operator = operator;

        prepareSerialization();
    }

    /** The initialization for a serialization. */
    private void prepareSerialization() {
        this.propertyName = property != null ? property.getName() : null;
        if (value != null &&
            (CujoProperty.class.equals(value.getClass()) ||
            CPathProperty.class.equals(value.getClass()))) {
            valuePropertyName = value.toString();
        } else {
            valueObject = new ClientObject(value);
        }
    }

    /** Returns the left node of the parrent */
    @Override
    public final CujoProperty getLeftNode() {
        return property;
    }

    /** Returns the right node of the parrent */
    @Override
    public final Object getRightNode() {
        return value;
    }

    /** Returns an operator */
    @Override
    public final COperator getOperator() {
        return operator;
    }

    @SuppressWarnings({"unchecked", "fallthrough"})
    @Override
    public boolean evaluate(UJO ujo) {
        if (isConstant()) {
            return (Boolean) value;
        }
        Object value2 = value instanceof CujoProperty
            ? ((CujoProperty) value).getValue(ujo)
            : value;
        boolean caseInsensitve = true;

        switch (operator) {
            case EQ:
            case NOT_EQ:
                boolean result = property.equals(ujo, value2);
                return operator == COperator.EQ ? result : !result;
            case REGEXP:
            case NOT_REGEXP:
                throw new UnsupportedOperationException(operator.toString());
            case STARTS:
            case ENDS:
            case CONTAINS:
                caseInsensitve = false;
            case EQUALS_CASE_INSENSITIVE:
            case STARTS_CASE_INSENSITIVE:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS_CASE_INSENSITIVE: {
                Object object = property.getValue(ujo);
                if (object == value2) {
                    return true;
                }
                if (object == null || value2 == null) {
                    return false;
                }

                String t1 = object.toString();
                String t2 = value2.toString();

                if (caseInsensitve) {
                    throw new IllegalStateException("State:" + operator);
                }

                switch (operator) {
                    case EQUALS_CASE_INSENSITIVE:
                        return t1.equals(t2);
                    case STARTS:
                    case STARTS_CASE_INSENSITIVE:
                        return t1.startsWith(t2);
                    case ENDS:
                    case ENDS_CASE_INSENSITIVE:
                        return t1.endsWith(t2);
                    case CONTAINS:
                    case CONTAINS_CASE_INSENSITIVE:
                        return t1.contains(t2);
                    default:
                        throw new IllegalStateException("State:" + operator);
                }
            }
        }

        Comparable val2 = (Comparable) value2;
        if (null == val2) {
            return false;
        }
        Comparable val1 = (Comparable) property.getValue(ujo);
        if (null == val1) {
            return false;
        }
        int result = compare(val1, val2);

        switch (operator) {
            case LT:
                return result < 0;
            case LE:
                return result <= 0;
            case GT:
                return result > 0;
            case GE:
                return result >= 0;
        }

        throw new IllegalArgumentException("Illegal operator: " + operator);
    }

    /** Test a value is instance of CharSequence or a type CujoProperty is type of CharSequence.
     * If property is not valid than throw Exception.
     */
    protected void makeCharSequenceTest(Object value) throws IllegalArgumentException {
        if (value instanceof String 
        ||  value instanceof CujoProperty && ((CujoProperty) value).isTypeOf(String.class)) {
            return;
        } else {
            final String msg = "Property type must be the String";
            throw new IllegalArgumentException(msg);
        }
    }

    /** Compare two object */
    @SuppressWarnings("unchecked")
    protected int compare(final Comparable o1, final Comparable o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return +1;
        }
        if (o2 == null) {
            return -1;
        }
        return o1.compareTo(o2);
    }

    /** Is the operator insensitive. */
    public boolean isInsensitive() {
        switch (operator) {
            case EQUALS_CASE_INSENSITIVE:
            case STARTS_CASE_INSENSITIVE:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS_CASE_INSENSITIVE:
                return true;
            default:
                return false;
        }
    }

    /** Is the criterion result independent on the bean object? */
    public final boolean isConstant() {
        return operator == COperator.XFIXED;
    }

    @Override
    public String toString() {
        String result = isConstant()
            ? ""
            : (property + " " + operator.name() + " ");
        return result + value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void restore(Class<UJO> ujoType) {

        property = CujoManager.findIndirectProperty(ujoType, propertyName);
        if (valuePropertyName != null) {
            value = CujoManager.findIndirectProperty(ujoType, valuePropertyName);
        } else {
            value = valueObject.getValue();
        }
    }
}
