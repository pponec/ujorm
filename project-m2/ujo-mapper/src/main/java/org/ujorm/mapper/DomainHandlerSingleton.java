package org.ujorm.mapper;

import org.jetbrains.annotations.NotNull;
import org.ujorm.mapper.core.DomainHandler;

/**
 * Singleton to provide Domain handlers.
 * This implementation is thread-safe using the Initialization-on-demand holder idiom.
 */
public final class DomainHandlerSingleton {

    /** Private constructor to prevent instantiation. */
    private DomainHandlerSingleton() {
    }

    /**
     * Holder class for lazy-loading the singleton instance.
     */
    private static final class Holder {
        private static final DomainHandlerProvider INSTANCE = new DomainHandlerProvider();
    }

    /**
     * Provides the DomainHandlerProvider instance.
     * @return DomainHandlerProvider
     */
    public static DomainHandlerProvider provider() {
        return Holder.INSTANCE;
    }

    /**
     * Gets a handler for the specified class.
     * @param clazz Domain class
     * @return DomainHandler
     */
    public static DomainHandler get(@NotNull Class<?> clazz) {
        return provider().getHandler(clazz);
    }
}