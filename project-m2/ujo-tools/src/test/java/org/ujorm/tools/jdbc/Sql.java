/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm.tools.jdbc;

/** Some SQL keyw0rods. */
public interface Sql {

    // --- Statements ---

    String SELECT = "SELECT";
    String FROM = "FROM";
    String WHERE = "WHERE";
    String INSERT = "INSERT";
    String INSERT_INTO = "INSERT INTO";
    String UPDATE = "UPDATE";
    String SET = "SET";
    String DELETE = "DELETE";

    // --- Comparatos ---

    String EQ = "=";
    String GT = ">";
    String GE = ">=";
    String LT = "<";
    String LE = "<=";
    String NOT_EQ = "<>";
    String IN = "IN";
    String BETWEEN = " BETWEEN ? AND ?";

    // --- Others ---

    String VALUES = "VALUES";
    String IS_NULL = "IS NULL";
    String IS_NOT_NULL = "IS NOT NULL";
    String UNDEFINED = null;

}
