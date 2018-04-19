/*
 *  Copyright 2013-2014 Effectiva Solutions company
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
package org.ujorm.orm;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.ao.QuoteEnum;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaIndex;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.tools.Check;

/**
 * Extended SQL dialect class.
 * @author Effectiva Solutions company
 */
@Deprecated
@SuppressWarnings("unchecked")
public class SqlDialectEx {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(SqlDialectEx.class);

    /** The main dialect */
    protected final SqlDialect dialect;

    /** Constructor */
    public SqlDialectEx(SqlDialect dialect) {
        this.dialect = dialect;
    }

    /** Print an DROP INDEX for the parameter column.
     * @return More statements separated by the ';' charactes are enabled
     */
    public Appendable printDropIndex(final MetaIndex index, final Appendable out) throws IOException {
        out.append("DROP INDEX ");
        out.append(MetaIndex.NAME.of(index));
        out.append(" ON ");
        dialect.printFullTableName(MetaIndex.TABLE.of(index), out);
        return out;
    }


    public Appendable printPrimaryKey(MetaColumn column, StringBuilder sql) throws Exception {
        sql.append("ALTER TABLE ");
        dialect.printFullTableName(column.getTable(), sql);
        sql.append(" ADD ");
        printPrimaryKeyConstraint(column.getTable(), Arrays.asList(column), sql);
        return sql;
    }

    /** SQL Name Provider */
    public String buildConstraintName(final MetaColumn column, final MetaTable table) {
        final String cn = column.getConstraintName();
        if (Check.hasLength(cn)) {
            return cn;
        } else {
            return getNameProvider().buildDefaultConstraintName(table, column);
        }
    }

    public String buildPrimaryKeyOverColumn(MetaTable table, List<MetaColumn> columns) throws IOException {
        String overColumn = "";
        String separator = "";
        for (MetaColumn col : columns) {
            String name = col.getName();
            overColumn += separator;
            overColumn += name;
            separator = ",";
        }
        return overColumn;
    }

    /** Print Unique Constraint */
    public Appendable printUniqueConstraint(List<MetaColumn> columns, StringBuilder out) throws IOException {
        return printUniqueConstraint(out, columns.toArray(new MetaColumn[] {} ));
    }

    /** printUniqueConstraint */
    public Appendable printUniqueConstraint(StringBuilder out, MetaColumn... columns) throws IOException {
        assert columns.length > 0;
        MetaTable table = columns[0].getTable();
        out.append("ALTER TABLE ");
        dialect.printFullTableName(table, out);
        out.append(" ADD CONSTRAINT ");

        out.append(getNameProvider().getUniqueConstraintName(columns));
        out.append(" UNIQUE (");
        String separator = "";
        for (MetaColumn column : columns) {
            out.append(separator);
            dialect.printColumnName(column, out);
            separator = ",";
        }
        out.append(")");
        return out;
    }

    /** Prints primary key constraint */
    protected void printPrimaryKeyConstraint(MetaTable table, List<MetaColumn> columns, Appendable out) throws IOException {
        out.append(" CONSTRAINT ");
        String pkName = getNameProvider().buildPrimaryKeyName(table, columns);
        out.append(pkName);
        out.append(" PRIMARY KEY ");
        String pkOverColumn = buildPrimaryKeyOverColumn(table, columns);
        out.append("(");
        dialect.printQuotedName(pkOverColumn, QuoteEnum.BY_CONFIG, out);
        out.append(")");
    }

    /** Print the next Sequence value */
    public Appendable printSequenceNextValueWithValues(final UjoSequencer sequence, long seq, final Appendable out) throws IOException {
        out.append("UPDATE ");
        dialect.printSequenceTableName(sequence, out);
        out.append(" SET ");
        dialect.printQuotedNameAlways(dialect.getSeqTableModel().getSequence(), out);
        out.append("=" + seq);
        out.append(" WHERE ");
        dialect.printQuotedNameAlways(dialect.getSeqTableModel().getId(), out);
        out.append("=?");
        return out;
    }

    /** Print SQL LIST ALL SEQUENCE IDs. */
    public Appendable printSequenceListAllId(final UjoSequencer sequence, final Appendable out) throws IOException {
        final SeqTableModel tm = dialect.getSeqTableModel();

        out.append("SELECT ");
        dialect.printQuotedNameAlways(tm.getId(), out);
        out.append(" FROM ");
        dialect.printSequenceTableName(sequence, out);

        return out;
    }

    /**
     * Returns a name provider
     * @return Current SQL name provider
     * @throws IllegalStateException A problem during creating an instance.
     */
    protected final SqlNameProvider getNameProvider() throws IllegalStateException {
        return dialect.getNameProvider();
    }

}
