package org.ujorm.orm.dsl.meta;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;
import org.ujorm.orm.dsl.TableAlias;

import javax.annotation.processing.Generated;

/** Auto-generated metamodel for DEmployee */
@Generated("org.ujorm.maven.UjormMetaProcessor.SourceGenerator")
public abstract class QEmployee {

    private static final DomainHandler<Employee> meta = DomainHandlerProvider.getHandler(Employee.class);

    public static final Key<Employee, Long> id = meta.getKey("id");
    public static final Key<Employee, String> name = meta.getKey("name");
    public static final Key<Employee, City> city = meta.getKey("city");
    public static final Key<Employee, Employee> boss = meta.getKey("boss");

    /** Creates a table alias for the Employee entity */
    public static TableAlias<Employee> as(String alias) {
        return new TableAlias<>(alias, Employee.class);
    }
}

