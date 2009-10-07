/*
 *  Copyright (c) 2009 Pavel Slovacek
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
import org.ujoframework.orm.Query;

public class FirebirdDialect extends org.ujoframework.orm.SqlDialect {

	/* Returns a default JDBC Driver
	 * @see org.ujoframework.orm.SqlDialect#getJdbcDriver()
	 */
	@Override
	public String getJdbcDriver() {
        return "org.firebirdsql.jdbc.FBDriver";
	}

	/* Returns a default JDBC URL
	 * @see org.ujoframework.orm.SqlDialect#getJdbcUrl()
	 */
	@Override
	public String getJdbcUrl() {
        return "jdbc:firebirdsql:localhost/3050:DbFile?lc_ctype=UTF8";
	}

    /** NO SCHEMA */
    @Override
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        return out;
    }

    /** Print a 'lock clausule' to the end of SQL SELECT statement to use a pessimistic lock.
     * The current database does not support the feature, throw an exception UnsupportedOperationException.
     * <br>The method prints a text "FOR UPDATE WITH LOCK".
     * @param query The UJO query
     */
    @Override
    protected Appendable printLockForSelect(final Query query, final Appendable out) throws IOException, UnsupportedOperationException {
        out.append("FOR UPDATE WITH LOCK");
        return out;
    }
	
}
