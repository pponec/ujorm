/*
 *  Copyright 2009-2014 Pavel Ponec
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

package org.ujorm.orm.dialect;

import java.io.IOException;
import org.ujorm.orm.Query;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaTable;

/**
 * Hsqldb dialect (http://hsqldb.org)
 * @author Pavel Ponec
 */
public class HsqldbDialect extends H2Dialect {
    
    /** Returns a default JDBC URL.
     * <br>For a PostgreSQL simulation use: 
     * {@code jdbc:hsqldb:mem:db1;sql.syntax_pgs=true}
     */    
    public String getJdbcUrl() {
        return "jdbc:hsqldb:mem:db1";
    }

    @Override
    public String getJdbcDriver() {
        return "org.hsqldb.jdbcDriver";
    }

    /** Print SQL 'CREATE SCHEMA' */
    @Override
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        out.append("CREATE SCHEMA ");
        printQuotedName(schema, out);
        out.append(" AUTHORIZATION DBA");
        return out;
    }

    /** COMMENT is not supported in HSQLDB database */
    @Override
    public Appendable printComment(MetaTable table, Appendable out) throws IOException {
        return out;
    }

    /** COMMENT is not supported in HSQLDB database */
    @Override
    public Appendable printComment(MetaColumn column, Appendable out) throws IOException {
        return out;
    }

    /** Database HSQLDB does not supports the MultiRow INSERT. */
    @Override
    public boolean isMultiRowInsertSupported() {
        return false;
    }

    /** Including 1.7.2, HSQLDB does not support table locking via SELECT FOR UPDATE.<br>
     * For more information see <a href="http://hsqldb.org/doc/src/org/hsqldb/jdbc/jdbcStatement.html">http://hsqldb.org</a>.
     */
    @Override
    protected Appendable printLockForSelect(Query query, Appendable out) throws IOException, UnsupportedOperationException {
        return out;
    }

    /** Create a SQL script for the NEXT SEQUENCE from a native database sequencer */
    @Override
    public Appendable printNextSequence(String sequenceName, MetaTable table, Appendable out) throws IOException {
        out.append("CALL NEXT VALUE FOR ");
        out.append(sequenceName);
        return out;
    }
}
