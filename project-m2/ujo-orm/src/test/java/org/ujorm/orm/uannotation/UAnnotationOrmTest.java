package org.ujorm.orm.uannotation;

import org.junit.jupiter.api.Test;
import org.ujorm.orm.AbstractDatabaseTest;
import org.ujorm.orm.SqlQuery;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.dsl.SelectQuery;
import org.ujorm.orm.uannotation.UWorker.Level;
import org.ujorm.orm.utils.EntityContext;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/** The ORM with entities mapped by the Ujorm annotations ({@code @UTable}, {@code @UColumn}, ...) */
class UAnnotationOrmTest extends AbstractDatabaseTest {

    private final EntityContext ctx = EntityContext.ofDefault();
    private final EntityManager<UDepartment, Long> departmentEm = ctx.entityManager(UDepartment.class);
    private final EntityManager<UWorker, Long> workerEm = ctx.entityManager(UWorker.class);

    @Test
    void crud() {
        var department = departmentEm.crud(connection()).insert(UDepartment.of("Sales"));
        var crud = workerEm.crud(connection());
        var id = crud.insert(UWorker.of("Joe", department, true, Level.SENIOR)).getId();

        var result = crud.findById(id).orElseThrow();
        assertEquals("Joe", result.getName());
        assertEquals(department.getId(), result.getDepartment().getId());
        assertTrue(result.getActive());
        assertEquals(Level.SENIOR, result.getLevel());
        assertEquals(Level.SENIOR, result.getLevelOrdinal());
        assertNull(result.getNote(), "A transient attribute");

        // Raw database values:
        var row = SqlQuery.run(connection(), query -> query
                .sql("SELECT name, dep_id, active, level, level_ordinal FROM u_worker WHERE id = :id")
                .bind("id", id)
                .toStream(rs -> rs.getString(1) + '|' + rs.getLong(2) + '|' + rs.getString(3)
                        + '|' + rs.getString(4) + '|' + rs.getInt(5))
                .findFirst()
                .orElseThrow());
        assertEquals("Joe|" + department.getId() + "|Y|SENIOR|1", row);

        result.setActive(false);
        crud.update(result);
        assertFalse(crud.findById(id).orElseThrow().getActive());
        assertEquals(1, crud.delete(result));
    }

    @Test
    void selectWithRelation() {
        var deps = departmentEm.crud(connection());
        var sales = deps.insert(UDepartment.of("Sales"));
        var it = deps.insert(UDepartment.of("IT"));
        var crud = workerEm.crud(connection());
        crud.insert(UWorker.of("Joe", sales, true, Level.SENIOR));
        crud.insert(UWorker.of("Ann", it, true, Level.JUNIOR));
        crud.insert(UWorker.of("Tom", it, false, Level.SENIOR));

        var result = SelectQuery.run(connection(), workerEm, query -> query
                .columns(true)
                .column(MetaUWorker.department, MetaUDepartment.name)
                .where(MetaUWorker.active.whereEq(true))
                .tail("ORDER BY", MetaUWorker.name)
                .toStream()
                .map(w -> w.getName() + ':' + w.getDepartment().getName() + ':' + w.getLevel())
                .toList());
        assertEquals("[Ann:IT:JUNIOR, Joe:Sales:SENIOR]", result.toString());

        var names = SelectQuery.run(connection(), workerEm, query -> query
                .columns(false)
                .where(MetaUWorker.level.whereEq(Level.SENIOR))
                .where(MetaUWorker.active.whereEq(false))
                .toStream()
                .map(UWorker::getName)
                .toList());
        assertEquals("[Tom]", names.toString());
    }

    @Override
    protected void initSchema(Connection connection) {
        try (var query = new SqlQuery(connection)) {
            query.sql("""
                    CREATE TABLE u_department
                    ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                    , dep_name VARCHAR(50) NOT NULL
                    )
                    """).execute();
        }
        try (var query = new SqlQuery(connection)) {
            query.sql("""
                    CREATE TABLE u_worker
                    ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                    , name VARCHAR(50) NOT NULL
                    , dep_id BIGINT NOT NULL REFERENCES u_department(id)
                    , active CHAR(1) NOT NULL
                    , level VARCHAR(10) NULL
                    , level_ordinal INT NULL
                    )
                    """).execute();
        }
    }
}
