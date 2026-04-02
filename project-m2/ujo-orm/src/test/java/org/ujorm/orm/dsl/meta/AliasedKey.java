package org.ujorm.orm.dsl.meta;

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
        return "";
    }

    @Override
    public @NotNull Class<V> type() {
        return null;
    }

    @Override
    public @NotNull Class<T> domainClass() {
        return null;
    }

    @Override
    public void setValue(@NotNull T bean, @Nullable V v) throws UnsupportedOperationException {

    }

    @Override
    public V getValue(@NotNull T bean) {
        return null;
    }

    @Override
    public @Nullable V getDefaultValue() {
        return null;
    }

    @Override
    public short index() {
        return 0;
    }

    @Override
    public @NotNull KeyInfo info() {
        return this.originalKey.info();
    }

    @Override
    public Criterion where(@NotNull Operator operator, @Nullable V v) {
        return null;
    }

    @Override
    public @NotNull Criterion where(@NotNull Operator operator, @NotNull ProxyValue<V> proxyValue) {
        return null;
    }

    @Override
    public @NotNull Criterion where(@NotNull Operator operator, @NotNull Key<?, V> value) {
        return null;
    }

    @Override
    public @NotNull Criterion whereEq(@Nullable V v) {
        return null;
    }

    @Override
    public @NotNull Criterion whereEq(@NotNull ProxyValue<V> proxyValue) {
        return null;
    }

    @Override
    public @NotNull Criterion whereEq(@NotNull Key<?, V> key) {
        return null;
    }

    @Override
    public @NotNull Criterion whereIn(@NotNull Collection<V> list) {
        return null;
    }

    @Override
    public @NotNull Criterion whereNotIn(@NotNull Collection<V> list) {
        return null;
    }

    @Override
    public @NotNull Criterion whereIn(@NotNull V... list) {
        return null;
    }

    @Override
    public @NotNull Criterion whereNotIn(@NotNull V... list) {
        return null;
    }

    @Override
    public @NotNull Criterion whereNeq(@Nullable V v) {
        return null;
    }

    @Override
    public Criterion whereGt(@NotNull V v) {
        return null;
    }

    @Override
    public Criterion whereGe(@NotNull V v) {
        return null;
    }

    @Override
    public Criterion whereLt(@NotNull V v) {
        return null;
    }

    @Override
    public Criterion whereLe(@NotNull V v) {
        return null;
    }

    @Override
    public @NotNull Criterion whereNull() {
        return null;
    }

    @Override
    public @NotNull Criterion whereNotNull() {
        return null;
    }

    @Override
    public @NotNull Criterion whereAll() {
        return null;
    }

    @Override
    public @NotNull Criterion whereNone() {
        return null;
    }
}