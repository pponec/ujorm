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
import org.ujorm.DomainHandler;
import org.ujorm.core.generator.TableIdentifier;
import org.ujorm.orm.Config;
import org.ujorm.orm.impl.Context;

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
    private Context mockContext;
    @Mock
    private Config mockConfig;
    @Mock
    private Connection mockConnection;
    @Mock
    private DatabaseMetaData mockMetaData;
    @Mock
    private ResultSet mockResultSet;

    private TableModelBuilder<Object> builder;

    @BeforeEach
    void setUp() {
        builder = new TableModelBuilder<>(mockHandler, mockContext);
    }

    @Test
    void testIsOracleReturnsTrue() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("Oracle Database 19c");

        var isOracle = builder.isOracle(mockConnection);
        assertTrue(isOracle);
    }

    @Test
    void testIsOracleReturnsFalse() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");

        var isOracle = builder.isOracle(mockConnection);
        assertFalse(isOracle);
    }

    @Test
    void testGetSqlQuoteMsSqlServer() throws SQLException {
        when(mockConfig.isEnableSqlQuoting()).thenReturn(true);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("Microsoft SQL Server");

        var quote = builder.getSqlQuote(mockConnection, mockConfig);
        assertEquals('[', quote.open());
        assertEquals(']', quote.close());
    }

    @Test
    void testGetSqlQuoteMySql() throws SQLException {
        when(mockConfig.isEnableSqlQuoting()).thenReturn(true);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("MySQL");

        var quote = builder.getSqlQuote(mockConnection, mockConfig);
        assertEquals('`', quote.open());
        assertEquals('`', quote.close());
    }

    @Test
    void testGetSqlQuoteFallbackToJdbcMetaData() throws SQLException {
        when(mockConfig.isEnableSqlQuoting()).thenReturn(true);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("Some Unknown DB");
        when(mockMetaData.getIdentifierQuoteString()).thenReturn("'");

        var quote = builder.getSqlQuote(mockConnection, mockConfig);
        assertEquals('\'', quote.open());
        assertEquals('\'', quote.close());
    }

    @Test
    void testGetSqlQuoteDisabled() {
        when(mockConfig.isEnableSqlQuoting()).thenReturn(false);

        var quote = builder.getSqlQuote(mockConnection, mockConfig);
        assertEquals(' ', quote.open());
        assertEquals(' ', quote.close());
    }

    @Test
    void testGetSqlQuoteDefaultFallback() throws SQLException {
        when(mockConfig.isEnableSqlQuoting()).thenReturn(true);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(mockMetaData.getIdentifierQuoteString()).thenReturn(""); // Blank string v JDBC

        var quote = builder.getSqlQuote(mockConnection, mockConfig);
        assertEquals('"', quote.open());
        assertEquals('"', quote.close());
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

        var realIdentifier = builder.createTableIdentifier(softIdentifier, mockConnection);

        assertEquals("USER_ACCOUNT", realIdentifier.table());
        assertEquals("PUBLIC", realIdentifier.schema());
        assertEquals("DEF_CAT", realIdentifier.catalog());
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