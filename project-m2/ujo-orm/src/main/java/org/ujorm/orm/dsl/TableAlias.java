package org.ujorm.orm.dsl;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.Key;
import org.ujorm.core.composed.ComposedKeyImpl;

/** Generic wrapper for SQL table aliases */
public record TableAlias<T> (
        /** Name of the SQL table alias */
        @NotNull String alias,
        /** Original domain class */
        @NotNull Class<T> domainClass
) implements CharSequence {

    /** Wraps a domain property with this table alias. If the alias is empty, return the original key */
    public <V> Key<T, V> key(@NotNull final Key<T, V> key) {
        var originalKey = (key instanceof AliasedKey akey) ? akey.originalKey() : key;
        return originalKey.name().isEmpty() ? originalKey : new AliasedKey<>(this, originalKey);
    }

    @Override
    public int length() {
        return alias.length();
    }

    @Override
    public char charAt(int index) {
        return alias.charAt(index);
    }

    @Override
    public @NotNull CharSequence subSequence(int start, int end) {
        return alias.subSequence(start, end);
    }

    @Override
    public String toString() {
        return domainClass.getSimpleName() + (!alias.isEmpty() ? " [" + alias + ']' : "");
    }

    /**
     * Factory method for TableAlias
     * @param domainClass Domain class
     * @param alias Database table alias
     * @return A new instance of TableAlias
     */
    public static <T> TableAlias<T> of(@NotNull Class<T> domainClass, @NotNull String alias) {
        return new TableAlias(alias, domainClass);
    }

    /**
     * Create a new AliasedKey for the given table alias and key.
     * In the case of a composed key, the alias is applied to the last element of the path.
     */
    @SuppressWarnings("unchecked")
    public static <T, V> Key<T, V> aliasedKey(@NotNull String alias, @NotNull Key<T, V> key) {
        var lastPathIndex = key.pathSize() - 1;
        if (lastPathIndex <= 0) {
            return of(key.domainClass(), alias).key(key);
        }
        var items = new Key<?, ?>[lastPathIndex + 1];
        for (var i = 0; i < lastPathIndex; i++) {
            items[i] = key.pathItem(i);
        }
        var lastItem = key.pathItem(lastPathIndex);
        items[lastPathIndex] = of((Class) lastItem.domainClass(), alias).key(lastItem);
        return ComposedKeyImpl.ofDirtyKeys(items);
    }
}