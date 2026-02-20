package org.ujorm.mapper.model;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.mapper.impl.Context;

import java.util.List;

public record EntityModel<D>(

        DomainHandler<D> hander,
        AttributeModel<D> pk,
        List<AttributeModel<D>> attributes
) {

    /** Full database name */
    public String database() {
        return hander.getDatabaseTable();
    }

    /** Original key of Ujo API */
    public Key<D,?> pkRaw() {
        return pk.key();
    }

    public static <D> EntityModel<D> of(DomainHandler<D> handler, Context ctx) {
        var attribs = handler.getKeyList().stream()
                .map(key -> AttributeModel.of(key, ctx))
                .toList();
        var pk = findPk(attribs, ctx);
        return new EntityModel(handler, pk, attribs);

    }

    private static <D> AttributeModel<D> findPk(List<AttributeModel<D>> columns, Context ctx) {
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
