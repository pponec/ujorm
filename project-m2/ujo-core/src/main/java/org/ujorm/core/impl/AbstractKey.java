package org.ujorm.core.impl;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract implementation of the {@link Key} interface.
 *
 * @param <D> Domain type
 * @param <V> Value type
 */
public abstract class AbstractKey<D, V> implements Key<D, V> {

    /** Default values for primitive types. */
    private static final Map<Class<?>, Object> DEFAULT_VALUES = Map.of(
            boolean.class, false,
            char.class, '\0',
            byte.class, (byte) 0,
            short.class, (short) 0,
            int.class, 0,
            long.class, 0L,
            float.class, 0.0f,
            double.class, 0.0d
    );

    /** Order of the key, starting at zero. */
    final int order;
    /** Simple name of the key has a canonical instance. */
    @NonNull
    final String name;
    /** Java type of the key value. */
    @NotNull
    final Class<V> type;
    /** Database column name. */
    @NotNull
    final String columnName;
    /** Indicates if the column is a primary key. */
    final boolean isPrimaryKey;
    /** Indicates if the database column is required. */
    final boolean required;
    /** Default value of the key. */
    protected final V defaultValue;

    public AbstractKey(
            final int order,
            @NotNull final String name,
            @NotNull final Class<V> type,
            @NotNull final String columnName,
            final boolean isPrimaryKey,
            final boolean required) {
        this.order = order;
        this.name = name.intern();
        this.type = type;
        this.columnName = columnName;
        this.isPrimaryKey = isPrimaryKey;
        this.required = required;
        this.defaultValue = getDefaultValue(type);
    }

    @Override
    public final int getIndex() {
        return order;
    }

    @Override
    public final @NotNull String getName() {
        return name;
    }

    @Override
    public String getFullName() {
        return getDomainClass().getSimpleName() + '.' + name;
    }

    @Override
    public final @NotNull Class<V> getType() {
        return type;
    }

    @Override
    public final boolean primitiveType() {
        return getType().isPrimitive();
    }

    @Override
    public @NotNull String columnName() {
        return columnName;
    }

    @Override
    public boolean primaryKey() {
        return isPrimaryKey;
    }

    @Override
    public boolean required() {
        return required;
    }

    @Override
    public V of(@NotNull final D bean) {
        return getValue(bean);
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
        return getDomainClass().isAssignableFrom(type);
    }

    @Override
    public final int length() {
        return getName().length();
    }

    @Override
    public final char charAt(final int index) {
        return getName().charAt(index);
    }

    @Override
    @NotNull
    public final CharSequence subSequence(final int start, final int end) {
        return getName().subSequence(start, end);
    }

    @Override
    public final int compareTo(@NotNull final Key o) {
        return Integer.compare(this.getIndex(), o.getIndex());
    }

    @Override
    public boolean isDefault(@NotNull final D bean) {
        final var value = getValue(bean);
        return Objects.equals(value, defaultValue);
    }

    @Override
    public @Nullable V getDefaultValue() {
        return defaultValue;
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

    /** Creates a new exception for missing setters. */
    protected UnsupportedOperationException unsupportedSetter(@NotNull Key<?, ?> key) {
        final var msg = "Setter is missing for: " + key.getFullName();
        return new UnsupportedOperationException(msg);
    }
}