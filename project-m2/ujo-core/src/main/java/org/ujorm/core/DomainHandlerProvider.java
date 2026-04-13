package org.ujorm.core;

import org.jetbrains.annotations.NotNull;

/**
 * Singleton to provide Domain handlers.
 * This implementation is thread-safe using the Initialization-on-demand holder idiom.
 */
public final class DomainHandlerProvider {

    /** Private constructor to prevent instantiation. */
    private DomainHandlerProvider() {
    }

    /**
     * Holder class for lazy-loading the singleton instance.
     */
    private static final class Holder {
        private static final DomainHandlerService INSTANCE = new DomainHandlerService();
    }

    /**
     * Provides the DomainHandlerService instance.
     * @return DomainHandlerService
     */
    public static DomainHandlerService provider() {
        return Holder.INSTANCE;
    }

    /**
     * Gets a handler for the specified class.
     * @param clazz An original domain class
     * @return DomainHandler
     */
    @NotNull
    public static <D> DomainHandler<D> getHandler(@NotNull Class<D> clazz) {
        return provider().getHandler(clazz);
    }
}