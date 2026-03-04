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
        private static final ResultSetMapperService INSTANCE = ResultSetMapperService.of();
    }

    /**
     * Provides the DomainHandlerProvider instance.
     * @return DomainHandlerProvider
     */
    public static ResultSetMapperService provider() {
        return Holder.INSTANCE;
    }

}