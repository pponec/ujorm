/*
 * Copyright 2018-2018 Pavel Ponec
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
package org.ujorm.tools.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A functional interface
 * @see JdbcBuilder#executeSelect(java.sql.Connection, org.ujorm.tools.jdbc.JdbcFunction)
 * @since 1.90
 */
@FunctionalInterface
public interface JdbcFunction<R> {

    /**
     * Applies this function to the given argument.
     *
     * @param rs ResultSet
     * @return the function result
     * @throws java.sql.SQLException An SQL exception
     */
    R apply(ResultSet rs) throws SQLException;

}
