package org.ujorm.core.impl;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.core.KeyInfo;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.Operator;
import org.ujorm.core.criterion.ProxyValue;
import org.ujorm.tools.common.Primitive;

import java.util.Collection;
import java.util.Map;

/**
 * Abstract implementation of the {@link Key} interface.
 *
 * @param <D> Domain type
 * @param <V> Value type
 */
public abstract class AbstractKey<D, V> implements Key<D, V>, KeyInfo<V> {

    /** Default values for primitive types. */
    private static final Map<Class<?>, Object> DEFAULT_VALUES = Primitive.ofAllToMap(
            Primitive::primitiveClass,
            Primitive::defaultValue
    );

    /** Order of the key, starting at zero. */
    final short index;
    /** Simple name of the key has a canonical instance. */
    @NotNull
    final String name;
    /** Java type of the key value. */
    @NotNull
    final Class<V> type;
    /** Database column name. */
    @NotNull
    final String columnLabel;
    /** Indicates if the column is a primary key. */
    final boolean primaryKey;
    /** Indicates if the column is a foreign key. */
    final boolean foreignKey;
    /** Indicates if the database column is required. */
    final boolean required;
    /** Default value of the key. */
    protected final V defaultValue;
    /** Determines whether to map the Enum by its ordinal (true) or name (false). */
    final boolean mapEnumByOrdinal;

    public AbstractKey(
            final int index,
            @NotNull final String name,
            @NotNull final Class<V> type,
            @NotNull final String columnLabel,
            final boolean primaryKey,
            final boolean foreignKey,
            final boolean required,
            final boolean mapEnumByOrdinal) {
        this.index = toShortIndex(index, name);
        this.name = name.intern();
        this.type = type;
        this.columnLabel = columnLabel.intern();
        this.primaryKey = primaryKey;
        this.foreignKey = foreignKey;
        this.required = required;
        this.defaultValue = getDefaultValue(type);
        this.mapEnumByOrdinal = mapEnumByOrdinal;
    }

    /** Convert int to short index. */
    private short toShortIndex(int index, @NotNull String attribute) {
        if (index < 0 || index > Short.MAX_VALUE) {
            var msg = "Index %s of %s is out of bounds [0, %d]".formatted(index, attribute, Short.MAX_VALUE);
            throw new IllegalArgumentException(msg);
        }
        return (short) index;
    }

    @Override
    public final short index() {
        return index;
    }

    @Override
    public final @NotNull String name() {
        return name;
    }

    @Override
    public final @NotNull Class<V> type() {
        return type;
    }

    @Override
    public @NotNull String columnLabel() {
        return columnLabel;
    }

    @Override
    public boolean primaryKey() {
        return primaryKey;
    }

    @Override
    public boolean foreignKey() {
        return foreignKey;
    }

    @Override
    public boolean required() {
        return required;
    }

    @Nullable
    @Override
    public final V getDefault() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isTypeOf(@NotNull final Class<?> type) {
        return this.type.isAssignableFrom(type);
    }

    @Override
    public final boolean isDomainOf(@NotNull final Class<?> type) {
        return domainClass().isAssignableFrom(type);
    }

    @Override
    public final int length() {
        return name().length();
    }

    @Override
    public final char charAt(final int index) {
        return name().charAt(index);
    }

    @Override
    @NotNull
    public final CharSequence subSequence(final int start, final int end) {
        return name().subSequence(start, end);
    }

    @Override
    public final int compareTo(@NotNull final Key o) {
        return Integer.compare(this.index(), o.index());
    }

    @Override
    public @Nullable V getDefaultValue() {
        return defaultValue;
    }

    @NotNull
    public @Nullable boolean mapEnumByOrdinal() {
        return mapEnumByOrdinal;
    }

    /**
     * Returns the default value for the given class.
     *
     * @param clazz The class to get the default value for.
     * @param <T>   The type of the class.
     * @return The default value or null.
     */
    @SuppressWarnings("unchecked")
    static <T> T getDefaultValue(final Class<T> clazz) {
        return (clazz != null && clazz.isPrimitive() && clazz != void.class)
                ? (T) DEFAULT_VALUES.get(clazz)
                : null;
    }

    /** The object should be a singleton within its working context. */
    @Override
    public final boolean equals(final Object object) {
        return this == object;
    }

    /** The object should be a singleton within its working context. */
    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public final KeyInfo<V> info() {
        return this;
    }

    // --- CRITERIONS ---


    /** {@inheritDoc} */
    @Override
    public Criterion where(@NotNull final Operator operator, @Nullable final V value) {
        return Criterion.where(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion where(@NotNull final Operator operator, @Nullable final ProxyValue<V> proxyValue) {
        return Criterion.where(this, operator, proxyValue);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion where(@NotNull final Operator operator, Key<?, V> value) {
        return Criterion.where(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion whereEq(@Nullable final V value) {
        return Criterion.where(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion whereEq(@NotNull final Key<?, V> value) {
        return Criterion.where(this, Operator.EQ, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion whereEq(@NotNull final ProxyValue<V> proxyValue) {
        return Criterion.where(this, Operator.EQ, proxyValue);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion whereIn(@NotNull final Collection<V> list) {
        return Criterion.whereIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion whereNotIn(@NotNull final Collection<V> list) {
        return Criterion.whereNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion whereNull() {
        return Criterion.whereNull(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion whereNotNull() {
        return Criterion.whereNotNull(this);
    }

    @Override
    public Criterion whereNeq(@Nullable final V value) {
        return Criterion.where(this, Operator.NOT_EQ, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion whereGt(@Nullable final V value) {
        return Criterion.where(this, Operator.GT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion whereGe(@Nullable final V value) {
        return Criterion.where(this, Operator.GE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion whereLt(@Nullable final V value) {
        return Criterion.where(this, Operator.LT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion whereLe(@Nullable final V value) {
        return Criterion.where(this, Operator.LE, value);
    }

    @Override
    public @NotNull Criterion whereAll() {
        return Criterion.forAll(this);
    }

    @Override
    public @NotNull Criterion whereNone() {
        return Criterion.forNone(this);
    }

    /** Creates a new exception for missing setters. */
    protected UnsupportedOperationException unsupportedSetter(@NotNull Key<?, ?> key) {
        final var msg = "Setter is missing for: " + key.fullName();
        return new UnsupportedOperationException(msg);
    }
}