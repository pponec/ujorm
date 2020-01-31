/*
 *  Copyright 2007-2020 Pavel Ponec
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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Validator;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.criterion.ProxyValue;
import org.ujorm.criterion.ValueCriterion;
import org.ujorm.tools.Assert;
import org.ujorm.tools.msg.MsgFormatter;
import org.ujorm.validator.ValidationException;

/**
 * TODO: Rename to {@code KeyImpl}
 * The main implementation of the interface {@link Key}.
 * @see AbstractUjo
 * @author Pavel Ponec
 */
@Immutable
public class Property<U,VALUE> implements Key<U,VALUE> {

    /** Property Separator character */
    public static final char PROPERTY_SEPARATOR = '.';
    /** Undefined index value */
    public static final Integer UNDEFINED_INDEX = null;

    /** Property name */
    private String name;
    /** Property index, there are exist three index ranges:
     * <ul>
     *     <li>index == UNDEFINED_INDEX
     *     : an undefine index or a signal for auto-index action</li>
     *     <li>index &lt; UNDEFINED_INDEX
     *      : the discontinuous and ascending series of numbers that is generated using a special method</li>
     *     <li>index &gt; UNDEFINED_INDEX
     *     : the continuous and ascending series of numbers usable as a pointer to an array. This is a final state</li>
     * </ul>
     */
    /** POJO writer */
    private BiConsumer<U,VALUE> writer;
    /** POJO reader */
    private Function<U,VALUE> reader;
    /** Property index */
    private int index;
    /** Property type (class) */
    private Class<VALUE> valueClass;
    /** Domain type type (class) */
    private Class<U> domainClass;
    /** Property default value */
    private VALUE defaultValue;
    /** Input Validator */
    private Validator<VALUE> validator;
    /** Attribute writer */
    private Writer attribWriter = new Writer();

    /** Lock the Property */
    protected final void lock() {
        attribWriter = null;
    }

    /** Check an internal log and throw an {@code IllegalStateException} if the object is locked. */
    @Nonnull
    public final Writer getAttribWriter() throws IllegalStateException {
        if (attribWriter == null) {
            throw new IllegalStateException("The key is already locked: "
                    + toStringDetailed());
        }
        return attribWriter;
    }

    /** Method returns the {@code true} in case the {@link #PROPERTY_SEPARATOR}
     * character is disabled in a key name.
     * The method can be overriden.
     * The {@code true} is a default value.
     */
    protected boolean isPropertySeparatorDisabled() {
        return true;
    }

    /** Check validity of keys */
    protected void checkValidity() throws IllegalArgumentException {
        Assert.hasLength(name, "name");
        Assert.notNull(valueClass, "type");
        Assert.notNull(domainClass, "domainType");
    }

    /** Name of Property */
    @Override
    public final String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public final String getFullName() {
        return domainClass != null
             ? domainClass.getSimpleName() + '.' + name
             : name ;
    }

    /** Type of Property */
    @Override
    public final Class<VALUE> getValueClass() {
        return valueClass;
    }

    /** Type of Property */
    @Override
    public final Class<U> getDomainClass() {
        return domainClass;
    }

    /** Index of Property */
    @Override
    public final int getIndex() {
        return index;
    }

    /**
     * It is a basic method for setting an appropriate type safe value to an MapUjo object.
     * <br>For the setting value is used internally a method
     *     {@link AbstractUjo#writeValue(org.ujorm.Key, java.lang.Object) }
     * @see AbstractUjo#writeValue(org.ujorm.Key, java.lang.Object)
     */
    @Override
    public void setValue(final VALUE value, @Nonnull final U ujo) throws ValidationException {
        writer.accept(ujo, value);
    }

    /**
     * A shortcut for the method {@link #of(org.ujorm.Ujo)}.
     * @see #of(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final VALUE getValue(@Nonnull final U ujo) {
        return reader.apply(ujo);
    }

    /**
     * It is a basic method for getting an appropriate type safe value from an Ujo object.
     * <br>For the getting value is used internally a method
     *     {@link AbstractUjo#readValue(org.ujorm.Key)} .
     * <br>Note: this method replaces the value of <strong>null</strong> for default
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the ujo object.
     * @see AbstractUjo#readValue(Key)
     */
    @SuppressWarnings("unchecked")
    @Override
    public VALUE of(@Nonnull final U ujo) {
                throw new UnsupportedOperationException("TODO");
    }

