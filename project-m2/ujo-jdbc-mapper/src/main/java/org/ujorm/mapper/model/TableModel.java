package org.ujorm.mapper.model;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.mapper.impl.Context;

import java.util.List;

public record TableModel<D>(
        DomainHandler<D> hander,
        ColumnModel<D,?> pk,
        List<ColumnModel<D,Object>> attributes
) {

    /** Full database name */
    public String database() {
        return hander.getDatabaseTable();
    }

    /** Original key of Ujo API */
    public Key<D,?> pkRaw() {
        return pk.key();
    }

    public static <D> TableModel<D> of(DomainHandler<D> handler, Context ctx) {
        var attribs = handler.getKeyList().stream()
                .map(key -> ColumnModel.of(key, ctx))
                .toList();
        var pk = findPk(attribs, ctx);
        return new TableModel(handler, pk, attribs);
    }

    private static <D> ColumnModel<D,?> findPk(List<? extends ColumnModel<D,?>> columns, Context ctx) {
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
}
