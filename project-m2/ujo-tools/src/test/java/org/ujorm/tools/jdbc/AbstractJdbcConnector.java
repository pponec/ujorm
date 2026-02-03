/*
 *  Copyright 2018-2022 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujorm.tools.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** JDBC connection support
 * @author Pavel Ponec
 */
public abstract class AbstractJdbcConnector {


    /** Create a new DB connection */
    protected Connection createDbConnection() throws ClassNotFoundException, SQLException {
        Class.forName(org.h2.Driver.class.getName());
        Connection result = DriverManager.getConnection("jdbc:h2:mem:test", "", "");
        result.setAutoCommit(false);
        return result;
    }
}
