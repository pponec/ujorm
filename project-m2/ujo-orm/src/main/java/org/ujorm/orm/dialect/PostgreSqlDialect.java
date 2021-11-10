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
import org.jetbrains.annotations.NotNull;
import org.ujorm.criterion.ValueCriterion;
import org.ujorm.orm.CriterionDecoder;
import org.ujorm.orm.Query;
import org.ujorm.orm.SqlDialect;
import org.ujorm.orm.TableWrapper;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaIndex;

/** PostgreSQL (http://www.postgresql.org/) */
public class PostgreSqlDialect extends SqlDialect {

    @Override
    public String getJdbcUrl() {
        return "";
    }

    @Override
    public String getJdbcDriver() {
        return "org.postgresql.Driver";
    }

    /** Print SQL 'CREATE SCHEMA' */
    @Override
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        out.append("CREATE SCHEMA ");
        out.append(schema);
        return out;
    }

    /** PostgreSql dialect uses a database type OID (instead of the BLBO). */
    @Override
    protected String getColumnType(final MetaColumn column) {
        switch (MetaColumn.DB_TYPE.of(column)) {
            case CLOB:
                if (column.isTypeOf(String.class)) {
                    return "TEXT"; // variable type with unlimited length
                }
            case BLOB:
            case BINARY:
                return "BYTEA"; // The 'bytea' data type allows storage of binary strings.
            default:
                return super.getColumnType(column);
        }
    }

    /**
     * Print an INDEX for the parameter column.
     * @return More statements separated by the ';' charactes are enabled
     */
    @Override
    public Appendable printIndex(final MetaIndex index, final Appendable out) throws IOException {
        super.printIndex(index,out);
        if (MetaIndex.UNIQUE.of(index)) {
            printIndexCondition(index, out);
        }
        return out;
    }

    /**
     * Create an PARTIAL INDEX for exclude NULL values.
     * The behaviour is similar like the PostgreSQL or ORACLE databases.
     * <br>NOTE: you can disable the feature by the overwriting current method by an empty body.
     */
    public Appendable printIndexCondition(final MetaIndex index, final Appendable out) throws IOException {
        String prefix = " WHERE ";
        for (MetaColumn column : MetaIndex.COLUMNS.getList(index)) {
            if (!column.hasDefaultValue()) {
                out.append(prefix);
                printColumnName(column, out);
                out.append(" IS NOT NULL");
                prefix = " AND ";
            }
        }
        return out;
    }

    /** Print an SQL DELETE statement. */
    @Override
    public Appendable printDelete
        ( CriterionDecoder decoder
        , Appendable out
        ) throws IOException
    {
        out.append("DELETE FROM ");
        TableWrapper[] tables = decoder.getTablesSorted();

        for (int i=0; i<tables.length; ++i) {
            if (i>0) {
                out.append(i==1 ? " USING " : ", ");
            }
            printTableAliasDefinition(tables[i], out);
        }

        final String where = decoder.getWhere();
        if (where.length()>0) {
            out.append(" WHERE ");
            out.append(where);
        }

        return out;
    }

    /** The implementation suppoorts {@code ILIKE} operator
     * @return Template with arguments type of {@code {1}={2}}
     */
    @NotNull @Override
    public String getCriterionTemplate(@NotNull final ValueCriterion crit) {

        switch (crit.getOperator()) {
            case STARTS_CASE_INSENSITIVE:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS_CASE_INSENSITIVE:
                return "{0} ILIKE {1}";
            default:
                return super.getCriterionTemplate(crit);
        }
    }

}
