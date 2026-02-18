package org.ujorm.mapper;

import org.ujorm.mapper.core.DomainHandler;

/** Singleton to provide Domain handlers */
public class DomainHandlerSingleton {

    private static DomainHandlerProvider provider = null;

    public static DomainHandlerProvider provider() {
        if (provider == null) {
            provider = new DomainHandlerProvider();
        }
        return provider;
    }

    public static DomainHandler getHandler(Class<?> clazz) {
        return provider().getHandler(clazz);
    }

}
