/*
 *  Copyright 2009 Paul Ponec
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

package org.ujoframework.orm.dialect;

import org.ujoframework.orm.SqlDialect;

/** MySQL (http://dev.mysql.com/) - dialect is not supported yet */
public class MySqlDialect extends SqlDialect {

    @Override
    public String getJdbcUrl() {
        return "jdbc:mysql://127.0.0.1:3306/db1";
    }

    @Override
    public String getJdbcDriver() {
        return "com.mysql.jdbc.Driver";
    }

}
