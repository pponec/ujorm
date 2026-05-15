package org.ujorm.core.criterion.domains;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;

/** Auto-generated metamodel for City */
public abstract class MetaCity {

    private static final DomainHandler<City> meta = DomainHandlerProvider.getHandler(City.class);

    /** The id property */
    public static final Key<City, Long> id = meta.getKey("id");
    /** The name property */
    public static final Key<City, String> name = meta.getKey("name");
    /** The countryCode property */
    public static final Key<City, String> countryCode = meta.getKey("countryCode");
}
