package org.ujorm.orm.dsl.meta;

import org.ujorm.core.Key;

/** Generic wrapper for SQL table aliases */
public class TableAlias<T> {

    private final String alias;

    /** Constructor */
    public TableAlias(String alias) {
        this.alias = alias;
    }

    /** Wraps a domain property with this table alias */
    public <V> AliasedKey<T, V> key(Key<T, V> key) {
        return new AliasedKey<>(this, key);
    }

    /** Returns the SQL table alias */
    public String alias() {
        return alias;
    }
}