package org.ujorm.core.criterion.domains;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;

/** Auto-generated metamodel for Employee */
public interface MetaEmployee {

    Key<Employee, Long> id = meta().getKey("id");
    Key<Employee, String> name = meta().getKey("name");
    Key<Employee, City> city = meta().getKey("city");
    Key<Employee, Employee> boss = meta().getKey("boss");

    private static DomainHandler<Employee> meta() {
        return DomainHandlerProvider.getHandler(Employee.class);
    }
}
