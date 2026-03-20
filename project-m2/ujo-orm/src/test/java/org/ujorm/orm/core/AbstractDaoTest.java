package org.ujorm.orm.core;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.ujorm.orm.SqlQuery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

abstract public class AbstractDaoTest {

    protected Connection dbConnection;

    /** Set up the database connection and initialize tables before each test. */
    @BeforeEach
    void setUp() throws SQLException {
        this.dbConnection = getDbConnection();
        this.initTables();
    }

    /** Rollback the transaction and close the connection after each test.
     * The try-finally block ensures the connection is closed even if rollback fails. */
    @AfterEach
    void tearDown() throws SQLException {
        if (dbConnection != null) {
            try {
                dbConnection.rollback();
            } finally {
                dbConnection.close();
            }
        }
    }

    /** Create DB tables */
    protected void initTables() {
        new CommonDao(dbConnection).initTables();
    }

    /** Create demo data */
    protected void initDemoData() throws SQLException {
        throw new IllegalStateException("TODO");
    }

    protected SqlQuery sqlQuery() {
        return new SqlQuery(dbConnection);
    }

    /**
     * Establish a connection to the H2 in-memory database and create tables.
     * Auto-commit is disabled to allow for rolling back test data.
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