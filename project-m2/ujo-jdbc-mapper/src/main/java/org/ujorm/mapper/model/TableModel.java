package org.ujorm.mapper.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.mapper.impl.Context;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public record TableModel<D>(
        DomainHandler<D> hander,
        ColumnModel<D,Object> pk,
        List<ColumnModel<D,Object>> columns,
        Map<String, ColumnModel<D,Object>> propertyMap,
        /** Inserted columns without PK. */
        List<ColumnModel<D, Object>> insertedColumns
) {

    /** Find a column model for the property name */
    @NotNull
    public ColumnModel<D,Object> getColumn(@NotNull String property) {
        var result = propertyMap.get(property);
        if (result == null) {
            var msg = "Property not found: %s.%s".formatted(hander.getDomainClass().getSimpleName(), property);
            throw new NoSuchElementException(msg);
        }
        return result;
    }

    /** Exclude PK according to the PK value. */
    @NotNull
    public List<ColumnModel<D,Object>> createInsertedColumns(@Nullable Object pkValue) {
        return pkValue == null
                ? insertedColumns()
                : columns();
    }

    /** Full database name */
    public String database() {
        return hander.getDatabaseTable();
    }

    /** Original key of Ujo API */
    public Key<D,?> pkRaw() {
        return pk.key();
    }

    /** Count of the properties */
    public int count() {
        return columns.size();
    }

    public static <D> TableModel<D> of(DomainHandler<D> handler, Context ctx) {
        var columns = handler.getKeyList().stream()
                .map(key -> ColumnModel.of(key, ctx))
                .toList();
        var pk = findPk(columns, ctx);
        var propertyMap = columns.stream().collect(
                Collectors.toUnmodifiableMap(ColumnModel::property, Function.identity()));
        var insertedColumns = columns.stream()
                .filter(c -> c != pk)
                .toList();
        return new TableModel(handler, pk, columns, propertyMap, insertedColumns);
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
