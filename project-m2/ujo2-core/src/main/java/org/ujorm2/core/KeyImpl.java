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

package org.ujorm2.core;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.msg.MsgFormatter;
import org.ujorm2.CompositeKey;
import org.ujorm2.Key;
import org.ujorm2.ListKey;
import org.ujorm2.Validator;
import org.ujorm2.criterion.Criterion;
import org.ujorm2.criterion.Operator;
import org.ujorm2.criterion.ProxyValue;
import org.ujorm2.criterion.ValueCriterion;
import org.ujorm2.validator.ValidationException;

/**
 * TODO: Rename to {@code KeyImpl}
 * The main implementation of the interface {@link Key}.
 * @see AbstractUjo
 * @author Pavel Ponec
 */
@Immutable
public class KeyImpl<D, V> implements Key<D, V>, MetaInterface<D> {

    /** Property Separator character */
    public static final char PROPERTY_SEPARATOR = '.';
    /** Undefined index value */
    public static final Integer UNDEFINED_INDEX = null;

    /** Domain type type (class) */
    private final Class<D> domainClass;
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
    private BiConsumer<D, V> writer;
    /** POJO reader */
    private Function<D, V> reader;
    /** Property index */
    private int index;
    /** Property type (class) */
    private Class<V> valueClass;
    /** Property default value */
    private V defaultValue;
    /** Input Validator */
    private Validator<V> validator;

    /** Context of the Ujorm */
    @Nonnull
    private final UjoContext context;

    public KeyImpl(Class<D> domainClass, UjoContext context) {
        this.domainClass = Assert.notNull(domainClass, "domainClass");
        this.context = context != context ? context : UjoContext.of();
    }

