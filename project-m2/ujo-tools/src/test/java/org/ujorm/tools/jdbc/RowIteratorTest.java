/*
 * Copyright 2019-2022 Pavel Ponec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Pavel Ponec
 */
public class RowIteratorTest extends AbstractJdbcConnector {

    /**
     * Test of iterator method, of class RowIterator.
     */
    @Test
    public void testShowUsage() throws ClassNotFoundException, SQLException {
        final int[] counter = {0};
        try (Connection dbConnection = createTestConnection()) {
            try (PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM employee")) {
                new RowIterator(ps).toStream().forEach((SqlConsumer)(resultSet) -> {
                    int value = resultSet.getInt(1);
                    System.out.println(" value: " + value);
                    counter[0]++;
                });
            }
        }
        assertEquals(1, counter[0]);
    }

    /**
     * Test of iterator method, of class RowIterator.
     */
    @Test
    public void testNoStreamIteration() throws ClassNotFoundException, SQLException {
        final int[] counter = {0};
        try (Connection dbConnection = createTestConnection()) {
            try (PreparedStatement ps = dbConnection.prepareStatement("SELECT * FROM employee")) {
                new RowIterator(ps).forEach((SqlConsumer)(resultSet) -> {
                    int value = resultSet.getInt(1);
                    System.out.println(" value: " + value);
                    counter[0]++;
                });
            }
        }
        assertEquals(1, counter[0]);
    }

    /** Create new DB connection */
    private Connection createTestConnection() throws ClassNotFoundException, SQLException {
        Connection result = super.createDbConnection();
        JdbcBuilderTest builder = new JdbcBuilderTest();
        builder.createTable(result);
        builder.showInsert(result);
        return result;
    }


}
