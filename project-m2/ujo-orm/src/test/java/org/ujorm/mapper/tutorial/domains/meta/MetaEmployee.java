package org.ujorm.mapper.tutorial.domains.meta;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;
import org.ujorm.mapper.tutorial.domains.City;
import org.ujorm.mapper.tutorial.domains.Employee;

/** Metamodel of the Employee entity */
public class MetaEmployee {

    static final DomainHandler<Employee> meta = DomainHandlerProvider.getHandler(Employee.class);
    public static final Key<Employee, Long> id = meta.getKey("id", Long.class);
    public static final Key<Employee, String> name = meta.getKey("name", String.class);
    public static final Key<Employee, Employee> boss = meta.getKey("boss", Employee.class);
    public static final Key<Employee, City> city = meta.getKey("city", City.class);

}
