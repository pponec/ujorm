package org.ujorm.mapper.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Singleton to provide EntityManagers.
 * This implementation is thread-safe using the Initialization-on-demand holder idiom.
 */
public final class EntityManagerProvider {

    /** Private constructor to prevent instantiation. */
    private EntityManagerProvider() {
    }

    /**
     * Holder class for lazy-loading the singleton instance.
     */
    private static final class Holder {
        private static final EntityManagerService INSTANCE = new EntityManagerService();
    }

    /**
     * Provides the EntityService instance.
     * @return EntityService
     */
    public static EntityManagerService provider() {
        return Holder.INSTANCE;
    }

    /**
     * Get the Entity Manager.
     * @param domainClass An original domain class
     * @return Entity
     */
    public static <D,V> EntityManager<D,V> em(@NotNull Class<D> domainClass, @Nullable Class<V> value) {
        return (EntityManager<D,V>) provider().entityManager(domainClass);
    }

}