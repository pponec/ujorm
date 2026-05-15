package org.ujorm.orm.dsl;

import org.junit.jupiter.api.Test;
import org.ujorm.orm.AbstractDatabaseTest;
import org.ujorm.orm.SqlQuery;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.dsl.meta.*;
import org.ujorm.orm.jdbc.ResultSetMapper;
import org.ujorm.orm.utils.EntityContext;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** DslQuery test class with shared connection */
class SelectQueryTest extends AbstractDatabaseTest {

    /** The class {@link SelectQuery} enables only SQL SELECT statements. */
    static final boolean DSL_SELECT_ONLY = true;

    private final EntityContext ctx = EntityContext.ofDefault();
    private final EntityManager<Employee, Long> entityManager = ctx.entityManager(Employee.class);
    private final EntityManager<City, Long> cityManager = ctx.entityManager(City.class);
    private final ResultSetMapper<Employee> employeeMapper = ResultSetMapper.of(Employee.class);

    @Test
    void testSelectEmployeeWithCity() {
        try (var query = new SelectQuery<>(connection(), entityManager)) {
            query.sql("SELECT")
                    .column(QEmployee.id)
                    .column(QEmployee.name)
                    .column(QEmployee.city, QCity.name)
                    .column(QEmployee.boss, QEmployee.name)
                    .where(QEmployee.id.whereGt(1L))
                    .tail("ORDER BY", QEmployee.id);

            var sqlValues = query.toString(); // Build the query.
            var sqlTempl = query.sqlTemplate();
            var sql = toQuotedLines(sqlTempl);
            System.out.println("<<SQL>>\n" + sql);

            assertEquals(9, sql.size());
            assertEquals("SELECT e.'ID' AS 'id'", sql.next());
            assertEquals(", e.'NAME' AS 'name'", sql.next());
            assertEquals(", c.'NAME' AS 'city.name'", sql.next());
            assertEquals(", b.'NAME' AS 'boss.name'", sql.next());
            assertEquals("FROM 'EMPLOYEE' e", sql.next());
            assertEquals("JOIN 'CITY' c ON c.'ID' = e.'CITY_ID'", sql.next());
            assertEquals("LEFT JOIN 'EMPLOYEE' b ON b.'ID' = e.'BOSS_ID'", sql.next());
            assertEquals("WHERE e.'ID' > :e_id_0", sql.next());
            assertEquals("WHERE e.'ID' > [1]", toQuotedLines(sqlValues).get(-2), "Check value");
            assertEquals("ORDER BY e.'ID'", sql.next());

            // Execute the query:
            var employees = query.toStream(employeeMapper.mapper()).toList();
            assertEquals(0,employees.size());
        }
    }

    @Test
    void testSelectCount() {
        try (var query = new SelectQuery<>(connection(), entityManager)) {
            query.sql("SELECT COUNT(*)").where(QEmployee.id.whereGt(1L));

            var sqlValues = query.toString(); // Build the query.
            assertNotNull(sqlValues);
            var sql = toQuotedLines(query.sqlTemplate());
            System.out.println("<<SQL>>\n" + sql.toString());

            assertEquals(3, sql.size());
            assertEquals("SELECT COUNT(*)", sql.next());
            assertEquals("FROM 'EMPLOYEE' e", sql.next());
            assertEquals("WHERE e.'ID' > :e_id_0", sql.next());

            // Execute the query:
            var count = query.toStream(rs -> rs.getInt(1)).findFirst().orElseThrow();
            assertEquals(0, count.intValue());
        }
    }

    @Test
    void testUpdate() {
        if (DSL_SELECT_ONLY) return;

        var employeeTable = TableAlias.of(Employee.class, "emp");
        try (var query = new SelectQuery<>(connection(), entityManager)) {
            query.sql("UPDATE", employeeTable,
                            "SET", employeeTable.key(QEmployee.name), "= :name")
                    .where(employeeTable.key(QEmployee.id).whereGt(1L))
                    .bind("name", "Joe");

            var sqlParam = query.toString();
            System.out.println("<<SQL>> " + sqlParam);
            var sql = toQuotedLines(sqlParam);

            assertEquals(3, sql.size());
            assertEquals("UPDATE \"EMPLOYEE\" AS emp", sql.next());
            assertEquals("SET \"NAME\" = [Joe]", sql.next());
            assertEquals("WHERE emp.\"ID\" > [1]", sql.next());

            // Execute the query:
            var count = query.execute();
            assertEquals(0, count);
        }
    }

