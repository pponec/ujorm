package org.ujorm.mapper.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.mapper.impl.Context;

import java.sql.JDBCType;

public class CommonService {

    /** Find PK. */
    public <D> Key<D,?> findPrimaryKey(Class<D> clazz, Context ctx) {
        var handler = ctx.domainService().getHandler(clazz);
        for (var key : handler.getKeyList()) {
            if (key.primaryKey()) return key;
        }
        if (ctx.config().isFirstPropertyIsIdentifier()) {
            return handler.getKeyList().get(0);
        } else {
            var msg = "No primary key was found by to annotation in " + clazz;
            throw new IllegalStateException(msg);
        }

    }

    @Nullable
    public JDBCType findJdbcType(@NotNull Class<?> clazz) throws IllegalArgumentException {
        return JdbcTypeProvider.findJdbcType(clazz);
    }

}
