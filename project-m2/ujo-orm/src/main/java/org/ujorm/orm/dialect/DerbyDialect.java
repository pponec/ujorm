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
import org.ujorm.orm.SqlDialect;
import org.ujorm.orm.ao.QuoteEnum;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaTable;

/** Derby (http://db.apache.org/derby/)
 * <br>NOTE: Dialect supports a LIMIT and OFFSET of the Derby release 10.5
 * @see <a href="http://db.apache.org/derby/docs/10.5/ref/">Derby documentation</a>
 */
public class DerbyDialect extends SqlDialect {

    /** Syntax: jdbc:derby:[subsubprotocol:][databaseName][;attribute=value]* */
    @Override
    public String getJdbcUrl() {
        return "jdbc:derby:c:\\temp\\derby-sample;create=true";
    }

    /** Embeded driver is default */
    @Override
    public String getJdbcDriver() {
        return "org.apache.derby.jdbc.EmbeddedDriver";
    }

    /** Print SQL 'CREATE SCHEMA' */
    @Override
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        out.append("CREATE SCHEMA ");
        printQuotedName(schema, QuoteEnum.BY_CONFIG, out);
        return out;
    }

    @Override
    public void printOffset(Query query, Appendable out) throws IOException {
        out.append(" OFFSET " + query.getOffset());
        out.append(" ROWS");
    }

    /** COMMENT is not supported in HSQLDB database */
    @Override
    public Appendable printComment(MetaTable table, Appendable out) throws IOException {
        return out;
    }

    /** COMMENT is not supported in HSQLDB database */
    @Override
    public Appendable printComment(MetaColumn table, Appendable out) throws IOException {
        return out;
    }
    
}
