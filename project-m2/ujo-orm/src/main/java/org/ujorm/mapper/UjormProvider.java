package org.ujorm.mapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.mapper.core.EntityManager;
import org.ujorm.mapper.core.EntityManagerService;
import org.ujorm.mapper.impl.Config;
import org.ujorm.mapper.impl.ConfigImpl;
import org.ujorm.mapper.jdbc.ResultSetMapper;
import org.ujorm.mapper.jdbc.ResultSetMapperService;

import java.sql.Connection;

/**
 * Object provide a unique instnces of the {@link EntityManagerService} and {@ ResultSetMapperService} class.
 * This implementation is thread-safe using the Initialization-on-demand holder idiom.
 * The services provided by this class can be disabled via the 'enabledEntityManagerProvider' configuration parameter.
 */
public final class UjormProvider {

    private static final Config config = Config.ofDefault();

    /** Private constructor to prevent instantiation. */
    private UjormProvider() {
    }

    /**
     * Holder class for lazy-loading the singleton instance.
     */
    private static final class Holder {
        private static final EntityManagerService EMS_INSTANCE = EntityManagerService.ofSingleton(config);
        private static final ResultSetMapperService RSM_INSTANCE = ResultSetMapperService.ofSingleton(config);
    }

    //--- EntityManagerService ---

    /**
     * Provides the same instance every time.
     * @return EntityManagerService
     */
    public static EntityManagerService managerService() {
        return Holder.EMS_INSTANCE;
    }

    /**
     * Get the Entity Manager.
     * @param domainClass An original domain class
     * @return Entity
     */
    public static <D,V> EntityManager<D,V> em(@NotNull Class<D> domainClass, @Nullable Class<V> value) {
        return (EntityManager<D,V>) managerService().entityManagerFromSingleton(domainClass);
    }

    public <D> Crud crud(Class<D> domainClass, Connection connection) {
        return em(domainClass, null).crud(connection);
    }

    //--- ResultSetMapperService ---

    /**
     * Provides the same instance every time.
     * @return EntityManagerService
     */
    public static ResultSetMapperService mapperService() {
        return Holder.RSM_INSTANCE;
    }

    /**
     * Get the Entity Manager.
     * @param domainClass An original domain class
     * @return Entity
     */
    public static <D> ResultSetMapper<D> em2(@NotNull Class<D> domainClass) {
        return mapperService().getMapper(domainClass);
    }

    public <D> Crud map(Class<D> domainClass, Connection connection) {
        return em(domainClass, null).crud(connection);
    }


}