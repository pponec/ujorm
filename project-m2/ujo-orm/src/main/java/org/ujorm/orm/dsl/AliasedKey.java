package org.ujorm.orm.dsl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.core.KeyInfo;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.Operator;
import org.ujorm.core.criterion.ProxyValue;

import java.util.Collection;

/** Represents a domain key bound to a specific SQL table alias */
public class AliasedKey<T, V> implements Key<T, V> {

    private final TableAlias<T> tableAlias;
    private final Key<T, V> originalKey;

    /** Constructor */
    public AliasedKey(TableAlias<T> tableAlias, Key<T, V> originalKey) {
        this.tableAlias = tableAlias;
        this.originalKey = originalKey;
    }

    /** Returns the bound table alias */
    public TableAlias<T> tableAlias() {
        return tableAlias;
    }

    /** Returns the original domain key */
    public Key<T,V> originalKey() {
        return originalKey;
    }

    @Override
    public @NotNull String name() {
        return originalKey.name();
    }

    @Override
    public @NotNull Class<V> type() {
        return originalKey.type();
    }

    @Override
    public @NotNull Class<T> domainClass() {
        return originalKey.domainClass();
    }

    @Override
    public void setValue(@NotNull T bean, @Nullable V value) throws UnsupportedOperationException {
        originalKey.setValue(bean, value);
    }

    @Override
    public V getValue(@NotNull T bean) {
        return originalKey.getValue(bean);
    }

    @Override
    public @Nullable V getDefaultValue() {
        return originalKey.getDefaultValue();
    }

    @Override
    public short index() {
        return originalKey.index();
    }

    @Override
    public @NotNull KeyInfo info() {
        return this.originalKey.info();
    }

    // --- CRITERIONS ---

    @Override
    public @NotNull Criterion where(@NotNull Operator operator, @Nullable V value) {
        return originalKey.where(operator, value);
    }

    @Override
    public @NotNull Criterion where(@NotNull Operator operator, @NotNull ProxyValue<V> proxyValue) {
        return originalKey.where(operator, proxyValue);
    }

    @Override
    public @NotNull Criterion where(@NotNull Operator operator, @NotNull Key<?, V> value) {
        return originalKey.where(operator, value);
    }

    @Override
    public @NotNull Criterion whereIn(@NotNull Collection<V> values) {
        return originalKey.whereIn(values);
    }

    @Override
    public @NotNull Criterion whereNotIn(@NotNull Collection<V> values) {
        return originalKey.whereNotIn(values);
    }

    @Override
    public @NotNull Criterion whereAll() {
        return originalKey.whereAll();
    }

    @Override
    public @NotNull Criterion whereNone() {
        return originalKey.whereNone();
    }
}