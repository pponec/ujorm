package org.ujorm.orm.dsl;

import org.junit.jupiter.api.Assertions;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.model.QuotePair;
import org.ujorm.orm.tutorial.domains.Employee;
import org.ujorm.orm.tutorial.domains.MetaCity;
import org.ujorm.orm.dsl.meta.MetaEmployee;
import org.ujorm.orm.utils.EntityContext;

import java.sql.Connection;

class DslQueryDemoTest {


    private EntityContext ctx = EntityContext.ofDefault();
    private EntityManager<Employee, Long> employeeEm = ctx.entityManager(Employee.class);
    private Employee emp = new Employee();
    private MetaEmployee emps = new MetaEmployee();
    private QuotePair quotePair = QuotePair.ofMsSqlServer();

    /** @Test  : Only demo */
    void demo() {

        var select = new DslQuery<>(connection(), employeeEm);
        select.column(MetaEmployee.id)
                .column(MetaEmployee.name)
                .column(MetaEmployee.city, MetaCity.name)
                .column(MetaEmployee.boss, MetaEmployee.name)
                .where(MetaEmployee.id.whereGt(1L))
                .tail("ORDER BY", MetaEmployee.id, "DESC" );

        var bossAlias = MetaEmployee.as("b");
        var emplId2 = bossAlias.key(MetaEmployee.id);
        var tableAlias = emplId2.tableAlias();

        Assertions.assertNotNull(tableAlias);

        //---

        var bossNameKey = MetaEmployee.as("b", MetaEmployee.name);

        var crn1 = MetaEmployee.name.whereEq("Joe");
        var crn2 = MetaCity.name.whereEq("Prague");
        var crn3 = bossNameKey.whereEq("Joe");
        var crnAll = crn1.and(crn2).and(crn3);

        select.column(MetaEmployee.id)
                .column(MetaEmployee.name)
                .column(MetaEmployee.city, MetaCity.name)
                .column(MetaEmployee.boss, MetaEmployee.name)
                .where(crnAll);
    }

    /** @Test  : Only demo */
    void count() {

        var select = new DslQuery<Employee>(connection(), employeeEm);
        select.sql("SELECT COUNT(*)")
                .where(MetaEmployee.id.whereGt(1L))
                .tail("ORDER BY", MetaEmployee.id, "DESC" );

        var bossAlias = MetaEmployee.as("b");
        var emplId2 = bossAlias.key(MetaEmployee.id);
        var tableAlias = emplId2.tableAlias();

        Assertions.assertNotNull(tableAlias);
    }

    private Connection connection() {
        return null;
    }
}