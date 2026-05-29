package org.ujorm.orm.tutorial;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.ujorm.orm.SqlQuery;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.dsl.SelectQuery;
import org.ujorm.orm.dsl.TableAlias;
import org.ujorm.orm.jdbc.ResultSetMapper;
import org.ujorm.orm.tutorial.domains.City;
import org.ujorm.orm.tutorial.domains.Employee;
import org.ujorm.orm.tutorial.domains.EmployeeState;
import org.ujorm.orm.tutorial.domains.MetaCity;
import org.ujorm.orm.tutorial.domains.MetaEmployee;
import org.ujorm.orm.utils.EntityContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Advanced tutorial scenarios for an AI-friendly pattern pack.
 *
 * <p>This suite extends basic tutorial examples with failure handling, transactions,
 * locking patterns, performance-oriented queries, and schema migration scenarios.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdvancedTutorialTest extends AbstractDemo {

    private static final EntityContext CTX = EntityContext.ofSqlInfoWithParams(false);
    private static final EntityManager<Employee, Long> EMPLOYEE_EM = CTX.entityManager(Employee.class);
    private static final EntityManager<City, Long> CITY_EM = CTX.entityManager(City.class);
    private static final ResultSetMapper<Employee> EMPLOYEE_MAPPER = ResultSetMapper.of(Employee.class);

    // -------------------------------------------------------------------------
    // 10-15 micro examples: one focused pattern per test
    // -------------------------------------------------------------------------

    @Test
    @Order(100)
    void micro_insert_with_explicit_default_fields() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Prague-Default", "CZ"));
        var employee = EMPLOYEE_EM.crud(connection()).insert(Employee.of("Alice-Default", city, null));

        assertNotNull(employee.getId());
        assertEquals("Alice-Default", employee.getName());
        assertEquals(city.id(), employee.getCity().id());
        assertEquals(0, employee.getState().ordinal(), "ACTIVE should be default enum value");
    }

    @Test
    @Order(110)
    void micro_find_by_id_not_found_contract() {
        var crud = EMPLOYEE_EM.crud(connection());
        var missingId = Long.MAX_VALUE;

        assertTrue(crud.findById(missingId).isEmpty());
        assertNull(crud.findByIdNullable(missingId));
    }

    @Test
    @Order(115)
    void micro_select_only_required_columns() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Lean-City", "CZ"));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("Lean-A", city, null));

        var compactRows = SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT e.id, e.name
                        FROM employee e
                        WHERE e.name LIKE :name
                        ORDER BY e.id
                        """)
                .bind("name", "Lean-%")
                .toStream(rs -> rs.getLong(1) + ":" + rs.getString(2))
                .toList());

        assertEquals(1, compactRows.size());
        assertTrue(compactRows.get(0).endsWith(":Lean-A"));
    }

    @Test
    @Order(118)
    void micro_self_join_alias_pattern() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Alias-City", "AT"));
        var boss = EMPLOYEE_EM.crud(connection()).insert(Employee.of("Alias-Boss", city, null));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("Alias-Worker", city, boss));

        var bossName = TableAlias.aliasedKey("bossAlias", MetaEmployee.name);
        var workers = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .column(MetaEmployee.boss, bossName)
                .where(MetaEmployee.name.whereEq("Alias-Worker").and(bossName.whereEq("Alias-Boss")))
                .toList());

        assertEquals(1, workers.size());
        assertEquals("Alias-Boss", workers.get(0).getBoss().getName());
    }

    @Test
    @Order(120)
    void micro_sql_label_and_column_mapping_equivalence() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Brno-Label", "CZ"));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("Bob-Label", city, null));

        var withColumn = SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT ${COLUMNS}
                        FROM employee e
                        JOIN city c ON c.id = e.city_id
                        WHERE e.name = :name
                        """)
                .column("e.id", MetaEmployee.id)
                .column("e.name", MetaEmployee.name)
                .column("c.name", MetaEmployee.city, MetaCity.name)
                .column("c.country_code", MetaEmployee.city, MetaCity.countryCode)
                .bind("name", "Bob-Label")
                .toStream(EMPLOYEE_MAPPER.mapper())
                .toList());

        var withLabel = SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT e.id AS ${e.id}
                        , e.name AS ${e.name}
                        , c.name AS ${c.name}
                        , c.country_code AS ${c.country_code}
                        FROM employee e
                        JOIN city c ON c.id = e.city_id
                        WHERE e.name = :name
                        """)
                .label("e.id", MetaEmployee.id)
                .label("e.name", MetaEmployee.name)
                .label("c.name", MetaEmployee.city, MetaCity.name)
                .label("c.country_code", MetaEmployee.city, MetaCity.countryCode)
                .bind("name", "Bob-Label")
                .toStream(EMPLOYEE_MAPPER.mapper())
                .toList());

        assertEquals(1, withColumn.size());
        assertEquals(1, withLabel.size());
        assertEquals(withColumn.get(0).getName(), withLabel.get(0).getName());
        assertEquals(withColumn.get(0).getCity().countryCode(), withLabel.get(0).getCity().countryCode());
    }

    @Test
    @Order(122)
    void micro_reusable_criteria_composition() {
        var cityA = CITY_EM.crud(connection()).insert(new City(null, "Crit-A", "CZ"));
        var cityB = CITY_EM.crud(connection()).insert(new City(null, "Crit-B", "DE"));
        var idA = EMPLOYEE_EM.crud(connection()).insert(Employee.of("Crit-A1", cityA, null)).getId();
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("Crit-B1", cityB, null));

        var byId = MetaEmployee.id.whereGe(idA);
        var byCity = MetaEmployee.city.join(MetaCity.name).whereEq("Crit-A");
        var reusable = byId.and(byCity);

        var filtered = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .where(reusable)
                .toList());

        assertEquals(1, filtered.size());
        assertEquals("Crit-A1", filtered.get(0).getName());
    }

    @Test
    @Order(123)
    void micro_not_criterion_in_select_query() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Not-City", "CZ"));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("Not-A", city, null));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("Not-B", city, null));

        // Simple NOT: exclude "Not-A"
        var notA = MetaEmployee.name.whereEq("Not-A").not();
        var result = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .where(notA.and(MetaEmployee.name.whereIn("Not-A", "Not-B")))
                .toList());

        assertEquals(1, result.size());
        assertEquals("Not-B", result.get(0).getName());

        // NOT combined with OR (negation of a composite criterion)
        var notAorB = MetaEmployee.name.whereEq("Not-A")
                .or(MetaEmployee.name.whereEq("Not-B"))
                .not();
        var empty = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .where(notAorB.and(MetaEmployee.name.whereIn("Not-A", "Not-B")))
                .toList());

        assertEquals(0, empty.size());
    }

    @Test
    @Order(124)
    void micro_enum_state_roundtrip() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Enum-City", "SK"));
        var employee = EMPLOYEE_EM.crud(connection()).insert(Employee.of("Enum-A", city, null));
        employee.setState(EmployeeState.INACTIVE);
        EMPLOYEE_EM.crud(connection()).update(employee, MetaEmployee.state);

        var inactive = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .where(MetaEmployee.state.whereEq(EmployeeState.INACTIVE).and(MetaEmployee.id.whereEq(employee.getId())))
                .findFirst()
                .orElseThrow());

        assertEquals(EmployeeState.INACTIVE, inactive.getState());
    }

    @Test
    @Order(130)
    void micro_partial_update_single_attribute() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Plzen-Update", "CZ"));
        var employee = EMPLOYEE_EM.crud(connection()).insert(Employee.of("Chris-Before", city, null));

        employee.setName("Chris-After");
        EMPLOYEE_EM.crud(connection()).update(employee, MetaEmployee.name);

        var reloaded = EMPLOYEE_EM.crud(connection()).findById(employee.getId()).orElseThrow();
        assertEquals("Chris-After", reloaded.getName());
        assertEquals(city.id(), reloaded.getCity().id(), "Only name should be modified");
    }

    @Test
    @Order(140)
    void micro_order_by_for_stable_pagination() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Berlin-Page", "DE"));
        var crud = EMPLOYEE_EM.crud(connection());
        var page1 = crud.insert(Employee.of("Page-1", city, null));
        var page2 = crud.insert(Employee.of("Page-2", city, null));
        var page3 = crud.insert(Employee.of("Page-3", city, null));

        var page = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .where(MetaEmployee.id.whereGe(page1.getId()).and(MetaEmployee.id.whereLe(page3.getId())))
                .tail("ORDER BY", MetaEmployee.id)
                .tail("LIMIT 2 OFFSET 1")
                .toList());

        assertEquals(2, page.size());
        assertEquals(page2.getId(), page.get(0).getId());
        assertEquals(page3.getId(), page.get(1).getId());
    }

    @Test
    @Order(150)
    void micro_group_by_with_typed_projection() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Group-City", "PL"));
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        employeeCrud.insert(Employee.of("Group-A", city, null));
        employeeCrud.insert(Employee.of("Group-A", city, null));
        employeeCrud.insert(Employee.of("Group-B", city, null));

        record NameCount(int count, String name) {}
        var grouped = SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT COUNT(*), e.name
                        FROM employee e
                        WHERE e.name IN (:nameA,:nameB)
                        GROUP BY e.name
                        ORDER BY e.name
                        """)
                .bind("nameA", "Group-A")
                .bind("nameB", "Group-B")
                .toStream(rs -> new NameCount(rs.getInt(1), rs.getString(2)))
                .toList());

        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get(0).count());
        assertEquals("Group-A", grouped.get(0).name());
    }

    @Test
    @Order(160)
    void micro_batch_update_with_minimal_projection() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Batch-City", "HU"));
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        employeeCrud.insert(Employee.of("Batch-A", city, null));
        employeeCrud.insert(Employee.of("Batch-B", city, null));

        var employees = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .where(MetaEmployee.name.whereIn("Batch-A", "Batch-B"))
                .toList());
        employees.forEach(e -> e.setState(EmployeeState.INACTIVE));
        employeeCrud.update(employees.stream(), MetaEmployee.state);

        var inactive = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .where(MetaEmployee.state.whereEq(EmployeeState.INACTIVE).and(MetaEmployee.name.whereIn("Batch-A", "Batch-B")))
                .toList());
        assertEquals(2, inactive.size());
    }

    @Test
    @Order(170)
    void micro_batch_delete_fk_safe_order() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Delete-City", "HR"));
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        var boss = employeeCrud.insert(Employee.of("Delete-Boss", city, null));
        employeeCrud.insert(Employee.of("Delete-Worker", city, boss));

        var qBossId = TableAlias.aliasedKey("b", MetaEmployee.id);
        var toDelete = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .column(MetaEmployee.boss, qBossId)
                .where(MetaEmployee.name.whereIn("Delete-Boss", "Delete-Worker"))
                .tail("ORDER BY", qBossId, "DESC NULLS LAST")
                .toList());
        employeeCrud.delete(toDelete.stream());

        var count = SqlQuery.run(connection(), query -> query
                .sql("SELECT COUNT(*) FROM employee WHERE name LIKE :name")
                .bind("name", "Delete-%")
                .toStream(rs -> rs.getInt(1))
                .findFirst()
                .orElseThrow());
        assertEquals(0, count);
    }

    // -------------------------------------------------------------------------
    // 5 failure scenarios
    // -------------------------------------------------------------------------

    @Test
    @Order(200)
    void failure_constraint_violation_is_predictable() {
        var invalidCityRef = new City(999_999L, "Ghost", "ZZ");
        var brokenEmployee = Employee.of("Broken-FK", invalidCityRef, null);

        var ex = assertThrows(RuntimeException.class, () -> EMPLOYEE_EM.crud(connection()).insert(brokenEmployee));
        assertTrue(ex.getMessage() != null && ex.getMessage().toLowerCase().contains("referential"),
                "FK failure should expose referential integrity hint");
    }

    @Test
    @Order(205)
    void failure_null_and_empty_edge_cases() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Edge-City", "CZ"));

        assertThrows(RuntimeException.class, () -> EMPLOYEE_EM.crud(connection())
                .insert(Employee.of(null, city, null)));

        var emptyNameEmployee = EMPLOYEE_EM.crud(connection()).insert(Employee.of("", city, null));
        assertNotNull(emptyNameEmployee.getId(), "Empty string is not blocked by DB NOT NULL constraint");
    }

    @Test
    @Order(210)
    void failure_incorrect_bind_contract() {
        var ex = assertThrows(RuntimeException.class, () -> SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT COUNT(*)
                        FROM employee
                        WHERE id > :fromId
                        """)
                .bind("wrongName", 0L)
                .toStream(rs -> rs.getInt(1))
                .findFirst()
                .orElseThrow()));
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("fromId"),
                "Missing bind placeholder should mention unresolved parameter");
    }

    @Test
    @Order(215)
    void failure_not_found_update_affects_no_rows() {
        var updatedRows = SqlQuery.run(connection(), query -> query
                .sql("""
                        UPDATE employee
                        SET name = :name
                        WHERE id = :id
                        """)
                .bind("name", "No-Op")
                .bind("id", 777_777L)
                .execute());
        assertEquals(0, updatedRows, "Mutation on missing record should affect zero rows");
    }

    @Test
    @Order(220)
    void failure_rollback_on_intermediate_error() throws SQLException {
        var cityCrud = CITY_EM.crud(connection());
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        var startCount = employeeCount();
        var city = cityCrud.insert(new City(null, "Rollback-City", "CZ"));

        try {
            employeeCrud.insert(Employee.of("Rollback-Ok", city, null));
            employeeCrud.insert(Employee.of("Rollback-Fail", new City(123_456L, "Ghost", "XX"), null));
            fail("The second insert should fail due to FK violation");
        } catch (RuntimeException expected) {
            connection().rollback();
        }

        assertEquals(startCount, employeeCount(), "Rollback should revert all inserts in transaction");
    }

    // -------------------------------------------------------------------------
    // 3 transaction scenarios
    // -------------------------------------------------------------------------

    @Test
    @Order(300)
    void transaction_explicit_commit_multi_step() throws SQLException {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Commit-City", "US"));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("Commit-A", city, null));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("Commit-B", city, null));

        connection().commit();

        var committedCount = SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT COUNT(*)
                        FROM employee
                        WHERE name LIKE :name
                        """)
                .bind("name", "Commit-%")
                .toStream(rs -> rs.getInt(1))
                .findFirst()
                .orElseThrow());
        assertEquals(2, committedCount);
    }

    @Test
    @Order(310)
    void transaction_explicit_rollback_multi_step() throws SQLException {
        var baseline = employeeCount();
        var city = CITY_EM.crud(connection()).insert(new City(null, "Tx-Rollback", "SK"));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("Tx-Rollback-A", city, null));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("Tx-Rollback-B", city, null));

        connection().rollback();
        assertEquals(baseline, employeeCount());
    }

    @Test
    @Order(320)
    void transaction_atomic_mixed_operations() throws SQLException {
        var baseline = employeeCount();
        var cityCrud = CITY_EM.crud(connection());
        var employeeCrud = EMPLOYEE_EM.crud(connection());

        var cityA = cityCrud.insert(new City(null, "Tx-Mix-A", "CZ"));
        var cityB = cityCrud.insert(new City(null, "Tx-Mix-B", "SK"));
        var kept = employeeCrud.insert(Employee.of("Tx-Mix-Keep", cityA, null));
        var removed = employeeCrud.insert(Employee.of("Tx-Mix-Remove", cityA, null));

        kept.setCity(cityB);
        employeeCrud.update(kept, MetaEmployee.city);
        employeeCrud.delete(removed);

        connection().rollback();
        assertEquals(baseline, employeeCount(), "Insert/update/delete unit must be atomic on rollback");
    }

    // -------------------------------------------------------------------------
    // 2 concurrency/locking scenarios (requested area)
    // -------------------------------------------------------------------------

    @Test
    @Order(330)
    void locking_optimistic_conflict_pattern() throws SQLException {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Lock-Opt-City", "SI"));
        var employee = EMPLOYEE_EM.crud(connection()).insert(Employee.of("Lock-Opt", city, null));
        connection().commit();

        // Snapshot of the initial state read in transaction A
        var originalState = SqlQuery.run(connection(), query -> query
                .sql("SELECT state FROM employee WHERE id = :id")
                .bind("id", employee.getId())
                .toStream(rs -> rs.getInt(1))
                .findFirst()
                .orElseThrow());

        try (var txB = openSecondaryConnection(); var txBQuery = new SqlQuery(txB)) {
            txBQuery.sql("UPDATE employee SET state = 1 WHERE id = :id").bind("id", employee.getId()).execute();
            txB.commit();
        }

        var updatedRows = SqlQuery.run(connection(), query -> query
                .sql("""
                        UPDATE employee
                        SET state = :newState
                        WHERE id = :id AND state = :expectedState
                        """)
                .bind("newState", 0)
                .bind("id", employee.getId())
                .bind("expectedState", originalState)
                .execute());
        assertEquals(0, updatedRows, "Conditional update detects optimistic write conflict");
    }

    @Test
    @Order(340)
    void locking_pessimistic_contention_pattern() throws SQLException {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Lock-Pes-City", "RO"));
        var employee = EMPLOYEE_EM.crud(connection()).insert(Employee.of("Lock-Pes", city, null));
        connection().commit();

        SqlQuery.run(connection(), query -> query
                .sql("SELECT id FROM employee WHERE id = :id FOR UPDATE")
                .bind("id", employee.getId())
                .toStream(rs -> rs.getLong(1))
                .findFirst()
                .orElseThrow());

        try (var txB = openSecondaryConnection(); var txBQuery = new SqlQuery(txB)) {
            // H2-specific lock timeout syntax. PostgreSQL uses statement_timeout/lock_timeout.
            txBQuery.sql("SET LOCK_TIMEOUT 100").execute();
            assertThrows(RuntimeException.class, () -> txBQuery
                    .sql("UPDATE employee SET name = :name WHERE id = :id")
                    .bind("name", "Lock-Pes-Conflict")
                    .bind("id", employee.getId())
                    .execute());
            txB.rollback();
        } finally {
            connection().rollback();
        }
    }

    @Test
    @Order(350)
    void transaction_isolation_read_committed_allows_non_repeatable_read() throws SQLException {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Iso-RC-City", "CZ"));
        var employee = EMPLOYEE_EM.crud(connection()).insert(Employee.of("Iso-RC-1", city, null));
        connection().commit();
        connection().setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        var firstRead = SqlQuery.run(connection(), query -> query
                .sql("SELECT name FROM employee WHERE id = :id")
                .bind("id", employee.getId())
                .toStream(rs -> rs.getString(1))
                .findFirst()
                .orElseThrow());

        try (var txB = openSecondaryConnection(); var txBQuery = new SqlQuery(txB)) {
            txBQuery.sql("UPDATE employee SET name = :name WHERE id = :id")
                    .bind("name", "Iso-RC-2")
                    .bind("id", employee.getId())
                    .execute();
            txB.commit();
        }

        var secondRead = SqlQuery.run(connection(), query -> query
                .sql("SELECT name FROM employee WHERE id = :id")
                .bind("id", employee.getId())
                .toStream(rs -> rs.getString(1))
                .findFirst()
                .orElseThrow());

        assertEquals("Iso-RC-1", firstRead);
        assertEquals("Iso-RC-2", secondRead);
    }

    @Test
    @Order(360)
    void transaction_isolation_repeatable_read_keeps_snapshot() throws SQLException {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Iso-RR-City", "CZ"));
        var employee = EMPLOYEE_EM.crud(connection()).insert(Employee.of("Iso-RR-1", city, null));
        connection().commit();
        connection().setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

        var firstRead = SqlQuery.run(connection(), query -> query
                .sql("SELECT name FROM employee WHERE id = :id")
                .bind("id", employee.getId())
                .toStream(rs -> rs.getString(1))
                .findFirst()
                .orElseThrow());

        try (var txB = openSecondaryConnection(); var txBQuery = new SqlQuery(txB)) {
            txBQuery.sql("UPDATE employee SET name = :name WHERE id = :id")
                    .bind("name", "Iso-RR-2")
                    .bind("id", employee.getId())
                    .execute();
            txB.commit();
        }

        var secondRead = SqlQuery.run(connection(), query -> query
                .sql("SELECT name FROM employee WHERE id = :id")
                .bind("id", employee.getId())
                .toStream(rs -> rs.getString(1))
                .findFirst()
                .orElseThrow());

        assertEquals("Iso-RR-1", firstRead);
        assertEquals("Iso-RR-1", secondRead);
        connection().rollback();
    }

    // -------------------------------------------------------------------------
    // 2 performance scenarios
    // -------------------------------------------------------------------------

    @Test
    @Order(390)
    void performance_prevent_n_plus_one() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Join-City", "DE"));
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        employeeCrud.insert(Employee.of("Join-A", city, null));
        employeeCrud.insert(Employee.of("Join-B", city, null));
        employeeCrud.insert(Employee.of("Join-C", city, null));

        var employees = SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT ${COLUMNS}
                        FROM employee e
                        JOIN city c ON c.id = e.city_id
                        WHERE e.name LIKE :name
                        ORDER BY e.id
                        """)
                .column("e.id", MetaEmployee.id)
                .column("e.name", MetaEmployee.name)
                .column("e.state", MetaEmployee.state)
                .column("c.id", MetaEmployee.city, MetaCity.id)
                .column("c.name", MetaEmployee.city, MetaCity.name)
                .column("c.country_code", MetaEmployee.city, MetaCity.countryCode)
                .bind("name", "Join-%")
                .toStream(EMPLOYEE_MAPPER.mapper())
                .toList());

        assertEquals(3, employees.size());
        assertTrue(employees.stream().allMatch(e -> e.getCity() != null && "Join-City".equals(e.getCity().name())));
    }

    @Test
    @Order(395)
    void anti_pattern_unordered_pagination_warning() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "AntiPage-City", "DE"));
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        for (int i = 1; i <= 6; i++) {
            employeeCrud.insert(Employee.of("AntiPage-" + i, city, null));
        }

        // Anti-pattern: pagination without ORDER BY is non-deterministic across DB engines.
        // PostgreSQL/MySQL may return different row order between executions/plans.
        var page = SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT id
                        FROM employee
                        WHERE name LIKE :name
                        LIMIT 3 OFFSET 1
                        """)
                .bind("name", "AntiPage-%")
                .toStream(rs -> rs.getLong(1))
                .toList());

        assertEquals(3, page.size());
    }

    @Test
    @Order(400)
    void performance_pagination_with_index_aware_filter() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Perf-City", "DE"));
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        for (int i = 1; i <= 8; i++) {
            employeeCrud.insert(Employee.of("Perf-" + i, city, null));
        }

        var firstPage = pageById(0, 3);
        var secondPage = pageById(3, 3);

        assertEquals(3, firstPage.size());
        assertEquals(3, secondPage.size());
        assertTrue(firstPage.get(firstPage.size() - 1).getId() < secondPage.get(0).getId());
    }

    // -------------------------------------------------------------------------
    // 2 migration/refactoring scenarios
    // -------------------------------------------------------------------------

    @Test
    @Order(490)
    void migration_column_rename_transition() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Mig-Rename-City", "CZ"));
        var employee = EMPLOYEE_EM.crud(connection()).insert(Employee.of("Mig-Rename-A", city, null));

        try (var query = new SqlQuery(connection())) {
            query.sql("ALTER TABLE employee ADD COLUMN full_name VARCHAR(50)").execute();
            query.sql("UPDATE employee SET full_name = name WHERE id = :id").bind("id", employee.getId()).execute();
        }

        var resolvedName = SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT COALESCE(full_name, name)
                        FROM employee
                        WHERE id = :id
                        """)
                .bind("id", employee.getId())
                .toStream(rs -> rs.getString(1))
                .findFirst()
                .orElseThrow());
        assertEquals("Mig-Rename-A", resolvedName);
    }

    @Test
    @Order(500)
    void migration_default_and_backfill_strategy() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Mig-City", "CZ"));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("MigDef-A", city, null));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("MigDef-B", city, null));

        try (var query = new SqlQuery(connection())) {
            query.sql("""
                    ALTER TABLE employee
                    ADD COLUMN external_code VARCHAR(20) DEFAULT 'NEW'
                    """).execute();
            query.sql("""
                    UPDATE employee
                    SET external_code = CONCAT('E-', id)
                    WHERE external_code = 'NEW'
                    """).execute();
        }

        var backfilled = SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT COUNT(*)
                        FROM employee
                        WHERE name LIKE :name
                          AND external_code LIKE :code
                        """)
                .bind("name", "MigDef-%")
                .bind("code", "E-%")
                .toStream(rs -> rs.getInt(1))
                .findFirst()
                .orElseThrow());
        assertEquals(2, backfilled);
    }

    @Test
    @Order(510)
    void boundary_mapper_null_relation_mapping() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Map-Null-City", "CZ"));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("Map-Null-Boss", city, null));

        var mapped = SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT ${COLUMNS}
                        FROM employee e
                        JOIN city c ON c.id = e.city_id
                        LEFT JOIN employee b ON b.id = e.boss_id
                        WHERE e.name = :name
                        """)
                .column("e.id", MetaEmployee.id)
                .column("e.name", MetaEmployee.name)
                .column("e.state", MetaEmployee.state)
                .column("c.id", MetaEmployee.city, MetaCity.id)
                .column("c.name", MetaEmployee.city, MetaCity.name)
                .column("c.country_code", MetaEmployee.city, MetaCity.countryCode)
                .column("b.id", MetaEmployee.boss, MetaEmployee.id)
                .bind("name", "Map-Null-Boss")
                .toStream(EMPLOYEE_MAPPER.mapper())
                .findFirst()
                .orElseThrow());

        assertNull(mapped.getBoss(), "LEFT JOIN null relation should map as null");
    }

    @Test
    @Order(520)
    void boundary_mapper_missing_label_maps_partial_record() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Map-Miss-City", "CZ"));
        EMPLOYEE_EM.crud(connection()).insert(Employee.of("Map-Miss-A", city, null));

        var mapped = SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT e.id AS ${e.id}
                        FROM employee e
                        WHERE e.name = :name
                        """)
                .label("e.id", MetaEmployee.id)
                .bind("name", "Map-Miss-A")
                .toStream(EMPLOYEE_MAPPER.mapper())
                .findFirst()
                .orElseThrow());

        assertNotNull(mapped.getId());
        assertNull(mapped.getName(), "Missing label/column may silently produce partial object mapping");
    }

    @Test
    @Order(530)
    void boundary_enum_unknown_value_fails() {
        var city = CITY_EM.crud(connection()).insert(new City(null, "Map-Enum-City", "CZ"));
        var employee = EMPLOYEE_EM.crud(connection()).insert(Employee.of("Map-Enum-A", city, null));
        SqlQuery.run(connection(), query -> query
                .sql("UPDATE employee SET state = 99 WHERE id = :id")
                .bind("id", employee.getId())
                .execute());

        assertThrows(RuntimeException.class, () -> EMPLOYEE_EM.crud(connection()).findById(employee.getId()).orElseThrow());
    }

    @Test
    @Order(540)
    void boundary_left_join_chain_does_not_drop_bossless_rows() {
        var crud = EMPLOYEE_EM.crud(connection());

        var city = CITY_EM.crud(connection()).insert(new City(null, "City", "CZ"));
        var boss = crud.insert(Employee.of("Boss", city, null));
        var worker = crud.insert(Employee.of("Worker", city, boss));
        var noBoss = crud.insert(Employee.of("NoBoss", city, null));

        // employee → boss (LEFT JOIN) → boss.city (was wrongly INNER JOIN before the fix)
        // Without the fix, NoBoss and Boss were silently dropped from the result.
        var result = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
                .columns(true)
                .column(MetaEmployee.boss, MetaEmployee.city, MetaCity.name)
                .where(MetaEmployee.id.whereIn(boss.getId(), worker.getId(), noBoss.getId()))
                .tail("ORDER BY", MetaEmployee.name)
                .toList());

        assertEquals(3, result.size(), "All employees must appear — bossless rows must not be dropped");
        var noBoss2 = result.stream().filter(e -> noBoss.getName().equals(e.getName())).findFirst().orElseThrow();
        assertNull(noBoss2.getBoss(), "Employee without a boss must map to null");
        var worker2 = result.stream().filter(e -> worker.getName().equals(e.getName())).findFirst().orElseThrow();
        assertNotNull(worker2.getBoss(), "Employee with a boss must have boss populated");
        assertNotNull(worker2.getBoss().getCity(), "Boss city must be populated via multi-level LEFT JOIN chain");
    }

    private int employeeCount() {
        return SqlQuery.run(connection(), query -> query
                .sql("SELECT COUNT(*) FROM employee")
                .toStream(rs -> rs.getInt(1))
                .findFirst()
                .orElseThrow());
    }

    private List<Employee> pageById(int offset, int limit) {
        return SqlQuery.run(connection(), query -> query
                .sql("""
                        SELECT ${COLUMNS}
                        FROM employee e
                        JOIN city c ON c.id = e.city_id
                        WHERE e.name LIKE :name
                        ORDER BY e.id
                        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
                        """)
                .column("e.id", MetaEmployee.id)
                .column("e.name", MetaEmployee.name)
                .column("e.boss_id", MetaEmployee.boss)
                .column("c.id", MetaEmployee.city, MetaCity.id)
                .column("c.name", MetaEmployee.city, MetaCity.name)
                .column("c.country_code", MetaEmployee.city, MetaCity.countryCode)
                .bind("name", "Perf-%")
                .bind("offset", offset)
                .bind("limit", limit)
                .toStream(EMPLOYEE_MAPPER.mapper())
                .toList());
    }

    private Connection openSecondaryConnection() throws SQLException {
        // Dedicated connection is needed for concurrent-lock demos.
        // This helper targets an in-memory H2 tutorial setup; locking semantics are DB-specific.
        var connection = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
        connection.setAutoCommit(false);
        return connection;
    }

    /** Keep a minimal schema so future TODO scenarios can be implemented in this file. */
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
                    ALTER TABLE employee ADD CONSTRAINT fk_employee_city_id__id
                    FOREIGN KEY (city_id)
                    REFERENCES city(id)
                    ON DELETE CASCADE ON UPDATE RESTRICT
                    """).execute();
            query.sql("""
                    ALTER TABLE employee ADD CONSTRAINT fk_employee_boss_id__id
                    FOREIGN KEY (boss_id)
                    REFERENCES employee(id)
                    ON DELETE RESTRICT ON UPDATE RESTRICT
                    """).execute();
            query.sql("""
                    CREATE INDEX idx_employee_city_id ON employee(city_id)
                    """).execute();
        }
    }
}