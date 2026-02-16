package org.ujorm.core.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;

@RequiredArgsConstructor
abstract public class AbstractKey<D, V> implements Key<D, V> {

    /** Order of the key with starting at zero. */
    @NonNull
    final int order;
    /** Simple name of the key */
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
        return getDomainType().isAssignableFrom(type);
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

    /** Create new exception */
    protected UnsupportedOperationException unsupportedSetter(Key<?,?> key) {
        var msg = "Setter is missing for: " + key.getFullName();
        return new UnsupportedOperationException(msg);
    }
}
