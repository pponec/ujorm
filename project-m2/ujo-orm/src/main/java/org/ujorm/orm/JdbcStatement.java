/*
 *  Copyright 2020-2022 Pavel Ponec
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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.core.UjoManager;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.extensions.Property;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaProcedure;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.tools.msg.MsgFormatter;

/**
 * JdbcStatement
 * @author Pavel Ponec
 */
public class JdbcStatement /*implements Closeable*/ {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(JdbcStatement.class);

    /** Prepared Statement */
    @NotNull
    private final PreparedStatement ps;
    @NotNull
    private final ITypeService typeService;
    /** Log limit */
    private final int logValueLengthLimit;
    /** Is Loggable level */
    final private boolean logValues;

    /** Parameter pointer */
    private int parameterPointer = 0;

    @Nullable
    private StringBuilder values;

    /** Constructor for a SQL statement */
    public JdbcStatement(@NotNull final Connection conn, @NotNull final CharSequence sql, @NotNull final OrmHandler handler) throws SQLException {
        this(conn.prepareStatement(sql.toString()), handler);
    }

    /** Constructor for a PreparedStatement */
    public JdbcStatement(@NotNull final PreparedStatement ps, @NotNull final OrmHandler handler) {
        this.ps = ps;
        this.typeService = handler.getParameters().getConverter(null);
        logValues = LOGGER.isLoggable(UjoLogger.INFO);
        logValueLengthLimit = Math.max(10, MetaParams.LOG_VALUE_LENGTH_LIMIT.of(handler.getParameters()));
        if (logValues) {
            values = new StringBuilder();
        }
    }

    /** Return values in format: [1, "ABC", 2.55] */
    @NotNull
    public String getAssignedValues() {
        if (values!=null
        &&  values.length()>0) {
            return values.toString() + "]";
        } else {
            return "NONE";
        }
    }

    /** Close the session */
    public void close() throws SQLException {
        ps.close();
    }

    /** Call the procedure. */
    public void execute() throws SQLException {
        ps.execute();
    }

    /** Run INSERT, UPDATE or DELETE.
     * @return The row count for SQL Data Manipulation Language (DML) statements
     */
    public int executeUpdate() throws SQLException {
        return ps.executeUpdate();
    }

    public ResultSet executeQuery() throws SQLException {
        return ps.executeQuery();
    }

    /** Assign values into the prepared statement */
    @SuppressWarnings("unchecked")
    public void assignValues(@NotNull OrmUjo bo) throws SQLException {
        final MetaTable dbTable = bo.readSession().getHandler().findTableModel((Class) bo.getClass());
        final List<MetaColumn> columns = MetaTable.COLUMNS.getList(dbTable);
        assignValues(bo, columns);
    }

    /** Assign values into the prepared statement */
    @SuppressWarnings("unchecked")
    public void assignValues(@NotNull List<? extends OrmUjo> bos, int idxFrom, int idxTo) throws SQLException {
        final OrmUjo bo = bos.get(idxFrom);
        final MetaTable dbTable = bo.readSession().getHandler().findTableModel((Class) bo.getClass());
        final List<MetaColumn> columns = MetaTable.COLUMNS.getList(dbTable);

        for (int i = idxFrom; i < idxTo; i++) {
            assignValues(bos.get(i), columns);
        }
    }

    /** Assign values into the prepared statement */
    @SuppressWarnings("unchecked")
    public void assignValues(@Nullable OrmUjo table, @NotNull List<MetaColumn> columns) throws SQLException {
        for (MetaColumn column : columns) {
            if (column.isForeignKey()) {
                Key key = column.getKey();
                Object value = table!=null ? key.of(table) : null ;
                assignValues((OrmUjo) value, column.getForeignColumns());
            } else if (column.isColumn()) {
                assignValue(table, column);
            }
        }
    }

    /** Assign values into the prepared statement */
    public final void assignValues(Query query) throws SQLException {
        if (query.getSqlParameters()!=null) {
            assignExtendedValues(query);
        }
        assignValues(query.getDecoder());
    }

