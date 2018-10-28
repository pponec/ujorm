/*
 *  Copyright 2018-2018 Pavel Ponec
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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Abstrat PreparedStatement to create a mock.
 * @author Pavel Ponec
 */
public abstract class AbstractPreparedStatement implements PreparedStatement {

    boolean closed = false;

    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    @Override
    public void close() throws SQLException {
        closed = true;
    }

    /** Creating a mock database connection for many rows in ResultSet */
    public static Connection createConnection( List<Object[]> tableValues) throws SQLException, IOException {
        AbstractResultSet resultSet = AbstractResultSet.of();
        for (Object[] rowValues : tableValues) {
            resultSet.addRow(rowValues);
        }

        PreparedStatement statement = Mockito.mock(AbstractPreparedStatement.class);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.doCallRealMethod().when(statement).close();
        Mockito.doCallRealMethod().when(statement).isClosed();

        Connection result = Mockito.mock(Connection.class);
        Mockito.when(result.prepareStatement(Matchers.<String>any()))
                .thenReturn(statement);

        return result;
    }

    /** Creating a mock database connection for a single value ResultSet */
    public static Connection createSingleValueConnection(Object value) throws IllegalStateException {
        try {
            return createConnection(Arrays.<Object[]>asList(new Object[] {value}));
        } catch (IOException | SQLException e)  {
            throw new IllegalStateException(e);
        }
    }

}
