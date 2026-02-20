package org.ujorm.core.impl;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import java.util.Map;
import java.util.Objects;

abstract public class AbstractKey<D, V> implements Key<D, V> {

    /** Default primitive values */
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

    /** Order of the key with starting at zero. */
    @NonNull
    final int order;
    /** Simple name of the key has a canonical instance. */
    @NonNull
    final String name;
    /** Java type of the key */
    @NonNull
    final Class<V> type;
    /** Database column name */
    @NotNull
    final String columnName;
    /** Is the column primary key ? */
    final boolean isPrimaryKey;
    /** Is the database column required? */
    final boolean required;
    /** Default value */
    protected final V defaultValue;

    public AbstractKey(
            final int order,
            @NonNull final String name,
            @NonNull final Class<V> type,
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

    public final int getIndex() {
        return order;
    }

    @Override
    public final @NotNull String getName() {
        return name;
    }

    @Override
    public String getFullName() {
        return getType().getSimpleName() + '.' + name;
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

    @Override
    public final boolean isTypeOf(@NotNull final Class type) {
        return getType().isAssignableFrom(type);
    }

    @Override
    public final boolean isDomainOf(@NotNull final Class type) {
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
        final var i1 = this.getIndex();
        final var i2 = o.getIndex();
        return i1 < i2 ? -1 : i1 == i2 ? 0 : 1;
    }

    @Override
    public boolean isDefault(@NotNull final D bean) {
        final V value = getValue(bean);
        return Objects.equals(value, defaultValue);
    }

    @Override
    public @Nullable V getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the default value for the given class.
     *
     * @param clazz the class to get the default value for
     * @param <T>   the type of the class
     * @return the default value or null
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

    /** Create new exception */
    protected UnsupportedOperationException unsupportedSetter(Key<?,?> key) {
        var msg = "Setter is missing for: " + key.getFullName();
        return new UnsupportedOperationException(msg);
    }
}
