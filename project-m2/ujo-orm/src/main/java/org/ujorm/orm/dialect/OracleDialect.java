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
import java.util.List;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaIndex;
import org.ujorm.orm.metaModel.MetaTable;
import static org.ujorm.core.UjoTools.SPACE;

/** Oracle (www.oracle.com/) release 9.0 */
public class OracleDialect extends PostgreSqlDialect {


	/* Returns a default JDBC URL
	 * @see org.ujorm.orm.SqlDialect#getJdbcUrl()
	 */
    @Override
    public String getJdbcUrl() {
        return "jdbc:oracle:thin:@myhost:1521:orcl";
    }

	/* Returns a JDBC Driver
	 * @see org.ujorm.orm.SqlDialect#getJdbcDriver()
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

    /** Print SQL database SELECT
     * @param query The UJO query
     * @param count only count of items is required;
     */
    @Override
    protected Appendable printSelectTable(Query query, boolean count, Appendable out) throws IOException {
        if (!count && (query.isOffset() || query.isLimit())) {
            out.append("SELECT * FROM (SELECT ujorm__.*, ROWNUM AS ujorm_rownum FROM (\n");
            super.printSelectTable(query, count, out);
            out.append("\n) ujorm__) WHERE ujorm_rownum > " + query.getOffset());
            if (query.isLimit()) {
                final long to = query.getOffset() + query.getLimit();
                out.append(" AND ujorm_rownum <= " + to);
            }
        } else {
            super.printSelectTable(query, count, out);
        }
        return out;
    }

    @Override
    public void printOffset(Query query, Appendable out) throws IOException {
        // ORACLE has a special implementation of the LIMIT & OFFSET.
    }

    /** PostgreSql dialect uses a database type OID (instead of the BLBO). */
    @Override
    protected String getColumnType(final MetaColumn column) {

        switch (MetaColumn.DB_TYPE.of(column)) {
            case BIGINT:
                return "NUMBER";
            default:
                // Don't call the super.getColumnType(..)
                return MetaColumn.DB_TYPE.of(column).name();
        }
    }

    /** Print a SQL sript to add a new column to the table
     * <br>Sample: ALTER TABLE sa_myphone.ord_order ADD (NEW_COLUMN INT DEFAULT 777 NOT NULL);
     */
    @Override
    public Appendable printAlterTableAddColumn(MetaColumn column, Appendable out) throws IOException {
        out.append("ALTER TABLE ");
        printFullTableName(column.getTable(), out);
        out.append(" ADD (");

        if (column.isForeignKey()) {
            printFKColumnsDeclaration(column, out);
        } else {
            printColumnDeclaration_2(column, null, out);
        }
        out.append(" )");

        return out;
    }

    /**
     *  Print a SQL to create column
     * @param column Database Column
     * @param aName The name parameter is not mandatory, the not null value means a foreign key.
     * @throws java.io.IOException
     */
    public Appendable printColumnDeclaration_2(MetaColumn column, String aName, Appendable out) throws IOException {

        String name = aName!=null ? aName : column.getName();
        printQuotedName(name, out);
        out.append(SPACE);
        out.append(getColumnType(column));

        if (!MetaColumn.MAX_LENGTH.isDefault(column)) {
            out.append("(" + MetaColumn.MAX_LENGTH.of(column));
            if (!MetaColumn.PRECISION.isDefault(column)) {
                out.append("," + MetaColumn.PRECISION.of(column));
            }
            out.append(")");
        }
        if (column.hasDefaultValue()) {
            printDefaultValue(column, out);
        }
        if (MetaColumn.MANDATORY.of(column) && aName == null) {
            out.append(" NOT NULL");
        }
        if (MetaColumn.PRIMARY_KEY.of(column) && aName == null) {
            out.append(" PRIMARY KEY");
        }
        return out;
    }


    /**
     * No PARTIAL INDEX is supported.
     */
    @Override
    public Appendable printIndexCondition(final MetaIndex index, final Appendable out) throws IOException {
        return out;
    }

    @Override
    public Appendable printInsert(List<? extends OrmUjo> bo, int idxFrom, int idxTo, Appendable out) throws IOException {
        return printInsertBySelect(bo, idxFrom, idxTo, "FROM DUAL", out);
    }

    /** Create a SQL script for the NEXT SEQUENCE from a native database sequencer */
    @Override
    public Appendable printNextSequence(String sequenceName, MetaTable table, Appendable out) throws IOException {
        out.append("SELECT ");
        out.append(sequenceName);
        out.append(".NEXTVAL FROM DUAL");
        return out;
    }

}
