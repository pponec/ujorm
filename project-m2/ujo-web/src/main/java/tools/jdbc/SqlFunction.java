/*
 * Copyright 2024-2024 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/JdbcFunction.java
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
package tools.jdbc;

import java.sql.SQLException;
import java.util.function.Function;

/**
 * A functional interface
 * @since 1.90
 */
@FunctionalInterface
public interface SqlFunction<T, R> extends Function<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param rs ResultSet
     * @return the function result
     */
    @Override
    default R apply(T rs) {
        try {
            return applyRs(rs);
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    R applyRs(T rs) throws SQLException;
}
