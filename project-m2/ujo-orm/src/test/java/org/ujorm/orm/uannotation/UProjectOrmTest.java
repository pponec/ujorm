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

/** All attributes of the Ujorm annotations on the JavaBean {@link UProject}. */
class UProjectOrmTest extends AbstractDatabaseTest {

    private final EntityContext ctx = EntityContext.ofDefault();
    private final EntityManager<UDepartment, Long> departmentEm = ctx.entityManager(UDepartment.class);
    private final EntityManager<UWorker, Long> workerEm = ctx.entityManager(UWorker.class);
    private final EntityManager<UProject, Long> projectEm = ctx.entityManager(UProject.class);

    /** The metamodel reflects the annotation attributes. */
    @Test
    void metamodel() {
        var table = projectEm.tableModel(connection()).tableName();
        assertEquals("TESTDB.U_SCHEMA.U_PROJECT", table.getQualifiedName(), "The real names of the H2 metadata");

        assertTrue(MetaUProject.id.info().primaryKey());
        assertTrue(MetaUProject.id.info().required());
        assertColumn(MetaUProject.name, "title", true, false);
        assertColumn(MetaUProject.description, "description", false, false);
        assertColumn(MetaUProject.owner, "owner_id", true, true);
        assertColumn(MetaUProject.department, "dep_id", false, true);
        assertColumn(MetaUProject.parentId, "parent_id", false, true);
        assertColumn(MetaUProject.active, "active", true, false);
        assertEquals(UWorker.YesNoConverter.class, MetaUProject.active.info().converter().getClass());
        assertEquals("YesNoConverter", MetaUProject.archived.info().converter().toString(), "The JPA adapter");
        assertTrue(MetaUProject.levelOrdinal.info().mapEnumByOrdinal());
        assertFalse(MetaUProject.level.info().mapEnumByOrdinal());
        assertFalse(MetaUProject.levelPlain.info().mapEnumByOrdinal());

        var names = MetaUProject.id.domainClass().getDeclaredFields().length;
        assertEquals(names - 2, projectEm.tableModel(connection()).columns().size(), "Two excluded fields");
    }

    @Test
    void crud() {
        var department = departmentEm.crud(connection()).insert(UDepartment.of("Sales"));
        var owner = workerEm.crud(connection()).insert(UWorker.of("Joe", department, true, Level.SENIOR));
        var crud = projectEm.crud(connection());
        var project = UProject.of("Alpha", owner, department, Level.SENIOR);
        project.setParentId(99L);
        var id = crud.insert(project).getId();

        var result = crud.findById(id).orElseThrow();
        assertEquals("Alpha", result.getName());
        assertNull(result.getDescription());
        assertEquals(owner.getId(), result.getOwner().getId());
        assertEquals(department.getId(), result.getDepartment().getId());
        assertEquals(99L, result.getParentId());
        assertEquals(Level.SENIOR, result.getLevelOrdinal());
        assertEquals(Level.SENIOR, result.getLevel());
        assertEquals(Level.SENIOR, result.getLevelPlain());
        assertTrue(result.isActive());
        assertFalse(result.getArchived());
        assertNull(result.getNote());
        assertNull(result.getCache());

        assertEquals("Alpha|" + owner.getId() + '|' + department.getId() + "|99|1|SENIOR|Y|N|SENIOR", rawRow(id));

        result.setDescription("The first one");
        result.setActive(false);
        result.setArchived(true);
        result.setLevel(Level.JUNIOR);
        result.setLevelOrdinal(Level.JUNIOR);
        result.setDepartment(null);
        crud.update(result);
        assertEquals("Alpha|" + owner.getId() + "|0|99|0|JUNIOR|N|Y|SENIOR", rawRow(id));
        assertEquals("The first one", crud.findById(id).orElseThrow().getDescription());

        assertEquals(1, crud.delete(result));
        assertTrue(crud.findById(id).isEmpty());
    }

