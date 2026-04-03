package org.ujorm.orm.dsl.meta;

import org.ujorm.orm.tutorial.domains.Employee;
import javax.annotation.processing.Generated;
import org.ujorm.core.Key;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;

/** Auto-generated metamodel for Employee */
@Generated("org.ujorm.maven.UjormMetaProcessor.SourceGenerator")
public class MetaEmployee {

    private static final DomainHandler<Employee> meta = DomainHandlerProvider.getHandler(Employee.class);

    public static final Key<Employee, Long> id = meta.getKey("id");
    public static final Key<Employee, String> name = meta.getKey("name");
    public static final Key<Employee, org.ujorm.orm.tutorial.domains.City> city = meta.getKey("city");
    public static final Key<Employee, org.ujorm.orm.tutorial.domains.Employee> boss = meta.getKey("boss");

    /** Creates a table alias for the Employee entity */
    public static TableAlias<Employee> as(String alias) {
        return new TableAlias<>(alias);
    }

    /** Creates a Key for the Employee alias */
    public static <V> Key<Employee, V> as(String alias, Key<Employee,V> key) {
        return as(alias).key(key);
    }
}