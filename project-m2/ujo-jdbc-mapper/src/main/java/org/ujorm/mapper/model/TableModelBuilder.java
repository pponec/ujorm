package org.ujorm.mapper.model;

import lombok.RequiredArgsConstructor;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.mapper.impl.Context;
import org.ujorm.tools.common.StreamUtils;

import java.util.List;

/** Table Model Builder */
@RequiredArgsConstructor
public class TableModelBuilder<D> {
    private final DomainHandler<D> handler;
    private final Context ctx;

    public TableModel<D> build() {
        var columns = handler.getKeyList().stream()
                .map(key -> column(key))
                .toList();
        var pk = findPk(columns);
        var propertyMap = StreamUtils.map(ColumnModel::property, columns);
        var insertedColumns = columns.stream()
                .filter(c -> c != pk)
                .toList();
        return new TableModel(handler, pk, columns, propertyMap, insertedColumns);
    }

    public <V> ColumnModel<D,V> column(Key<D,V> key) {
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

    private ColumnModel<D,?> findPk(List<? extends ColumnModel<D,?>> columns) {
        for (var col : columns) {
            if (col.pk()) return col;
        }
        var firstColumn = columns.get(0);
        if (ctx.config().isFirstPropertyIsIdentifier()) {
            return firstColumn;
        } else {
            var msg = "No primary key was found by to annotation in " + firstColumn.key().getDomainClass();
            throw new IllegalStateException(msg);
        }
    }

    /** Static builder */
    public static <D> TableModel<D> build(DomainHandler<D> handler, Context ctx) {
        return new TableModelBuilder<D>(handler, ctx).build();
    }
}
