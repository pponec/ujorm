package org.ujorm.orm.dsl.meta;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;

import javax.annotation.processing.Generated;

/** Auto-generated metamodel for DCity */
@Generated("org.ujorm.maven.UjormMetaProcessor.SourceGenerator")
public interface QCity {

    /** The id property */
    Key<City, Long> id = meta().getKey("id");
    /** The name property */
    Key<City, String> name = meta().getKey("name");
    /** The countryCode property */
    Key<City, String> countryCode = meta().getKey("countryCode");

    private static DomainHandler<City> meta() {
        return DomainHandlerProvider.getHandler(City.class);
    }
}