    /** The required relation makes the INNER JOIN, the optional one the LEFT JOIN. */
    @Test
    void joins() {
        var deps = departmentEm.crud(connection());
        var sales = deps.insert(UDepartment.of("Sales"));
        var owner = workerEm.crud(connection()).insert(UWorker.of("Joe", sales, true, Level.SENIOR));
        var crud = projectEm.crud(connection());
        crud.insert(UProject.of("Alpha", owner, sales, Level.SENIOR));
        crud.insert(UProject.of("Beta", owner, null, Level.JUNIOR));

        var result = SelectQuery.run(connection(), projectEm, query -> query
                .columns(true)
                .column(MetaUProject.owner, MetaUWorker.name)
                .column(MetaUProject.department, MetaUDepartment.name)
                .where(MetaUProject.active.whereEq(true))
                .tail("ORDER BY", MetaUProject.name)
                .toStream()
                .map(p -> p.getName() + ':' + p.getOwner().getName() + ':'
                        + (p.getDepartment() != null ? p.getDepartment().getName() : null))
                .toList());
        assertEquals("[Alpha:Joe:Sales, Beta:Joe:null]", result.toString());

        var names = SelectQuery.run(connection(), projectEm, query -> query
                .columns(false)
                .where(MetaUProject.levelOrdinal.whereEq(Level.JUNIOR))
                .where(MetaUProject.archived.whereEq(false))
                .toStream()
                .map(UProject::getName)
                .toList());
        assertEquals("[Beta]", names.toString());
    }

    /** A foreign key kept as a raw value is a common column: it is selected, compared and written as it is. */
    @Test
    void rawForeignKey() {
        var sales = departmentEm.crud(connection()).insert(UDepartment.of("Sales"));
        var owner = workerEm.crud(connection()).insert(UWorker.of("Joe", sales, true, Level.SENIOR));
        var crud = projectEm.crud(connection());
        var parent = crud.insert(UProject.of("Parent", owner, sales, Level.SENIOR));
        var child = UProject.of("Child", owner, sales, Level.JUNIOR);
        child.setParentId(parent.getId());
        crud.insert(child);

        var result = SelectQuery.run(connection(), projectEm, query -> query
                .columns(false)
                .where(MetaUProject.parentId.whereEq(parent.getId()))
                .toStream()
                .map(p -> p.getName() + ':' + p.getParentId())
                .toList());
        assertEquals("[Child:" + parent.getId() + "]", result.toString());

        var roots = SelectQuery.run(connection(), projectEm, query -> query
                .columns(false)
                .where(MetaUProject.parentId.whereNull())
                .toStream()
                .map(UProject::getName)
                .toList());
        assertEquals("[Parent]", roots.toString());
    }

    private String rawRow(Long id) {
        return SqlQuery.run(connection(), query -> query
                .sql("""
                     SELECT title, owner_id, dep_id, parent_id, level_ordinal, level, active, archived, level_plain
                     FROM U_SCHEMA.u_project WHERE id = :id""")
                .bind("id", id)
                .toStream(rs -> rs.getString(1) + '|' + rs.getLong(2) + '|' + rs.getLong(3) + '|' + rs.getLong(4)
                        + '|' + rs.getInt(5) + '|' + rs.getString(6) + '|' + rs.getString(7) + '|' + rs.getString(8)
                        + '|' + rs.getString(9))
                .findFirst()
                .orElseThrow());
    }

    private static void assertColumn(org.ujorm.core.Key<UProject, ?> key, String column, boolean required, boolean foreignKey) {
        assertEquals(column, key.info().columnLabel(), key.fullName());
        assertEquals(required, key.info().required(), key.fullName());
        assertEquals(foreignKey, key.info().foreignKey(), key.fullName());
    }

    @Override
    protected void initSchema(Connection connection) {
        for (var sql : new String[] {
                "CREATE TABLE u_department (id BIGINT AUTO_INCREMENT PRIMARY KEY, dep_name VARCHAR(50) NOT NULL)",
                """
                CREATE TABLE u_worker
                ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                , name VARCHAR(50) NOT NULL
                , dep_id BIGINT NOT NULL REFERENCES u_department(id)
                , active CHAR(1) NOT NULL
                , level VARCHAR(10) NULL
                , level_ordinal INT NULL
                )""",
                "CREATE SCHEMA U_SCHEMA",
                """
                CREATE TABLE U_SCHEMA.u_project
                ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                , title VARCHAR(50) NOT NULL
                , description VARCHAR(200) NULL
                , owner_id BIGINT NOT NULL REFERENCES PUBLIC.u_worker(id)
                , dep_id BIGINT NULL REFERENCES PUBLIC.u_department(id)
                , parent_id BIGINT NULL
                , level_ordinal INT NULL
                , level VARCHAR(10) NULL
                , active CHAR(1) NOT NULL
                , archived CHAR(1) NULL
                , level_plain VARCHAR(10) NULL
                )"""}) {
            try (var query = new SqlQuery(connection)) {
                query.sql(sql).execute();
            }
        }
    }
}
