package org.ujorm.mapper.model;

import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.mapper.impl.Context;

import java.sql.JDBCType;

public record ColumnModel<D,V>(
        Key<D,V> key,
        JDBCType jdbcType,
        /** Is foreign key to a relation */
        @Nullable
        Key<V,?> foreignKey
) {

    /** Column Name */
    public String name() {
        return key.columnName();
    }

    /** Java Property Name */
    public String property() {
        return key.getName();
    }

    /** Column index */
    public int index() {
        return key.getIndex();
    }

    /** Column value */
    @SuppressWarnings("unchecked")
    public <T> T valueOf(D domain) {
        return (T) key.getValue(domain);
    }

    /** Is it a Primary Key? */
    public boolean pk() {
        return key.primaryKey();
    }

    /** Return a domain Key for a common value type. */
    @SuppressWarnings("unchecked")
    public Key<D,Object> keyObject() {
        return (Key<D,Object>) key;
    }

    public boolean relation() {
        return foreignKey != null;
    }

    /** Returns a full name */
    @Override
    public String toString() {
        return key.getFullName();
    }

    public static <D,V> ColumnModel<D,V> of(Key<D,V> key, Context ctx) {
        var jdbcType = ctx.commonService().findJdbcType(key.getType());
        var relation = jdbcType == null;
        var foreignKey = (Key<V,?>) null;
        if (relation) {
            var foreignHandler = ctx.domainService().getHandler(key.getType());
            foreignKey = (Key<V,?>) ctx.commonService().findPrimaryKey(foreignHandler.getDomainClass(), ctx);
            jdbcType = ctx.commonService().findJdbcType(foreignKey.getType());
        }
        return new ColumnModel<>(key, jdbcType, foreignKey);
    }

}