    /** Assign extended values into the prepared statement
     * @param query
     * @throws java.sql.SQLException */
    public void assignExtendedValues(Query query) throws SQLException {
        SqlParameters params = query.getSqlParameters();
        if (params==null) {
            return;
        }

        for (int i=0, max=params.getCount(); i<max; i++) {
            final Object value = params.getParameter(i);
            final Class type = value!=null ? value.getClass() : Void.class;
            final Property key = Property.of("[sqlParameter]", type);
            final MetaColumn column = new MetaColumn(typeService);

            MetaColumn.TABLE.setValue(column, query.getTableModel());
            MetaColumn.TABLE_KEY.setValue(column, key);
            query.getTableModel().getDatabase().changeDbType(column);
            query.getTableModel().getDatabase().changeDbLength(column);
            column.initTypeCode();

            if (logValues) {
                String textValue = UjoManager.getInstance().encodeValue(value, false);
                logValue(textValue, key);
            }

            try {
                ++parameterPointer;
                column.getConverter().setValue(column, ps, value, parameterPointer);
            } catch (RuntimeException | OutOfMemoryError e) {
                String textValue = UjoManager.getInstance().encodeValue(value, false);
                String msg = MsgFormatter.format("table: {}, column {}, columnOffset: {}, value: {}", key.getType().getSimpleName(), column, parameterPointer, textValue);
                throw new IllegalUjormException(msg, e);
            }
        }
    }

    /** Assign values into the prepared statement */
    public void assignValues(CriterionDecoder decoder) throws SQLException {
        int columnCount = decoder.getColumnCount();
        for (int i=0; i<columnCount; ++i) {
            final MetaColumn column = decoder.getColumn(i);
            final Object value = decoder.getValueExtended(i);

            if (column.isForeignKey()) {
                List<MetaColumn> fc = column.getForeignColumns();

                if (value instanceof Object[]) {
                    final Object[] ujoValues = (Object[]) value;
                    final Object[] rValues = new Object[ujoValues.length];
                    final MetaColumn rColumn = fc.get(0); // only one PK is supported
                    final boolean isUjo = ujoValues.length > 0
                                       && ujoValues[0] instanceof OrmUjo;

                    for (int j=0; j<ujoValues.length; j++) {
                        final Object ujoValue = ujoValues[j];
                        if (isUjo) {
                            // if instance is OrmUjo, then assign value of key
                            final OrmUjo bo = (OrmUjo) ujoValue;
                            final Object rValue = rColumn.getValue(bo);
                            rValues[j] = rValue;
                        } else {
                            // if instance is not OrmUjo, then assign directly value (it's key)
                            rValues[j] = ujoValue;
                        }
                    }
                    assignValue(rColumn, rValues, null);

                } else if (value instanceof OrmUjo) {
                    final OrmUjo bo = (OrmUjo) value;
                    for (MetaColumn rColumn : fc) {
                        Object rValue = rColumn.getValue(bo);
                        assignValue(rColumn, rValue, bo);
                    }
                } else {
                    assert column.getKey().getType().isInstance(value);
                    assignValue(column, value, null);
                }
            } else {
                assignValue(column, value, null);
            }
        }
    }


    /** Add a next value to a SQL prepared statement. */
    @SuppressWarnings("unchecked")
    public void assignValue(final OrmUjo table, final MetaColumn column) throws SQLException {

        final Key key = column.getKey();
        final Object value = table!=null ? key.of(table) : null ;

        assignValue(column, value, table);
    }


    /** Add a next value to a SQL prepared statement. */
    @SuppressWarnings("unchecked")
    public void assignValue
        ( final MetaColumn column
        , final Object value
        , final OrmUjo bo
        ) throws SQLException {

        final Key key = column.getKey();

        if (logValues) {
            if (bo != null) {
                logValue(bo, key);
            } else {
                String textValue = value instanceof Object[]
                        ? arrayToString( (Object[]) value)
                        : UjoManager.getInstance().encodeValue(value, false) ;
                logValue(textValue, key);
            }
        }

        try {
            if (value instanceof Object[]) for (Object v : (Object[]) value) {
                ++parameterPointer;
                column.getConverter().setValue(column, ps, v, parameterPointer);
            } else {
                ++parameterPointer;
                column.getConverter().setValue(column, ps, value, parameterPointer);
            }
        } catch (RuntimeException | OutOfMemoryError e) {
            String textValue = bo!=null
                ? UjoManager.getInstance().getText(bo, key, UjoAction.DUMMY)
                : UjoManager.getInstance().encodeValue(value, false)
                ;
            String msg = MsgFormatter.format
                ( "table: {}, column {}, columnOffset: {}, value: {}"
                , bo!=null ? bo.getClass().getSimpleName() : "null"
                , column
                , parameterPointer
                , textValue
                );
            throw new IllegalUjormException(msg, e);
        }
    }

