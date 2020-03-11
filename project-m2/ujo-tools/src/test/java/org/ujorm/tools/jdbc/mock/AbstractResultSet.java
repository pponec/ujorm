/*
 *  Copyright 2018-2020 Pavel Ponec
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
package org.ujorm.tools.jdbc.mock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Abstrat ResultSet to create a mock.
 * @author Pavel Ponec
 */
public abstract class AbstractResultSet implements ResultSet {

    private List<Map<String,Object>> rows;
    private int pointer;
    private boolean closed = false;

    /** An initialization */
    protected void init() {
        rows = new ArrayList<>();
        pointer = -1;
        closed = false;
    }

    /** Create a one row
     * @param values Object array is supported
     */
    public void addRow(@Nonnull Object... values) {
        if (values.length == 1 && values[0] instanceof Object[]) {
            values = (Object[]) values[0];
        }
        Map<String,Object> row = new HashMap<>(values.length);
        for (int i = 0; i < values.length; i++) {
            row.put(String.valueOf(i + 1), values[i]);
        }
        addRow(row);
    }

    /** Create a one row */
    public void addRow(Map<String,Object> dbRow) {
        rows.add(dbRow);
    }

    // --- Overrided method ---

    @Override
    public boolean next() throws SQLException {
        ++pointer;
        return pointer < rows.size();
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return getObject(String.valueOf(columnIndex), type);
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        final T result = (T) rows.get(pointer).get(String.valueOf(columnLabel));
        if (result == null || type.isInstance(result)) {
            return result;
        }
        throw new ClassCastException(type.getName());
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        Integer result = getObject(columnIndex, Integer.class);
        return result != null ? result : 0;
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return getObject(columnIndex, String.class);
    }

    @Override
    public void close() throws SQLException {
        closed = true;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    // --- Factory method ---

    /** Create new instance */
    public static AbstractResultSet of() {
        try {
            final AbstractResultSet result = Mockito.mock(AbstractResultSet.class);

            Mockito.doCallRealMethod().when(result).init();
            Mockito.doCallRealMethod().when(result).addRow(Matchers.anyMap());
            Mockito.doCallRealMethod().when(result).addRow(Matchers.<Object>anyVararg());
            Mockito.doCallRealMethod().when(result).next();
            Mockito.doCallRealMethod().when(result).getObject(Matchers.anyInt(), Matchers.any(Class.class));
            Mockito.doCallRealMethod().when(result).getObject(Matchers.any(String.class), Matchers.any(Class.class));
            Mockito.doCallRealMethod().when(result).getInt(Matchers.anyInt());
            Mockito.doCallRealMethod().when(result).getString(Matchers.anyInt());
            Mockito.doCallRealMethod().when(result).close();
            Mockito.doCallRealMethod().when(result).isClosed();

            result.init();
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Instance of the class failed", e);
        }
    }
}
