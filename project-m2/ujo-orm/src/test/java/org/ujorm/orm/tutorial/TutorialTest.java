package org.ujorm.orm.tutorial;

import org.junit.jupiter.api.*;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.dsl.SelectQuery;
import org.ujorm.orm.dsl.TableAlias;
import org.ujorm.orm.jdbc.ResultSetMapper;
import org.ujorm.orm.tutorial.domains.*;
import org.ujorm.orm.SqlQuery;
import org.ujorm.orm.utils.EntityContext;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ujorm3: Lightweight, fast, and transparent ORM.
 * Entities are either standard JavaBeans or Records. No magic, pure speed.
 * Note: These tests run sequentially to demonstrate an entity lifecycle.
 *
 * @see QuickStartTutorialTest
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TutorialTest extends AbstractDemo {

    private static final EntityContext CTX = EntityContext.ofSqlInfoWithParams(false);
    private static final EntityManager<Employee, Long> EMPLOYEE_EM = CTX.entityManager(Employee.class);
    private static final EntityManager<City, Long> CITY_EM = CTX.entityManager(City.class);
    private static final ResultSetMapper<Employee> EMPLOYEE_MAPPER = ResultSetMapper.of(Employee.class);

    /** Persists new entities, showcasing handling of both immutable Records and mutable JavaBeans. */
    @Test
    @Order(100)
    void insert() {
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        var cityCrud = CITY_EM.crud(connection());

        // City is an immutable Record, Employee is a mutable JavaBean
        var cityOttawa = cityCrud.insert(new City(null, "Ottawa", "CA"));
        var emplIngrid = Employee.of("Ingrid", cityOttawa, null);
        var emplDave = Employee.of("Dave", cityOttawa, emplIngrid);
        var emplCarol = Employee.of("Carol", cityOttawa, emplIngrid);

        employeeCrud.insert(emplIngrid);
        employeeCrud.insert(emplDave, emplCarol);

        assertNotNull(emplIngrid.getId());
    }

    /** Basic retrieval of an entity from the database using its unique primary key. */
    @Test
    @Order(200)
    void selectEntity_by_id() {
        var crud = CITY_EM.crud(connection());
        var barcelonaId = 1L;
        var barcelona = crud.findById(barcelonaId).orElseThrow();
        Assertions.assertNotNull(barcelona.id());
    }

    /**
     * Executes a complex DSL query with type-safe criteria and joined table columns.
     * See generated SQL statement from the log:
     * <pre>
     *   SELECT e."ID" AS "id"
     *   , e."NAME" AS "name"
     *   , e."CITY_ID" AS "city"
     *   , e."BOSS_ID" AS "boss"
     *   , c."NAME" AS "city.name"
     *   , c."COUNTRY_CODE" AS "city.countryCode"
     *   , b."NAME" AS "boss.name"
     *   FROM "EMPLOYEE" e
     *   JOIN "CITY" c ON c."ID" = e."CITY_ID"
     *   LEFT JOIN "EMPLOYEE" b ON b."ID" = e."BOSS_ID"
     *   WHERE e."ID" &gt;= ? AND c."ID" IN (?,?)
     *   ORDER BY e."ID"
     * </pre>
     */
    @Test
    @Order(210)
    void select_by_criteron() {
        var c1 = MetaEmployee.id.whereGe(1L);
        var c2 = MetaCity.id.whereIn(1L, 2L);
        var criterion = c1.and(c2);

        var employees = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .sql("SELECT")  // Optional call because SELECT is the default
                .columns(true)  // Include all direct foreign keys
                .column(MetaEmployee.city, MetaCity.name)
                .column(MetaEmployee.city, MetaCity.countryCode)
                .column(MetaEmployee.boss, MetaEmployee.name)
                .where(criterion)
                .tail("ORDER BY", MetaEmployee.id)
                .toList()
        );

        assertEquals(3, employees.size());
        assertEquals("Dave", employees.get(1).getName());
        assertEquals("Ingrid", employees.get(1).getBoss().getName());
    }

    /**
     * Executes a complex DSL query with type-safe criteria and joined table columns.
     * See generated SQL statement from the log:
     * <pre>
     * </pre>
     */
    @Test
    @Order(211)
    void select_by_criteron_fk() {
        var ottawa = SelectQuery.run(connection(), CITY_EM, query -> query
                .column(MetaCity.id)
                .where(MetaCity.name.whereEq("Ottawa"))
                .findFirst().orElseThrow());
        var emoloyee = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .where(MetaEmployee.city.whereEq(ottawa))
                .findFirst().orElseThrow());

        assertEquals(ottawa.id(), emoloyee.getCity().id());
    }


    @Test
    @Order(212)
    void select_by_criteron_simplified() {
        var employees = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(false)         // No foreign keys
                .column(MetaEmployee.city, MetaCity.name)
                .column(MetaEmployee.boss, MetaEmployee.name)
                .where( MetaEmployee.id.whereGe(1L))
                .tail("ORDER BY", MetaEmployee.id)
                .toList()
        );

        assertEquals(3, employees.size());
        assertEquals("Dave", employees.get(1).getName());
        assertEquals("Ingrid", employees.get(1).getBoss().getName());
    }

    /** Manages self-referencing relationships by applying an alias in a DSL query. */
    @Test
    @Order(211)
    void select_by_alias() {

        // --- By default alias ---

        var metaBossName =  MetaEmployee.boss.join(MetaEmployee.name);
        var employees = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .column(metaBossName)
                .where(metaBossName.whereEq("Ingrid"))
                .toList()
        );

        assertEquals(2, employees.size());

        // --- By explicit alias: ---

        var aliasedBossName = TableAlias.aliasedKey("boss", MetaEmployee.name);
        employees = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .column(MetaEmployee.boss, aliasedBossName)
                .where(aliasedBossName.whereEq("Ingrid"))
                .toList()
        );

        assertEquals(2, employees.size());

    }

    /** Performs SQL grouping and aggregation, mapping the output to a custom object array. */
    @Test
    @Order(215)
    void select_group_by() {
        var employees = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .sql("SELECT COUNT(*),")
                .column(MetaEmployee.name)
                .where(MetaEmployee.id.whereGe(1L))
                .tail("GROUP BY", MetaEmployee.name)
                .toStream(rs -> new Object[]{
                        rs.getInt(1),
                        rs.getString(2)})
                .toList()
        );

        assertEquals(3, employees.size());
        assertEquals((Object)1, employees.get(0)[0]);
    }

    /** Runs a raw SQL query with dynamic column definition and standard result mapping. */
    @Test
    @Order(220)
    void select_by_columns() {
        var sql = """
                 SELECT ${COLUMNS}
                 FROM employee e
                 JOIN city c ON c.id = e.city_id
                 LEFT JOIN employee b ON b.id = e.boss_id
                 WHERE e.id > :employeeId
                 ORDER BY e.id
                 """;

        var employees = SqlQuery.run(connection(), query -> query
                .sql(sql)
                .column("e.id", MetaEmployee.id)
                .column("e.name", MetaEmployee.name)
                .column("c.name", MetaEmployee.city, MetaCity.name)
                .column("c.country_code", MetaEmployee.city, MetaCity.countryCode)
                .column("b.name", MetaEmployee.boss, MetaEmployee.name)
                .bind("employeeId", 0L)
                .toStream(EMPLOYEE_MAPPER.mapper())
                .toList());

        assertEquals(3, employees.size());
        assertEquals("Dave", employees.get(1).getName());
        assertEquals("Ingrid", employees.get(1).getBoss().getName());
    }

    /** Uses Ujorm labels for consistent naming and mapping in raw SQL queries. */
    @Test
    @Order(230)
    void select_by_labels() {
        var sql = """
                 SELECT e.id      AS ${e.id}
                 , e.name         AS ${e.name}
                 , c.name         AS ${c.name}
                 , c.country_code AS ${c.country_code}
                 , b.name         AS ${b.name}
                 FROM employee e
                 JOIN city c ON c.id = e.city_id
                 LEFT JOIN employee b ON b.id = e.boss_id
                 WHERE e.id > :employeeId
                 ORDER BY e.id
                 """;

        var employees = SqlQuery.run(connection(), query -> query
                .sql(sql)
                .label("e.id", MetaEmployee.id)
                .label("e.name", MetaEmployee.name)
                .label("c.name", MetaEmployee.city, MetaCity.name)
                .label("c.country_code", MetaEmployee.city, MetaCity.countryCode)
                .label("b.name", MetaEmployee.boss, MetaEmployee.name)
                .bind("employeeId", 0L)
                .toStream(EMPLOYEE_MAPPER.mapper())
                .toList());

        assertEquals(3, employees.size());
        assertEquals("Dave", employees.get(1).getName());
        assertEquals("Ingrid", employees.get(1).getBoss().getName());
    }

    /** Updates specific attributes across a collection of entities in a single batch operation. */
    @Test
    @Order(300)
    void update() {
        var employeeCrud = EMPLOYEE_EM.crud(connection());

        var emplIngrid = employeeCrud.findById(1L).orElseThrow();
        var emplDave = employeeCrud.findById(2L).orElseThrow();
        var emplCarol = employeeCrud.findById(3L).orElseThrow();

        emplIngrid.setBoss(emplDave);
        emplDave.setBoss(null);
        emplCarol.setBoss(emplDave);

        employeeCrud.update(Stream.of(emplIngrid, emplDave, emplCarol),
                MetaEmployee.boss);

        assertNull(employeeCrud.findByIdNullable(2L).getBoss());
    }

    /** Demonstrates enum persistence using ORDINAL mapping (ACTIVE=0, INACTIVE=1). */
    @Test
    @Order(310)
    void update_state_enum() {
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        var activeEmployee = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .where(MetaEmployee.state.whereEq(EmployeeState.ACTIVE))
                .tail("ORDER BY", MetaEmployee.id)
                .findFirst()
                .orElseThrow());

        activeEmployee.setState(EmployeeState.INACTIVE);
        employeeCrud.update(activeEmployee, MetaEmployee.state);

        var reloaded = employeeCrud.findById(activeEmployee.getId()).orElseThrow();
        assertEquals(EmployeeState.INACTIVE, reloaded.getState());

        var inactiveCount = SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT COUNT(*)
                        FROM employee
                        WHERE state = :state
                        """)
                .bind("state", MetaEmployee.state, EmployeeState.INACTIVE)
                .toStream(rs -> rs.getInt(1))
                .findFirst()
                .orElseThrow());
        assertEquals(1, inactiveCount);
    }

    /**
     * Deletes rows directly via SQL DELETE with a type-safe Criterion as the WHERE filter.
     * The {@code sql("DELETE")} call sets the SQL head; columns are left empty;
     * {@link SelectQuery#where} translates the Criterion into a parameterised WHERE clause.
     * See generated SQL statements from the log:
     * <pre>
     *   DELETE
     *   FROM "EMPLOYEE" e
     *   WHERE e."STATE" = ?
     * </pre>
     * <pre>
     *   SELECT COUNT(*)
     *   FROM "EMPLOYEE" e
     *   WHERE e."STATE" = ?
     * </pre>
     */
    @Test
    @Order(390)
    void delete_by_criterion() {
        var inactiveCriterion = MetaEmployee.state.whereEq(EmployeeState.INACTIVE);

        try (var query = new SelectQuery<>(connection(), EMPLOYEE_EM)) {
            var deletedCount = query
                    .sql("DELETE")
                    .where(inactiveCriterion)
                    .execute();
            assertEquals(1, deletedCount);

            var remainingCount = query.sql("SELECT COUNT(*)")
                    .where(inactiveCriterion)
                    .toStream(rs -> rs.getInt(1))
                    .findFirst().orElseThrow();
            assertEquals(0, remainingCount);
        }
    }

    /** Handles batch deletion with specialized filtering and ordering for related entities. */
    @Test
    @Order(400)
    void delete() {
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        var qBossId = TableAlias.aliasedKey("b", MetaEmployee.id);
        var criterion = MetaEmployee.id.whereGe(1L);

        try (var query = new SelectQuery<>(connection(), EMPLOYEE_EM)) {
            var employees = query
                    .column(MetaEmployee.id)
                    .column(MetaEmployee.boss, qBossId) // Build the relation
                    .where(criterion)
                    .tail("ORDER BY", qBossId, "DESC NULLS LAST") // Bosses last
                    .toList();

            employeeCrud.delete(employees.stream());

            var count = query.sql("SELECT COUNT(*)")
                    .where(criterion)
                    .toStream(rs -> rs.getInt(1))
                    .findFirst().orElseThrow();

            assertEquals(0, count);
        }
    }

    /** Configures the initial database schema using raw DDL statements. */
    @Override
    void init() {
        try (var query = new SqlQuery(connection())) {
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
                    , state SMALLINT NOT NULL DEFAULT 0
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