package org.ujorm.mapper.tutorial;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractDemo {

    private Connection dbConnection;

    /** Database connection */
    protected Connection connection() {
        return dbConnection;
    }

    /** Set up the database connection before the first test. */
    @BeforeAll
    void setUp() throws SQLException {
        this.dbConnection = getDbConnection();
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
        var jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
        var databaseUser = "sa";
        var databasePassword = "";
        var result = DriverManager.getConnection(jdbcUrl, databaseUser, databasePassword);
        result.setAutoCommit(false);
        return result;
    }
}