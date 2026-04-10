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
}