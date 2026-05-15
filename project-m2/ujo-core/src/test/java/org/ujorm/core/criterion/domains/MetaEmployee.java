package org.ujorm.core.criterion.domains;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;

/** Auto-generated metamodel for Employee */
public abstract class MetaEmployee {

    private static final DomainHandler<Employee> meta = DomainHandlerProvider.getHandler(Employee.class);

    public static final Key<Employee, Long> id = meta.getKey("id");
    public static final Key<Employee, String> name = meta.getKey("name");
    public static final Key<Employee, City> city = meta.getKey("city");
    public static final Key<Employee, Employee> boss = meta.getKey("boss");
}
