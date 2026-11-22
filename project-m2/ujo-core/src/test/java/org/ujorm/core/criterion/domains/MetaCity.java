package org.ujorm.core.criterion.domains;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;

/** Auto-generated metamodel for City */
public interface MetaCity {

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
