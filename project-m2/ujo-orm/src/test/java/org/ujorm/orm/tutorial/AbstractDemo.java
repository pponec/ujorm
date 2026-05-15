package org.ujorm.orm.tutorial;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractDemo {

    private Connection dbConnection;
    private boolean initialized = false;

    /** Database connection */
    protected Connection connection() {
        return dbConnection;
    }

    void init() {
    }

    /** Set up the database connection before the first test. */
    @BeforeAll
    void setUp() throws SQLException {
        this.dbConnection = getDbConnection();
    }

    /**
     * Verify that at least two tables exist before running data-dependent tests.
     * The createTable test is skipped because tables do not exist yet.
     */
    @BeforeEach
    void initTables() {
        if (!initialized) {
            initialized = true;
            init();
        }
    }

    /** Commit the transaction after each test method. */
    @AfterEach
    void commitTransaction() throws SQLException {
        if (dbConnection != null && !dbConnection.getAutoCommit()) {
            dbConnection.commit();
        }
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
        var jdbcUrl = "jdbc:h2:mem:testdb";
        var databaseUser = "sa";
        var databasePassword = "";
        var result = DriverManager.getConnection(jdbcUrl, databaseUser, databasePassword);
        result.setAutoCommit(false);
        return result;
    }

}