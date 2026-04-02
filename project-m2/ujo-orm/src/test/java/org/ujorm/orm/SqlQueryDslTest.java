package org.ujorm.orm;

import org.junit.jupiter.api.Test;
import org.ujorm.orm.demo.City;
import org.ujorm.orm.demo.Employee;
import org.ujorm.orm.demo.MetaCity;
import org.ujorm.orm.demo.MetaEmployee;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class SqlQueryDslTest {



    private Employee emp = new Employee();
    private MetaEmployee emps = new MetaEmployee();



    @Test
    void demo() {

        var query = new SqlQueryDsl(connection());

        query.select( MetaEmployee.id
                , MetaEmployee.active
                , MetaEmployee.city.join(MetaCity.name)
                , MetaEmployee.superior.join(MetaEmployee.name) // BUG !
                );


    }

    private Connection connection() {
        return null;
    }
}