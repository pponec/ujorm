package org.ujorm.orm.dsl;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.Key;

import java.util.Objects;

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
     * @param alias Table alias
     * @return A new instance of TableAlias
     */
    public static <T> TableAlias<T> of(@NotNull Class<T> domainClass, @NotNull String alias) {
        return new TableAlias(alias, domainClass);
    }

    /**
     * Create a new AliasedKey for the given table alias and key
     * @param alias Table alias
     * @param key Original key
     * @return A new instance of AliasedKey
     */
    public static <T,V> Key<T,V> aliasedKey(@NotNull String alias, @NotNull Key<T,V> key) {
        return of(key.domainClass(), alias).key(key);
    }
}