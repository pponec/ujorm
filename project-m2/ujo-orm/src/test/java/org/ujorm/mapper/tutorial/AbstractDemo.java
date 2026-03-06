package org.ujorm.mapper.tutorial;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.ujorm.tools.jdbc.SQLExceptionBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractDemo {

    private Connection dbConnection;
    private boolean checkTables = false;

    /** Database connection */
    protected Connection connection() {
        return dbConnection;
    }

    /** Commit connection, set table check flag and call init method. */
    protected void superInit() {
        if (dbConnection == null) {
            throw new IllegalStateException("No connection available.");
        }
        try {
            dbConnection.commit();
        } catch (SQLException ex) {
            throw SQLExceptionBuilder.build(ex);
        }
        this.checkTables = true;
        init();
    }

    void init() {
    }

    /** Set up the database connection before the first test. */
    @BeforeAll
    void setUp() throws SQLException {
        this.dbConnection = getDbConnection();
    }

    /** Verify that at least two tables exist if the check is enabled. */
    @BeforeEach
    void checkTablesExist() {
        if (checkTables) {
            var tableCount = getAllTables().count();
            assertTrue(tableCount >= 2, "Expected at least 2 tables, but found: " + tableCount);
        }
    }

    /** Commit the transaction after each test method and enable table checks. */
    @AfterEach
    void commitTransaction() throws SQLException {
        if (dbConnection != null && !dbConnection.getAutoCommit()) {
            dbConnection.commit();
        }
        this.checkTables = true;
    }

    /** Close the database connection after all tests have finished. */
    @AfterAll
    void tearDown() throws SQLException {
        if (dbConnection != null) {
            dbConnection.close();
            dbConnection = null;
        }
    }

    /**
     * Establish a connection to the H2 in-memory database.
     * Auto-commit is disabled to allow manual transaction management.
     *
     * @return The configured database connection
     * @throws SQLException If a database error occurs
     */
    private Connection getDbConnection() throws SQLException {
        var jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
        var databaseUser = "sa";
        var databasePassword = "";
        var result = DriverManager.getConnection(jdbcUrl, databaseUser, databasePassword);
        result.setAutoCommit(false);
        return result;
    }

    /** Prints and returns a stream of all database tables. */
    protected Stream<String> getAllTables() {
        var result = new ArrayList<String>();
        var tableTypes = new String[]{"TABLE"};
        try (var rs = dbConnection.getMetaData().getTables(null, null, "%", tableTypes)) {
            while (rs.next()) {
                result.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException ex) {
            throw SQLExceptionBuilder.build(ex);
        }
        return result.stream();
    }
}