    /** Context of the Ujorm */
    protected final UjoContext getContext() {
        return context;
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
    public final Class<V> getValueClass() {
        return valueClass;
    }

    /** Type of Property */
    @Override
    public final Class<D> getDomainClass() {
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
    public void setValue(final V value, @Nonnull final D ujo) throws ValidationException {
        writer.accept(ujo, value);
    }

    /**
     * A shortcut for the method {@link #of(org.ujorm.Ujo)}.
     * @see #of(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final V getValue(@Nonnull final D ujo) {
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
    public V of(@Nonnull final D ujo) {
                throw new UnsupportedOperationException("TODO");
    }

    /** Returns a Default key value. The value replace the {@code null} value in the method Ujo.readValue(...).
     * If the default value is not modified, returns the {@code null}.
     */
    @Override
    public V getDefaultValue() {
        return defaultValue;
    }

    /** Indicates whether a parameter value of the ujo "equal to" this default value. */
    @Override
    public boolean isDefault(@Nonnull final D ujo) {
        V value = of(ujo);
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
    public Key<D, V> descending() {
        return descending(true);
    }

    /** Create a new instance of the <strong>indirect</strong> key with a descending direction of order.
     * @since 1.21
     * @see #isAscending()
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public Key<D, V> descending(final boolean descending) {
            throw new UnsupportedOperationException("TODO");
    }

    /** Get the ujorm key validator or return the {@code null} value if no validator was assigned */
    @Override
    public Validator<V> getValidator() {
        return validator;
    }

    /** Create new composite (indirect) instance.
     * @since 0.92
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> CompositeKey<D, T> join(@Nonnull final Key<? super V, T> key) {
            throw new UnsupportedOperationException("TODO");

    }

    /** Create new composite (indirect) instance for an object type of ListKey.
     * @since 0.92
     */
    @Override
    public <T> ListKey<D, T> join(@Nonnull final ListKey<? super V, T> key) {
            throw new UnsupportedOperationException("TODO");

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> CompositeKey<D, T> join(@Nonnull final Key<? super V, T> key, final String alias) {
            throw new UnsupportedOperationException("TODO");
    }

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    @Override
    public void copy(@Nonnull final  D from, @Nonnull final D to) {
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
    public boolean equals(@Nonnull final D ujo, @Nullable final V value) {
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
        if (key instanceof KeyImpl) {
            final KeyImpl arg = (KeyImpl) key;
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
    public Criterion<D> forCriterion(@Nonnull final Operator operator, @Nullable final V value) {
        return Criterion.forCriton(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forCriterion(@Nonnull final Operator operator, @Nullable final ProxyValue<V> proxyValue) {
        return Criterion.forCriton(this, operator, proxyValue);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forCriterion(@Nonnull final Operator operator, Key<?, V> value) {
        return Criterion.forCriton(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forEq(@Nullable final V value) {
        return Criterion.forCriton(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forEq(@Nonnull final Key<D, V> value) {
        return Criterion.forCrn(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forEq(@Nonnull final ProxyValue<V> proxyValue) {
        return Criterion.forCriton(this, Operator.EQ, proxyValue);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forIn(@Nonnull final Collection<V> list) {
        return Criterion.forIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forNotIn(@Nonnull final Collection<V> list) {
        return Criterion.forNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forIn(@Nonnull final V... list) {
        return Criterion.forIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forNotIn(@Nonnull final V... list) {
        return Criterion.forNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forNull() {
        return Criterion.forNull(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forNotNull() {
        return Criterion.forNotNull(this);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public Criterion<D> forLength() {
        final Criterion<D> result = forNotNull()
            .and(Criterion.forCriton(this, Operator.NOT_EQ, (V) getEmptyValue()))
                ;
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public Criterion<D> forEmpty() {
        final Criterion<D> result = forNull()
                .or(ValueCriterion.forCriton(this, Operator.EQ, (V) getEmptyValue()));
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
    public Criterion<D> forNeq(@Nullable final V value) {
        return Criterion.forCriton(this, Operator.NOT_EQ, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forGt(@Nullable final V value) {
        return Criterion.forCriton(this, Operator.GT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forGe(@Nullable final V value) {
        return Criterion.forCriton(this, Operator.GE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forLt(@Nullable final V value) {
        return Criterion.forCriton(this, Operator.LT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forLe(@Nullable final V value) {
        return Criterion.forCriton(this, Operator.LE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forSql(String sqlCondition) {
        return Criterion.forSql(this, sqlCondition);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forSql(String sqlCondition, V value) {
        return Criterion.forSql(this, sqlCondition, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forSqlUnchecked(@Nonnull final String sqlCondition, @Nullable final Object value) {
        return Criterion.forSqlUnchecked(this, sqlCondition, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forAll() {
        return Criterion.forAll(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<D> forNone() {
        return Criterion.forNone(this);
    }

    @Override
    public D createDomain() {
        try {
            return getDomainClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Create a new Key builder */
    public KeyImpl.Builder buider() {
        Assert.isEmpty(name, "The key is locked");
        return new Builder();
    }

    // ---- INNER CLASSES ---

    /** An attribute writer */
    public final class Builder {

        private KeyImpl key = KeyImpl.this;

        /** Privare constructor */
        private Builder() {
        }

        private KeyImpl key() {
            Assert.validState(key != null, "The key is closed");
            return key;
        }

        public <K extends Key> K build() {
            final KeyImpl result = key;
            key = null;
            return (K) result;
        }

        /** The Name must not contain any dot character */
        public void setName(@Nonnull final String name) throws IllegalArgumentException {
            Assert.hasLength(name, "name");
            Assert.isFalse(isPropertySeparatorDisabled() && name.indexOf(PROPERTY_SEPARATOR) > 0,
                     "Key name '{}' must not contain a dot character '{}'.",
                     name,
                     PROPERTY_SEPARATOR);

            key().name = name.intern();
        }

        public void setWriter(@Nonnull final BiConsumer<D, V> writer) {
            key().writer = Assert.notNull(writer, "writer");
        }

        public void setReader(@Nonnull final Function<D, V> reader) {
            key().reader = Assert.notNull(reader, "reader");
        }

        public void setType(@Nonnull final Class<V> type) {
            key().valueClass = Assert.notNull(type, "type");
        }

        public void setDefaultValue(@Nullable final V defaultValue) {
            Assert.validState(valueClass != null, "type is required");
            Assert.isTrue(defaultValue == null || valueClass.isInstance(defaultValue), "defaultValue");
            key().defaultValue = defaultValue;
        }

        public void setValidator(@Nullable final Validator<V> validator) {
            key().validator = validator;
        }
    }

}
