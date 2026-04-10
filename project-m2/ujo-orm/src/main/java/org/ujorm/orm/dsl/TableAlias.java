package org.ujorm.orm.dsl;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.Key;

import java.util.Objects;

/** Generic wrapper for SQL table aliases */
public final class TableAlias<T> {

    @NotNull
    private final String alias;

    /** Constructor */
    private TableAlias(@NotNull String alias) {
        this.alias = Objects.requireNonNull(alias, "alias");
    }

    /** Wraps a domain property with this table alias */
    public <V> AliasedKey<T, V> key(@NotNull final Key<T, V> key) {
        var originalKey = (key instanceof AliasedKey akey) ? akey.originalKey() : key;
        return new AliasedKey<>(this, originalKey);
    }

    /** Returns the SQL table alias */
    @NotNull
    public String alias() {
        return alias;
    }

    /** Factory method */
    public static <T> TableAlias<T> of(@NotNull final String alias) {
        return new TableAlias<>(alias);
    }
}