/*
 * Copyright 2026-2026 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.orm.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.generator.TableIdentifier;
import org.ujorm.orm.Config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Tests for TableModelBuilder */
@ExtendWith(MockitoExtension.class)
class TableModelBuilderTest {

    @Mock
    private DomainHandler<Object> mockHandler;
    @Mock
    private Connection mockConnection;
    @Mock
    private DatabaseMetaData mockMetaData;
    @Mock
    private ResultSet mockResultSet;

    private TableModelBuilder<Object> builder;
    private Config config;

    @BeforeEach
    void setUp() {
        config = new Config();
        builder = new TableModelBuilder<>(mockHandler, config);
    }

    @Test
    void testGetDbVendorReturnsOracle() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("Oracle Database 19c");

        assertEquals(DatabaseVendor.ORACLE, builder.getDbVendor(mockConnection));
    }

    @Test
    void testGetDbVendorReturnsDefault() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");

        assertEquals(DatabaseVendor.DEFAULT, builder.getDbVendor(mockConnection));
    }

    @Test
    void testGetDbVendorReturnsMsSqlServer() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("Microsoft SQL Server");

        assertEquals(DatabaseVendor.MS_SQL_SERVER, builder.getDbVendor(mockConnection));
    }

    /** Test MySQL database vendor detection */
    @Test
    void testGetDbVendorReturnsMySql() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("MySQL");

        assertEquals(DatabaseVendor.MY_SQL, builder.getDbVendor(mockConnection));
    }

    /** Test MariaDB database vendor detection */
    @Test
    void testGetDbVendorReturnsMariaDb() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("MariaDB");

        assertEquals(DatabaseVendor.MARIA_DB, builder.getDbVendor(mockConnection));
    }

    /** Test fallback to DEFAULT on SQLException */
    @Test
    void testGetDbVendorThrowsException() throws SQLException {
        when(mockConnection.getMetaData()).thenThrow(new SQLException("Mock DB Error"));

        assertEquals(DatabaseVendor.DEFAULT, builder.getDbVendor(mockConnection));
    }

    @Test
    void testGetSqlQuoteMsSqlServer() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("Microsoft SQL Server");

        var result = builder.getSqlQuote(mockConnection, config);
        assertEquals('[', result.open());
        assertEquals(']', result.close());
    }

    @Test
    void testGetSqlQuoteMySql() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("MySQL");

        var result = builder.getSqlQuote(mockConnection, config);
        assertEquals('`', result.open());
        assertEquals('`', result.close());
    }

    @Test
    void testGetSqlQuoteMariaDb() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("MariaDB");

        var result = builder.getSqlQuote(mockConnection, config);
        assertEquals('`', result.open());
        assertEquals('`', result.close());
    }

    @Test
    void testGetSqlQuoteFallbackToJdbcMetaData() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("Some Unknown DB");
        when(mockMetaData.getIdentifierQuoteString()).thenReturn("'");

        var result = builder.getSqlQuote(mockConnection, config);
        assertEquals('\'', result.open());
        assertEquals('\'', result.close());
    }

    @Test
    void testGetSqlQuoteJdbcNull() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(mockMetaData.getIdentifierQuoteString()).thenReturn(null);

        var result = builder.getSqlQuote(mockConnection, config);
        assertEquals('"', result.open());
        assertEquals('"', result.close());
    }

    @Test
    void testGetSqlQuoteJdbcSpace() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("Some DB");
        when(mockMetaData.getIdentifierQuoteString()).thenReturn(" ");

        var result = builder.getSqlQuote(mockConnection, config);
        assertEquals(' ', result.open());
        assertEquals(' ', result.close());
    }

    @Test
    void testGetSqlQuoteDefaultFallback() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(mockMetaData.getIdentifierQuoteString()).thenReturn("");

        var result = builder.getSqlQuote(mockConnection, config);
        assertEquals('"', result.open());
        assertEquals('"', result.close());
    }

    @Test
    void testGetSqlQuoteThrowsSQLException() throws SQLException {
        when(mockConnection.getMetaData()).thenThrow(new SQLException("Mock DB Error"));

        var exception = assertThrows(RuntimeException.class, () -> {
            builder.getSqlQuote(mockConnection, config);
        });

        assertTrue(exception.getMessage().contains("Cannot read DB metadata"));
    }

    @Test
    void testCreateTableIdentifierSuccessIgnoreCase() throws SQLException {
        var softIdentifier = new TableIdentifier("user_account", "public", null);

        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getTables(any(), eq("public"), isNull(), any(String[].class)))
                .thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("TABLE_NAME")).thenReturn("USER_ACCOUNT");
        when(mockResultSet.getString("TABLE_SCHEM")).thenReturn("PUBLIC");
        when(mockResultSet.getString("TABLE_CAT")).thenReturn("DEF_CAT");

        var result = builder.createTableIdentifier(softIdentifier, mockConnection);

        assertEquals("USER_ACCOUNT", result.table());
        assertEquals("PUBLIC", result.schema());
        assertEquals("DEF_CAT", result.catalog());
    }

    @Test
    void testCreateTableIdentifierNotFound() throws SQLException {
        var softIdentifier = new TableIdentifier("missing_table", null, null);

        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getTables(isNull(), isNull(), isNull(), any(String[].class)))
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        var exception = assertThrows(IllegalStateException.class, () -> {
            builder.createTableIdentifier(softIdentifier, mockConnection);
        });

        assertTrue(exception.getMessage().contains("No table was found"));
    }
}