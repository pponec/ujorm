package org.ujorm.orm.dsl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.orm.tutorial.domains.Employee;
import org.ujorm.orm.tutorial.domains.MetaCity;
import org.ujorm.orm.dsl.meta.MetaEmployee;

import java.sql.Connection;

class DslQueryTest {



    private Employee emp = new Employee();
    private MetaEmployee emps = new MetaEmployee();

    @Test
    void demo() {

        var select = new DslQuery<Employee>(connection());
        select.column(MetaEmployee.id)
                .column(MetaEmployee.name)
                .column(MetaEmployee.city, MetaCity.name)
                .column(MetaEmployee.boss, MetaEmployee.name)
                .where(MetaEmployee.id.whereGt(1L))
                .append("ORDER BY", MetaEmployee.id, "DESC" );


         var query2 = new DslQuery(connection());

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

    private Connection connection() {
        return null;
    }
}