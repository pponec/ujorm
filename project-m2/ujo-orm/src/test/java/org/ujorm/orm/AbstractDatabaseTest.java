package org.ujorm.orm;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.ujorm.tools.Lines;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.TestInstance.Lifecycle;

/** Abstract parent for DSL tests managing H2 lifecycle */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class AbstractDatabaseTest {

    /** Datatabase configuration */
    final DatabaseParam db = new DatabaseParam(
            "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
            "sa",
            "");

    private Connection dbConnection;

    /** Set up a logically empty database with schema before each test method. */
    @BeforeEach
    void setUp() {
        try {
            if (dbConnection == null) {
                var result = DriverManager.getConnection(db.url(), db.user(), db.password());
                result.setAutoCommit(false);
                dbConnection = result;
            }
            try (var stmt = dbConnection.createStatement()) {
                stmt.execute("DROP ALL OBJECTS");
            }
            initSchema(dbConnection);
            dbConnection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to reset database state", e);
        }
    }

    /** Clean up and close the database connection after all tests in the class */
    @AfterAll
    void tearDownAll() {
        if (dbConnection != null) {
            try (var conn = dbConnection;
                 var stmt = conn.createStatement()) {
                stmt.execute("SHUTDOWN");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /** Returns the active database connection */
    public Connection connection() {
        return dbConnection;
    }

    /**
     * Method to be implemented by child classes to define the DB structure.
     * @param connection Database connection to use for schema creation.
     */
    protected abstract void initSchema(Connection connection);

    /** Splits text into lines and replaces double quotes with single quotes. */
    protected Lines toQuotedLines(String sql) {
        return Lines.ofQuoted(sql);
    }

    /**
     * Database parameters
     */
    record DatabaseParam(
            /** URL of the database */
            String url,
            /** Database user */
            String user,
            /** Database password */
            String password
    ) {
    }
}