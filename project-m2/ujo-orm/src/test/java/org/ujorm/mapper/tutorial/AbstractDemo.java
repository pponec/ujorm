package org.ujorm.mapper.tutorial;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.ujorm.tools.jdbc.SQLExceptionBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractDemo {

    private Connection dbConnection;

    /** Database connection */
    protected Connection connection() {
        return dbConnection;
    }

    /** Close connectin and call init method. */
    protected void connectionCommit() {
        if (dbConnection == null) {
            throw new IllegalStateException("No connection available.");
        }
        try {
            dbConnection.commit();
        } catch (SQLException ex) {
            throw SQLExceptionBuilder.build(ex);
        }
        init();
    }

    void init() {
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

    /** Prints and returns a list of all database tables. */
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