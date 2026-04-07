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
    public String tableAlias() {
        return tableAlias.alias();
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

    /** Key name by a template {@code Employee.e:name} . */
    @Override
    public @NotNull String fullName() {
        return originalKey.domainClass().getSimpleName() + '.' + tableAlias() + ':' + originalKey.name();
    }

    /** Returns a key name including a table alias according to the template: {@code e:name} . */
    @Override
    public String toString() {
        return tableAlias() + ':' + originalKey.name();
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
    @NotNull
    public Criterion whereSql(@NotNull String template, V... values) {
        return Criterion.forSql(this, template, values);
    }

    @Override
    public @NotNull Criterion whereTrue() {
        return Criterion.forAll(this);
    }

    @Override
    public @NotNull Criterion whereFalse() {
        return Criterion.forNone(this);
    }

}