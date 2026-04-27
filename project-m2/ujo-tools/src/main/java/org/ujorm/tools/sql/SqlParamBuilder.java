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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

}