    /** Returns a Default key value. The value replace the {@code null} value in the method Ujo.readValue(...).
     * If the default value is not modified, returns the {@code null}.
     */
    @Override
    public VALUE getDefaultValue() {
        return defaultValue;
    }

    /** Indicates whether a parameter value of the ujo "equal to" this default value. */
    @Override
    public boolean isDefault(@Nonnull final U ujo) {
        VALUE value = of(ujo);
        final boolean result
        =  value==defaultValue
        || (defaultValue!=null && defaultValue.equals(value))
        ;
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isComposite() {
        return false;
    }

    /** A flag for a direction of sorting. This method returns true always.
     * @since 0.85
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public boolean isAscending() {
        return true;
    }

    /** Create a new instance of the <strong>indirect</strong> key with a descending direction of order.
     * @since 0.85
     * @see #isAscending()
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public Key<U, VALUE> descending() {
        return descending(true);
    }

    /** Create a new instance of the <strong>indirect</strong> key with a descending direction of order.
     * @since 1.21
     * @see #isAscending()
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public Key<U, VALUE> descending(final boolean descending) {
            throw new UnsupportedOperationException("TODO");
    }

    /** Get the ujorm key validator or return the {@code null} value if no validator was assigned */
    @Override
    public Validator<VALUE> getValidator() {
        return validator;
    }

    /** Create new composite (indirect) instance.
     * @since 0.92
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> CompositeKey<U, T> join(@Nonnull final Key<? super VALUE, T> key) {
            throw new UnsupportedOperationException("TODO");

    }

    /** Create new composite (indirect) instance for an object type of ListKey.
     * @since 0.92
     */
    @Override
    public <T> ListKey<U, T> join(@Nonnull final ListKey<? super VALUE, T> key) {
            throw new UnsupportedOperationException("TODO");

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> CompositeKey<U, T> join(@Nonnull final Key<? super VALUE, T> key, final String alias) {
            throw new UnsupportedOperationException("TODO");
    }

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    @Override
    public void copy(@Nonnull final  U from, @Nonnull final U to) {
            throw new UnsupportedOperationException("TODO");

    }

    /** Returns true if the key type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isTypeOf(@Nonnull final Class type) {
        return type.isAssignableFrom(this.valueClass);
    }

    /** Returns true if the domain type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isDomainOf(@Nonnull final Class type) {
        return type.isAssignableFrom(this.domainClass);
    }

    /**
     * Returns true, if the key value equals to a parameter value. The key value can be null.
     *
     * @param ujo A basic Ujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    @Override
    public boolean equals(@Nonnull final U ujo, @Nullable final VALUE value) {
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
     * Returns {@code true}, if the domain class and property name are the same.
     * The key value can be {@code null}.
     * @param key An another key.
     */
    @Override
    public boolean equals(@Nullable final Object key) {
        if (key instanceof Property) {
            final Property arg = (Property) key;
            return this.domainClass == arg.domainClass && this.name == arg.name;
        }
        return false;
    }

    /**
     * Returns true, if the key name equals to the parameter value.
     */
    @Override
    public boolean equalsName(final CharSequence name) {
        return name!=null && name.toString().equals(this.name);
    }

    /** Returns a native hash
     * @see java.lang.System#identityHashCode(java.lang.Object)
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /** Compare to another Key object by the index and name of the key.
     * @since 1.20
     */
    @Override
    public int compareTo(@Nonnull final Key p) {
        return index<p.getIndex() ? -1
             : index>p.getIndex() ?  1
             : name.compareTo(p.getName())
             ;
    }

    /** A char from Name */
    @Override
    public char charAt(final int index) {
        return name.charAt(index);
    }

    /** Length of the Name */
    @Override
    public int length() {
        return name.length();
    }

    /** Sub sequence from the Name */
    @Override
    public CharSequence subSequence(final int start, final int end) {
        return name.subSequence(start, end);
    }

    /** Returns a name of Property */
    @Override
    public final String toString() {
        return name;
    }

     /** Print  */
    public String toStringDetailed() {
        return MsgFormatter.format
                ( "fullName={}, "
                + "valueClass={}, "
                + "ascending={}, "
                + "composite={}, "
                + "defaultValue={}, "
                + "validator={}, "
                + "index={}"
                , getFullName()
                , getValueClass().getSimpleName()
                , isAscending()
                , isComposite()
                , getDefaultValue()
                , getValidator() != null ? getValidator().getClass().getSimpleName() : null
                , getIndex()
                );
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forCriterion(@Nonnull final Operator operator, @Nullable final VALUE value) {
        return Criterion.forCriton(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forCriterion(@Nonnull final Operator operator, @Nullable final ProxyValue<VALUE> proxyValue) {
        return Criterion.forCriton(this, operator, proxyValue);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forCriterion(@Nonnull final Operator operator, Key<?, VALUE> value) {
        return Criterion.forCriton(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forEq(@Nullable final VALUE value) {
        return Criterion.forCriton(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forEq(@Nonnull final Key<U, VALUE> value) {
        return Criterion.forCrn(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forEq(@Nonnull final ProxyValue<VALUE> proxyValue) {
        return Criterion.forCriton(this, Operator.EQ, proxyValue);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forIn(@Nonnull final Collection<VALUE> list) {
        return Criterion.forIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forNotIn(@Nonnull final Collection<VALUE> list) {
        return Criterion.forNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forIn(@Nonnull final VALUE... list) {
        return Criterion.forIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forNotIn(@Nonnull final VALUE... list) {
        return Criterion.forNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forNull() {
        return Criterion.forNull(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forNotNull() {
        return Criterion.forNotNull(this);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public Criterion<U> forLength() {
        final Criterion<U> result = forNotNull()
            .and(Criterion.forCriton(this, Operator.NOT_EQ, (VALUE) getEmptyValue()))
                ;
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public Criterion<U> forEmpty() {
        final Criterion<U> result = forNull()
                .or(ValueCriterion.forCriton(this, Operator.EQ, (VALUE) getEmptyValue()));
        return result;
    }

    /** Returns an empty value */
    @Nullable
    private Object getEmptyValue() {
        if (CharSequence.class.isAssignableFrom(valueClass)) {
            return "";
        }
        if (valueClass.isArray()) {
            return Array.newInstance(valueClass, 0);
        }
        if (List.class.isAssignableFrom(valueClass)) {
            return Collections.EMPTY_LIST;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forNeq(@Nullable final VALUE value) {
        return Criterion.forCriton(this, Operator.NOT_EQ, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forGt(@Nullable final VALUE value) {
        return Criterion.forCriton(this, Operator.GT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forGe(@Nullable final VALUE value) {
        return Criterion.forCriton(this, Operator.GE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forLt(@Nullable final VALUE value) {
        return Criterion.forCriton(this, Operator.LT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forLe(@Nullable final VALUE value) {
        return Criterion.forCriton(this, Operator.LE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forSql(String sqlCondition) {
        return Criterion.forSql(this, sqlCondition);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forSql(String sqlCondition, VALUE value) {
        return Criterion.forSql(this, sqlCondition, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forSqlUnchecked(@Nonnull final String sqlCondition, @Nullable final Object value) {
        return Criterion.forSqlUnchecked(this, sqlCondition, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forAll() {
        return Criterion.forAll(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<U> forNone() {
        return Criterion.forNone(this);
    }

    // ---- INNER CLASSES ---

    /** An attribute writer */
    public final class Writer {

        /** The Name must not contain any dot character */
        public void setName(@Nonnull final String name) throws IllegalArgumentException {
            Assert.hasLength(name, "name");
            Assert.isFalse(isPropertySeparatorDisabled() && name.indexOf(PROPERTY_SEPARATOR) > 0,
                     "Key name '{}' must not contain a dot character '{}'.",
                     name,
                     PROPERTY_SEPARATOR);

            Property.this.name = name.intern();
        }

        public void setWriter(@Nonnull final BiConsumer<U, VALUE> writer) {
            Property.this.writer = Assert.notNull(writer, "writer");
        }

        public void setReader(@Nonnull final Function<U, VALUE> reader) {
            Property.this.reader = Assert.notNull(reader, "reader");
        }

        public void setType(@Nonnull final Class<VALUE> type) {
            Property.this.valueClass = Assert.notNull(type, "type");
        }

        public void setDomainType(@Nonnull final Class<U> domainType) {
            Property.this.domainClass = Assert.notNull(domainType, "domainType");
        }

        public void setDefaultValue(@Nullable final VALUE defaultValue) {
            Assert.validState(valueClass != null, "type is required");
            Assert.isTrue(defaultValue == null || valueClass.isInstance(defaultValue), "defaultValue");
            Property.this.defaultValue = defaultValue;
        }

        public void setValidator(@Nullable final Validator<VALUE> validator) {
            Property.this.validator = validator;
        }
    }

}
