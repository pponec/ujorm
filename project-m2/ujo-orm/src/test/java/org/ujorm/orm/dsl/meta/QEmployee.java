package org.ujorm.orm.dsl.meta;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;

import javax.annotation.processing.Generated;

/** Auto-generated metamodel for DEmployee */
@Generated("org.ujorm.maven.UjormMetaProcessor.SourceGenerator")
public interface QEmployee {

    Key<Employee, Long> id = meta().getKey("id");
    Key<Employee, String> name = meta().getKey("name");
    Key<Employee, City> city = meta().getKey("city");
    Key<Employee, Employee> boss = meta().getKey("boss");
    Key<Employee, EmployeeState> stateOrdinal = meta().getKey("stateOrdinal");
    Key<Employee, EmployeeState> stateString = meta().getKey("stateString");

    private static DomainHandler<Employee> meta() {
        return DomainHandlerProvider.getHandler(Employee.class);
    }
}
