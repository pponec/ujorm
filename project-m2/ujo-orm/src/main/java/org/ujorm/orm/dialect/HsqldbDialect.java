/*
 *  Copyright 2009-2013 Pavel Ponec
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

}
