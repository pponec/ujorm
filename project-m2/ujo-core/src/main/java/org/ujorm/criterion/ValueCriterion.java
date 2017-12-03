/*
 *  Copyright 2007-2016 Pavel Ponec
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

package org.ujorm.criterion;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.core.KeyRing;
import org.ujorm.core.UjoCoder;
import org.ujorm.tools.Assert;
import org.ujorm.tools.MsgFormatter;
import static org.ujorm.core.UjoTools.SPACE;

/**
 * The value criterion implementation.
 * @since 0.90
 * @author Pavel Ponec
 */
public class ValueCriterion<U extends Ujo> extends Criterion<U> implements Serializable {
    static final long serialVersionUID = 20140128L;

    /** True constant criterion */
    public static final Criterion<Ujo> TRUE  = new ValueCriterion<>(true);
    /** False constant criterion */
    public static final Criterion<Ujo> FALSE = new ValueCriterion<>(false);

    private Key<U, Object> key;
    private Operator operator;
    protected Object value;

    /** Create an Criterion constant */
    public ValueCriterion(final boolean value) {
        this(null, Operator.XFIXED, value);
    }

    /** An undefined operator (null) is replaced by EQ. */
    public ValueCriterion
        ( @Nullable final Key<U,? extends Object> key
        , @Nullable final Operator operator
        , @Nullable final Key<U,Object> value) {
        this(key, operator, (Object) value);
    }

    /** An undefined operator (null) is replaced by EQ. */
    public ValueCriterion
        ( @Nullable final Key<U,? extends Object> key
        , @Nullable Operator operator
        , @Nullable Object value) {

        if (key==null) {
            value = (Boolean) value; // Type test for the CriterionConstant.
        }
        if (operator==null) {
            operator = Operator.EQ;  // The default operator.
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
                 makeCharSequenceTest(key);
                 makeCharSequenceTest(value);
                 break;
            case IN:
            case NOT_IN:
                 makeArrayTest(value);
                 break;
            case XSQL:
                 String template = value instanceof TemplateValue
                      ? ((TemplateValue)value).getTemplate()
                      : String.valueOf(value);

                 Assert.isFalse(value==null || template.trim().isEmpty(), "Value must not be empty");
                 break;
        }

        this.key = (Key<U, Object>) key;
        this.value = value;
        this.operator = operator;
    }

    /** Returns the left node of the parent */
    @Override
    public final Key<?,?> getLeftNode() {
        return key;
    }

    /** Returns the right node of the parent */
    @Override
    public Object getRightNode() {
        return value;
    }

    /** Returns an operator */
    @Override
    public final Operator getOperator() {
        return operator;
    }

    /** Join this instance with a second criterion by an operator with a simple logical optimization. */
    @Override
    public Criterion<U> join(final BinaryOperator operator, final Criterion<U> criterion) {
        if (this.operator == Operator.XFIXED) {
            switch (operator) {
                case OR : return (Boolean) value ? this : criterion;
                case AND: return (Boolean) value ? criterion : this;
            }
        }
        return super.join(operator, criterion);
    }

    @SuppressWarnings({"unchecked", "fallthrough"})
    @Override
    public boolean evaluate(final U ujo) {
        if (operator==Operator.XSQL) {
            final String msg = MsgFormatter.format("The operator {} can't evaluate '{}'"
                    , operator
                    , getRightNode());
            throw new UnsupportedOperationException(msg);
        }

        if (operator==Operator.XFIXED) {
            return (Boolean) value;
        }
        final Object myValue = value instanceof Key
            ? ((Key)value).of(ujo)
            : getRightNode();
            ;
        boolean caseInsensitve = true;

        switch (operator) {
            case EQ:
            case NOT_EQ:
                boolean result = key.equals(ujo, myValue);
                return operator==Operator.EQ ? result : !result ;
            case REGEXP:
            case NOT_REGEXP:
                Pattern p = myValue instanceof Pattern
                    ? (Pattern) myValue
                    : Pattern.compile(myValue.toString())
                    ;
                Object val1 = key.of(ujo);
                boolean result2 = val1!=null && p.matcher(val1.toString()).matches();
                return operator==Operator.REGEXP ? result2 : !result2 ;
            case STARTS:
            case ENDS:
            case CONTAINS:
                 caseInsensitve = false;
            case EQUALS_CASE_INSENSITIVE:
            case STARTS_CASE_INSENSITIVE:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS_CASE_INSENSITIVE: {
                Object object = key.of(ujo);
                if (object==myValue              ) { return true ; }
                if (object==null || myValue==null) { return false; }

                String t1 = object.toString();
                String t2 = myValue.toString();

                if (caseInsensitve) {
                    t1 = t1.toUpperCase(Locale.ENGLISH);
                    t2 = t2.toUpperCase(Locale.ENGLISH);
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
                         throw new IllegalUjormException("State:" + operator);
                }
            }
            case NOT_IN:
                 caseInsensitve = false; // match result
            case IN:
                for(Object o : (Object[]) myValue) {
                    if (key.equals(ujo, o)) {
                        return caseInsensitve;
                    }
                }
                return !caseInsensitve;
        }

        final Comparable val2 = (Comparable) myValue;
        if (null== val2)  {return false; }
        final Comparable val1 = (Comparable) key.of(ujo);
        if (null== val1)  {return false; }
        int result = compare(val1, val2);

        switch (operator) {
            case LT: return result< 0;
            case LE: return result<=0;
            case GT: return result> 0;
            case GE: return result>=0;
        }

        throw new IllegalArgumentException("Illegal operator: " + operator);
    }

