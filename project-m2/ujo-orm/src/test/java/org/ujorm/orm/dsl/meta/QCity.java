package org.ujorm.orm.dsl.meta;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;

import javax.annotation.processing.Generated;

/** Auto-generated metamodel for DCity */
@Generated("org.ujorm.maven.UjormMetaProcessor.SourceGenerator")
public abstract class QCity {

    private static final DomainHandler<City> meta = DomainHandlerProvider.getHandler(City.class);

    /** The id property */
    public static final Key<City, Long> id = meta.getKey("id");
    /** The name property */
    public static final Key<City, String> name = meta.getKey("name");
    /** The countryCode property */
    public static final Key<City, String> countryCode = meta.getKey("countryCode");
}
