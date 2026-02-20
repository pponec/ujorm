package org.ujorm.core.generator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record DatabaseModel(
        /** Name of the database table. */
        @NotNull
        String table,
        /** (Optional) The schema of the database. */
        @Nullable
        String schema,
        /** (Optional) The catalog of the database. */
        @Nullable
        String catalog
) {
    public String getQualifiedName() {
        return Stream.of(
                        catalog,
                        schema,
                        table)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining("."));
    }

}