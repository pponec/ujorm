package org.ujorm.orm.dsl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.core.KeyInfo;

/** Represents a domain key bound to a specific SQL table alias */
public class AliasedKey<T, V> implements Key<T, V> {

    private final TableAlias<T> tableAlias;
    private final Key<T, V> originalKey;

    /** Constructor */
    public AliasedKey(TableAlias<T> tableAlias, Key<T, V> originalKey) {
        this.tableAlias = tableAlias;
        this.originalKey = originalKey;
    }

    /** For the Criterion implementations */
    @Override
    public @NotNull Key<T, V> self() {
        return this;
    }

    /** Returns the bound table alias */
    @Override
    public @NotNull String tableAlias() {
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

}