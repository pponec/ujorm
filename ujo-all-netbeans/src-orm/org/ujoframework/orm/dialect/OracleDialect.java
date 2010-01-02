/*
 *  Copyright 2009-2010 Pavel Ponec
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

import java.io.IOException;
import org.ujoframework.orm.metaModel.MetaColumn;

/** Oracle (www.oracle.com/) */
public class OracleDialect extends PostgreSqlDialect {


	/* Returns a default JDBC URL
	 * @see org.ujoframework.orm.SqlDialect#getJdbcUrl()
	 */
    @Override
    public String getJdbcUrl() {
        return "jdbc:oracle:thin:@myhost:1521:orcl";
    }

	/* Returns a JDBC Driver
	 * @see org.ujoframework.orm.SqlDialect#getJdbcDriver()
	 */
    @Override
    public String getJdbcDriver() {
        return "oracle.jdbc.driver.OracleDriver";
    }

    /** Print no schema */
    @Override
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        return out;
    }

    /** Returns a default primary key database type.
     * The method is called from method 'SqlDialect.printSequenceTable()' and from 'MetaDatabase.changeDbType()'.
     */
    //    @Override
    //    public DbType getPrimaryKeyType() {
    //        return DbType.NUMBER;
    //    }

    /** PostgreSql dialect uses a database type OID (instead of the BLBO). */
    @Override
    protected String getColumnType(final MetaColumn column) {
        switch (MetaColumn.DB_TYPE.of(column)) {
            case BIGINT:
                return "NUMBER";
            default:
                return super.getColumnType(column);
        }
    }



}
