package org.ujorm.orm.dsl;

import org.junit.jupiter.api.Assertions;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.model.QuotePair;
import org.ujorm.orm.utils.EntityContext;
import org.ujorm.orm.dsl.meta.*;

import java.sql.Connection;

class SelectQueryDemoTest {


    private final EntityContext ctx = EntityContext.ofDefault();
    private final EntityManager<Employee, Long> employeeEm = ctx.entityManager(Employee.class);
    private final Employee emp = new Employee();
    private final QuotePair quotePair = QuotePair.ofMsSqlServer();

    /** @Test  : Only demo */
    void demo() {

        var select = new SelectQuery<>(connection(), employeeEm);
        select.column(QEmployee.id)
                .column(QEmployee.name)
                .column(QEmployee.city, QCity.name)
                .column(QEmployee.boss, QEmployee.name)
                .where(QEmployee.id.whereGt(1L))
                .tail("ORDER BY", QEmployee.id, "DESC" );

        var bossAlias = TableAlias.of(Employee.class, "b");
        var emplId2 = bossAlias.key(QEmployee.id);
        var tableAlias = emplId2.tableAlias();

        Assertions.assertNotNull(tableAlias);

        //---

        var bossNameKey = TableAlias.aliasedKey("b", QEmployee.name);

        var crn1 = QEmployee.name.whereEq("Joe");
        var crn2 = QCity.name.whereEq("Prague");
        var crn3 = bossNameKey.whereEq("Joe");
        var crnAll = crn1.and(crn2).and(crn3);

        select.column(QEmployee.id)
                .column(QEmployee.name)
                .column(QEmployee.city, QCity.name)
                .column(QEmployee.boss, QEmployee.name)
                .where(crnAll);
    }

    /** @Test  : Only demo */
    void count() {
        var select = new SelectQuery<>(connection(), employeeEm);
        select.sql("SELECT COUNT(*)")
                .where(QEmployee.id.whereGt(1L))
                .tail("ORDER BY", QEmployee.id, "DESC" );

        var bossAlias = TableAlias.of(Employee.class, "b");
        var emplId2 = bossAlias.key(QEmployee.id);
        var tableAlias = emplId2.tableAlias();

        Assertions.assertNotNull(tableAlias);
    }

    private Connection connection() {
        return null;
    }
}