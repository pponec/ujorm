/*
 * Copyright 2024-2026 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/SqlBuilder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.jdbc.SqlBuilder;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * A fluent wrapper over {@link PreparedStatement}.
 * @deprecated Use the same class from the package {@code org.ujorm.tools.jdbc} rather.
 */
@Deprecated
public class SqlParamBuilder extends SqlBuilder {

    public SqlParamBuilder(@NotNull Connection dbConnection) {
        super(dbConnection);
    }

    /** @deprecated Use the method {@link #toStream(SqlFunction)} rather. */
    @NotNull
    public <R> Stream<R> streamMap(SqlFunction<ResultSet, ? extends R> mapper ) {
        return toStream(mapper);
    }

    /** @deprecated Use the method {@link #getGeneratedKeysResultSet()}  rather. */
    @Nullable
    protected ResultSet generatedKeysRs() {
        return getGeneratedKeysResultSet();
    }

    /** @deprecated Use the method {@link #getGeneratedKeys(SqlFunction)} rather. */
    @NotNull
    public <R> Stream<R> generatedKeys(SqlFunction<ResultSet, ? extends R> mapper) {
        return getGeneratedKeys(mapper);
    }

    /** Sets a new SQL template and resets current parameters. Any existing resources are closed. */
    public SqlParamBuilder sql(@NotNull CharSequence... sqlLines) {
        return (SqlParamBuilder) super.sql(sqlLines);
    }

    // --- BINDS ---


    public SqlParamBuilder bind(@NotNull final String key, final Boolean... values) {
        return (SqlParamBuilder) super.bind(key, values);
    }

    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final Boolean... values) {
        return (SqlParamBuilder) super.bind(enabled, key, values);
    }

    /** Bind Bytes */
    public SqlParamBuilder bind(@NotNull final String key, final Byte... values) {
        return (SqlParamBuilder) super.bind(key, values);
    }

    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final Byte... values) {
        return (SqlParamBuilder) super.bind(enabled, key, values);
    }

    /** Bind Shorts */
    public SqlParamBuilder bind(@NotNull final String key, final Short... values) {
        return (SqlParamBuilder) super.bind(key, values);
    }

    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final Short... values) {
        return (SqlParamBuilder) super.bind(enabled, key, values);
    }

    /** Bind Integers */
    public SqlParamBuilder bind(@NotNull final String key, final Integer... values) {
        return (SqlParamBuilder) super.bind(key, values);
    }

    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final Integer... values) {
        return (SqlParamBuilder) super.bind(enabled, key, values);
    }

    /** Bind Longs */
    public SqlParamBuilder bind(@NotNull final String key, final Long... values) {
        return (SqlParamBuilder) super.bind(key, values);
    }

    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final Long... values) {
        return (SqlParamBuilder) super.bind(enabled, key, values);
    }

    /** Bind BigDecimal */
    public SqlParamBuilder bind(@NotNull final String key, final BigDecimal... values) {
        return (SqlParamBuilder) super.bind(key, values);
    }

    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final BigDecimal... values) {
        return (SqlParamBuilder) super.bind(enabled, key, values);
    }

    /** Bind String */
    public SqlParamBuilder bind(@NotNull final String key, final String... values) {
        return (SqlParamBuilder) super.bind(key, values);
    }

    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final String... values) {
        return (SqlParamBuilder) super.bind(enabled, key, values);
    }

    /** Bind LocalDates */
    public SqlParamBuilder bind(@NotNull final String key, final LocalDate... values) {
        return (SqlParamBuilder) super.bind(true, key, values);
    }

    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final LocalDate... values) {
        return (SqlParamBuilder) super.bind(enabled, key, values);
    }

    /** Bind LocalDateTimes */
    public SqlParamBuilder bind(@NotNull final String key, final LocalDateTime... values) {
        return (SqlParamBuilder) super.bind(key, values);
    }

    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final LocalDateTime... values) {
        return (SqlParamBuilder) super.bind(enabled, key, values);
    }

}