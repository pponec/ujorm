package org.ujorm.mapper.jdbc;

import org.jetbrains.annotations.NotNull;

/**
 * Singleton to provide Domain handlers.
 * This implementation is thread-safe using the Initialization-on-demand holder idiom.
 */
public final class ResultSetMapperProvider {

    /** Private constructor to prevent instantiation. */
    private ResultSetMapperProvider() {
    }

    /**
     * Holder class for lazy-loading the singleton instance.
     */
    private static final class Holder {
        private static final ResultSetMapper INSTANCE = new ResultSetMapper();
    }

    /**
     * Provides the DomainHandlerProvider instance.
     * @return DomainHandlerProvider
     */
    public static ResultSetMapper provider() {
        return Holder.INSTANCE;
    }

    /**
     * Gets a handler for the specified class.
     * @param clazz An original domain class
     * @return DomainHandler
     */
    public static <D> ResultSetMapper<D> getHandler(@NotNull Class<D> clazz) {
        return provider().getHandler(clazz);
    }
}