    /** Assign procedure parameters */
    @SuppressWarnings("unchecked")
    public void assignValues(DbProcedure bo) {

        CallableStatement ps = (CallableStatement) this.ps;
        MetaProcedure procedure = bo.metaProcedure();
        Object value = null;

        for (MetaColumn metaParam : MetaProcedure.PARAMETERS.getList(procedure)) {
            final Key key = metaParam.getKey();

            if (!key.isTypeOf(Void.class)) try {

                ++parameterPointer;
                int sqlType = MetaColumn.DB_TYPE.of(metaParam).getSqlType();

                if (procedure.isInput(metaParam)) {
                    value = key.of(bo);
                    metaParam.getConverter().setValue(metaParam, ps, value, parameterPointer);

                    if (logValues) {
                        String textValue = UjoManager.getInstance().encodeValue(value, false);
                        logValue(textValue, key);
                    }

                }
                if (procedure.isOutput(metaParam)) {
                    ps.registerOutParameter(parameterPointer, sqlType);
                }

            } catch (RuntimeException | SQLException | OutOfMemoryError e) {
                String textValue = bo != null
                    ? UjoManager.getInstance().getText(bo, key, UjoAction.DUMMY)
                    : UjoManager.getInstance().encodeValue(value, false);
                String msg = MsgFormatter.format("table: {}, column {}, columnOffset: {}, value: {}"
                        , bo != null ? bo.getClass().getSimpleName() : "null"
                        , key
                        , parameterPointer
                        , textValue);
                throw new IllegalUjormException(msg, e);
            }
        }
    }

    /** Assign procedure parameters */
    @SuppressWarnings("unchecked")
    public void loadValues(DbProcedure bo) {

        CallableStatement ps = (CallableStatement) this.ps;
        MetaProcedure procedure = bo.metaProcedure();
        int i = 0;

        // Load data from CallableStatement:
        try {
            for (MetaColumn c : MetaProcedure.PARAMETERS.getList(procedure)) {
                if (procedure.isOutput(c)) {
                    final Object value = c.getConverter().getValue(c, ps, ++i);
                    c.setValueRaw(bo, value);
                }
                else if (procedure.isInput(c)) {
                    ++i;
                }
            }
        } catch (Exception e) {
            throw new IllegalUjormException("Procedure: " + bo, e);
        }
    }

    /** Log a value value into a text format. */
    protected void logValue(final Ujo bo, final Key key) {
        String textValue = UjoManager.getInstance().getText(bo, key, UjoAction.DUMMY);
        logValue(textValue, key);
    }

    /** Log a value value into a text format. */
    protected void logValue(final String textValue, final Key key) {
        final boolean quotaType = key.isTypeOf(CharSequence.class)
                               || key.isTypeOf(java.util.Date.class)
                                ;
        final String textSeparator = quotaType ? "\'" : "";

        values.append(parameterPointer == 0 ? "[" : ", ");
        values.append(textSeparator);
        if (textValue!=null && textValue.length() > logValueLengthLimit) {
          values.append(textValue.subSequence(0, logValueLengthLimit));
          values.append("...[");
          values.append(textValue.length());
          values.append(logValueLengthLimit < 20 ? "]" : " total characters]");
        } else {
          values.append(textValue);
        }
        values.append(textSeparator);
    }

    /** Returns prepared statement - for internal use only */
    @NotNull
    @PackagePrivate PreparedStatement getPreparedStatement() {
        return ps;
    }

    /** Vizualizuje první tři znaky pole */
    private String arrayToString(final Object[] vals) {
        final StringBuilder sb = new StringBuilder(128);
        final int max = Math.min(3, vals.length);
        for (int i = 0; i < max; i++) {
            sb.append(sb.length()==0 ? '[' : ',');
            sb.append(vals[i]);
        }
        if (max < vals.length) {
            sb.append(", ...");
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public String toString() {
        if (ps!=null) {
            return ps.toString();
        } else {
            return super.toString();
        }
    }

}