    /** Returns a list of items which satisfies the condition in this Criterion.
     * @see org.ujorm.criterion.CriteriaTool#select(java.util.List, org.ujorm.criterion.Criterion, org.ujorm.core.UjoComparator)
     */
    @Override
    public final List<U> evaluate(final Iterable<U> ujoList) {
        switch (operator) {
            case XFIXED:
                return Boolean.FALSE.equals(getRightNode())
                     ? Collections.<U>emptyList()
                     : ujoList instanceof List
                     ? (List) ujoList
                     : ujoList instanceof Collection
                     ? new ArrayList<>((Collection) ujoList)
                     : super.evaluate(ujoList);
            default:
                return super.evaluate(ujoList);
        }
    }

    /** Returns a list of items which satisfies the condition in this Criterion.
     * @see org.ujorm.criterion.CriteriaTool#select(java.util.List, org.ujorm.criterion.Criterion, org.ujorm.core.UjoComparator)
     */
    @Override
    public final List<U> evaluate(final U ... ujoList) {
        switch (operator) {
            case XFIXED:
                return Boolean.FALSE.equals(value)
                     ? Collections.<U>emptyList()
                     : Arrays.asList(ujoList);
            default:
                return super.evaluate(ujoList);
        }
    }

    /** Test a value is an instance of CharSequence or a type Key is type of CharSequence.
     * If parameter is not valid than method throws Exception.
     */
    protected void makeCharSequenceTest(Object value) throws IllegalArgumentException {
        final boolean ok = value instanceof CharSequence
        || value instanceof Key
        && ((Key)value).isTypeOf(CharSequence.class);

        Assert.isTrue(ok, "Key type must be a {}", CharSequence.class);
    }

    /** Test a value is an instance of Iterable.
     * If parameter is not valid than method throws Exception.
     */
    protected final void makeArrayTest(Object value) throws IllegalArgumentException {
        Assert.isTrue(value instanceof Object[], "Value must be an Array type only");
    }


    /** Compare two object */
    @SuppressWarnings("unchecked")
    protected int compare
        ( final Comparable o1
        , final Comparable o2
    ) {
        if (o1==o2  ) { return  0; }
        if (o1==null) { return +1; }
        if (o2==null) { return -1; }
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

    /** Is the operator have got value XFIXED or XSQL ? */
    public final boolean isConstant() {
        return operator==Operator.XFIXED || operator==Operator.XSQL;
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder(256).append('(');

        if (operator==Operator.XSQL) {
            out.append(getRightNode());
            return out.append(')').toString();
        }

        if (operator!=Operator.XFIXED) {
            out
            .append(key)
            .append(SPACE)
            .append(operator.name())
            .append(SPACE);
        }

        printValue(getRightNode(), out);
        return out.append(')').toString();
    }

    /** Print an Ujo value
     * @param value Not null value
     */
    @SuppressWarnings("unchecked")
    protected void printValue(final Object value, final StringBuilder out) {
        if (value instanceof Ujo) {
            final Ujo ujo = (Ujo) value;
            final Key firstProperty = ujo.readKeys().get(0);
            final Object firstValue = firstProperty.of(ujo);

            out.append(ujo.getClass().getSimpleName());
            out.append('[');
            out.append(firstProperty);
            out.append('=');
            printValue(firstValue, out);
            out.append(']');
        } else {
            if (value instanceof CharSequence) {
                String quotation = value instanceof Key ? "" : "\"";
                out.append(quotation)
                   .append(value)
                   .append(quotation)
                   ;
            } else if (value instanceof Object[]) {
                boolean first = true;
                for (Object object : (Object[]) value) {
                    if (first) {
                        first = !first;
                    } else {
                        out.append(", ");
                    }
                    printValue(object, out);
                }
            } else {
                out.append(value instanceof Number
                    ? value.toString()
                    : new UjoCoder().encodeValue(value, false)
                );
            }
        }
    }

    /** Find a domain class type of {@code Class<UJO>} from its keys.
     * @return returns Method returns the {@code Ujo.class} instance if no domain was found.
     */
    @Override
    public Class<?> getDomain() {
        final Key key = getLeftNode();
        final Class<?> result = key != null ? key.getDomainType() : null;
        return result != null ? result : Ujo.class;
    }

    // -------------- SERIALIZATION METHOD(S) --------------

    /** Serialization method */
    @SuppressWarnings({"unused", "unchecked"})
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(operator);
        boolean valueIsKey = value instanceof Key;
        out.writeBoolean(valueIsKey);

        if (valueIsKey) {
            out.writeObject(KeyRing.of(key.getDomainType(), key, (Key) value));
        } else {
            out.writeObject(KeyRing.of(key.getDomainType(), key));
            out.writeObject(value);
        }
    }

    /** De-serialization method */
    @SuppressWarnings({"unused", "unchecked"})
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.operator = (Operator) in.readObject();
        final boolean valueIsKey = in.readBoolean();
        final KeyRing keyRing = (KeyRing) in.readObject();
        this.key = keyRing.get(0);
        this.value = valueIsKey
                ? keyRing.get(1)
                : in.readObject();
    }

}