    @Test
    void testEnumWhereEqForBothMappings() {
        var city = cityManager.crud(connection()).insert(new City(null, "Prague", "CZ"));
        var employee = Employee.of("Joe", city, null);
        employee.setStateOrdinal(EmployeeState.INACTIVE);
        employee.setStateString(EmployeeState.INACTIVE);
        entityManager.crud(connection()).insert(employee);

        try (var query = new SelectQuery<>(connection(), entityManager)) {
            var employees = query.sql("SELECT")
                    .column(QEmployee.id)
                    .column(QEmployee.name)
                    .where(QEmployee.stateOrdinal.whereEq(EmployeeState.INACTIVE)
                            .and(QEmployee.stateString.whereEq(EmployeeState.INACTIVE)))
                    .toList();

            var sqlWithValues = query.toString();
            assertEquals(1, employees.size());
            assertTrue(sqlWithValues.contains("[1]"), sqlWithValues);
            assertTrue(sqlWithValues.contains("[INACTIVE]"), sqlWithValues);
        }
    }

    @Test
    void testEnumWhereInForBothMappings() {
        var city = cityManager.crud(connection()).insert(new City(null, "London", "UK"));
        var employee = Employee.of("Bob", city, null);
        employee.setStateOrdinal(EmployeeState.ACTIVE);
        employee.setStateString(EmployeeState.ACTIVE);
        entityManager.crud(connection()).insert(employee);

        try (var query = new SelectQuery<>(connection(), entityManager)) {
            var employees = query.sql("SELECT")
                    .column(QEmployee.id)
                    .column(QEmployee.name)
                    .where(QEmployee.stateOrdinal.whereIn(EmployeeState.ACTIVE, EmployeeState.INACTIVE)
                            .and(QEmployee.stateString.whereIn(EmployeeState.ACTIVE, EmployeeState.INACTIVE)))
                    .toList();

            var sqlWithValues = query.toString();
            assertEquals(1, employees.size());
            assertTrue(sqlWithValues.contains("[0]"), sqlWithValues);
            assertTrue(sqlWithValues.contains("[1]"), sqlWithValues);
            assertTrue(sqlWithValues.contains("[ACTIVE]"), sqlWithValues);
            assertTrue(sqlWithValues.contains("[INACTIVE]"), sqlWithValues);
        }
    }

    /** Test query without logging using FINEST level */
    @Test
    void testLogOffByFinest() {
        var logger = java.util.logging.Logger.getLogger(org.ujorm.tools.jdbc.AbstractSqlQuery.class.getName());
        var logRecords = new java.util.ArrayList<java.util.logging.LogRecord>();
        var handler = new java.util.logging.Handler() {
            @Override
            public void publish(java.util.logging.LogRecord record) {
                logRecords.add(record);
            }
            @Override public void flush() {}
            @Override public void close() throws SecurityException {}
        };

        var originalLevel = logger.getLevel();
        logger.setLevel(java.util.logging.Level.INFO);
        logger.addHandler(handler);

        try (var query = new SelectQuery<>(connection(), entityManager)) {
            query.log(java.util.logging.Level.FINEST, false);
            query.sql("SELECT COUNT(*)").where(QEmployee.id.whereGt(1L));

            // Execute the query:
            var count = query.toStream(rs -> rs.getInt(1)).findFirst().orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals(0, count.intValue());

            var hasLogs = logRecords.stream()
                    .anyMatch(record -> record.getMessage() != null && record.getMessage().contains("SELECT COUNT(*)"));

            org.junit.jupiter.api.Assertions.assertFalse(hasLogs,
                    "SQL should NOT be logged when level is FINEST and logger is set to INFO.");
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(originalLevel);
        }
    }

    // -------------

    /** Initialize database schema */
    protected void initSchema(Connection connection) {
        try (var query = new SqlQuery(connection)) {
            query.sql("""
                    CREATE TABLE city
                    ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                    , name VARCHAR(50) NOT NULL
                    , country_code VARCHAR(2) NOT NULL
                    )
                    """).execute();
            query.sql("""
                    CREATE TABLE employee
                    ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                    , name VARCHAR(50) NOT NULL
                    , boss_id BIGINT NULL
                    , city_id BIGINT NOT NULL
                    , state_ordinal SMALLINT NOT NULL
                    , state_string VARCHAR(16) NOT NULL
                    )
                    """).execute();
            query.sql("""
                    ALTER TABLE employee ADD CONSTRAINT fk_employee_boss_id__id
                    FOREIGN KEY (boss_id)
                    REFERENCES employee(id)
                    ON DELETE RESTRICT ON UPDATE RESTRICT;
                    """).execute();
            query.sql("""
                    ALTER TABLE employee ADD CONSTRAINT fk_employee_city_id__id
                    FOREIGN KEY (city_id)
                    REFERENCES city(id)
                    ON DELETE CASCADE ON UPDATE RESTRICT;
                    """).execute();
        }
    }
}