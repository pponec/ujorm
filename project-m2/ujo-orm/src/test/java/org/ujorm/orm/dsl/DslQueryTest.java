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

/** DslQuery test class with shared connection */
class DslQueryTest extends AbstractDatabaseTest {

    private final EntityContext ctx = EntityContext.ofDefault();
    private final EntityManager<Employee, Long> entityManager = ctx.entityManager(Employee.class);
    private final ResultSetMapper<Employee> employeeMapper = ResultSetMapper.of(Employee.class);

    @Test
    void testSelectEmployeeWithCity() {
        try (var query = new DslQuery<>(connection(), entityManager)) {
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
            var employees = query.streamMap(employeeMapper.mapper()).toList();
            assertEquals(0,employees.size());
        }
    }

    @Test
    void testSelectCount() {
        try (var query = new DslQuery<>(connection(), entityManager)) {
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
            var count = query.streamMap(rs -> rs.getInt(1)).findFirst().orElseThrow();
            assertEquals(0, count.intValue());
        }
    }


    @Test
    void testUpdateCount() {
        try (var query = new DslQuery<>(connection(), entityManager)) {
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
            var count = query.streamMap(rs -> rs.getInt(1)).findFirst().orElseThrow();
            assertEquals(0, count.intValue());
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