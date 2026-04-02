package org.ujorm.orm.dsl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.orm.SqlQueryDsl;
import org.ujorm.orm.tutorial.domains.Employee;
import org.ujorm.orm.tutorial.domains.MetaCity;
import org.ujorm.orm.dsl.meta.MetaEmployee;

import java.sql.Connection;

class SqlQueryDslTest {



    private Employee emp = new Employee();
    private MetaEmployee emps = new MetaEmployee();



    @Test
    void demo() {


        var query = new SqlQueryDsl(connection());
        query.select( MetaEmployee.id
                , MetaEmployee.name
                , MetaEmployee.city.join(MetaCity.name)
                , MetaEmployee.boss.join(MetaEmployee.name)
                ).where(MetaCity.name, "= :id");



         var query2 = new SqlQueryDsl(connection());

        var bossAlias = MetaEmployee.as("b");
        var emplId2 = bossAlias.key(MetaEmployee.id);
        var tableAlias = emplId2.tableAlias();

        Assertions.assertNotNull(tableAlias);

//        var crn1 = MetaEmployee.name.whereEq("Joe");
//        var crn2 = MetaCity.name.whereEq("Prague");
//        var crn3 = crn1.and(crn2);

//        query.select( MetaEmployee.id
//                , MetaEmployee.name
//                , MetaEmployee.city.join(MetaCity.name)
//                , MetaEmployee.boss.join(MetaEmployee.name)
//        ).where(crn3);


    }

    private Connection connection() {
        return null;
    }
}