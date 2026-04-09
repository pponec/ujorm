package org.ujorm.orm.dsl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ujorm.orm.SqlQuery;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.dsl.meta.*;
import org.ujorm.orm.utils.EntityContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** DslQuery test class with shared connection */
class DslQueryTest extends AbstractDslQueryTest {

    private final EntityContext ctx = EntityContext.ofDefault();
    private final EntityManager<Employee, Long> entityManager = ctx.entityManager(Employee.class);

    @Test
    void testSelectEmployeeWithCity() {
        var query = new DslQuery<>(connection(), entityManager);
        query.sql("SELECT")
                .column(QEmployee.id)
                .column(QEmployee.name)
                .column(QEmployee.city, QCity.name)
                .where(QEmployee.id.whereGt(1L))
                .tail("ORDER BY", QEmployee.id);

        var sql = query.toString();
        assertNotNull(sql);
        System.out.println("<<SQL>>\n" + sql);
    }

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