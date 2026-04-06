/*
 * Copyright 2024-2026 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/SqlBuilder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.orm.dsl;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.Key;
import org.ujorm.tools.jdbc.SQLException;

/**
 * A fluent wrapper over {@link java.sql.PreparedStatement}
 * that manages named parameters and ensures automatic resource cleanup
 * of both statements and result sets.
 * This class has no dependencies other than its abstract parent, annotations, and {@link SQLException}.
 *
 * @since 2.26
 */
public interface DslQueryWriter {

    /** Write database table name. */
    void writeTableName(@NotNull String tableAlias, @NotNull Class<?> entityClass);

    /** Write database column name. */
    void writeColumnName(@NotNull String tableAlias, @NotNull Key<?,?> column, Key<?,?>... labels);

    StringBuilder append(String str);

    StringBuilder append(char str);

    default StringBuilder append(Object obj) {
        return append(String.valueOf(obj));
    }

}