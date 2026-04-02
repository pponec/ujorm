package org.ujorm.orm;

import org.junit.jupiter.api.Test;
import org.ujorm.orm.tutorial.domains.*;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

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
                );


    }

    private Connection connection() {
        return null;
    }
}