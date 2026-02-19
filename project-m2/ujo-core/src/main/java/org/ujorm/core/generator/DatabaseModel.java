package org.ujorm.core.generator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
) {}