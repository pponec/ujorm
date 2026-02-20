package org.ujorm.mapper.model;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.mapper.impl.Context;

import java.sql.JDBCType;

public record AttributeModel<D>(
        Key<D,?> key,
        JDBCType jdbcType,
        /** Is foreign key to a relation */
        boolean relation
) {

    /** Column Name */
    public String column() {
        return key.columnName();
    }

    /** Is it a Primary Key? */
    public boolean pk() {
        return key.primaryKey();
    }

    public static <D> AttributeModel<D> of(Key<D,?> key, Context ctx) {
        var jdbcType = ctx.commonService().findJdbcType(key.getType());
        var relation = jdbcType == null;
        if (relation) {
            var foreignHandler = ctx.domainService().getHandler(key.getType());
            var foreighKey = ctx.commonService().findPrimaryKey(foreignHandler.getDomainClass(), ctx);
            jdbcType = ctx.commonService().findJdbcType(foreighKey.getType());
        }
        return new AttributeModel<D>(key, jdbcType, relation);
    }

}
