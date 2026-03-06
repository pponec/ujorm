package org.ujorm.mapper.tutorial.domains.meta;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;
import org.ujorm.mapper.tutorial.domains.City;

/** Metamodel of the City entity */
public class MetaCity {
    static final DomainHandler<City> meta = DomainHandlerProvider.getHandler(City.class);
    public static final Key<City, Long> id = meta.getKey("id", Long.class);
    public static final Key<City, String> name = meta.getKey("name", String.class);
    public static final Key<City, String> countryCode = meta.getKey("countryCode", String.class);
}
