package org.ujorm.mapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.mapper.core.EntityManager;
import org.ujorm.mapper.core.EntityManagerService;
import org.ujorm.mapper.impl.Config;
import org.ujorm.mapper.jdbc.ResultSetMapper;
import org.ujorm.mapper.jdbc.ResultSetMapperService;

import java.sql.Connection;

/**
 * Object provides unique instances of the {@link EntityManagerService} and {@link ResultSetMapperService} classes.
 * This implementation is thread-safe using the Initialization-on-demand holder idiom.
 * The services provided by this class can be disabled via the 'enabledUjormServiceProvider' configuration parameter.
 */
public final class UjormServiceProvider {

    private static final Config config = Config.ofDefault();

    /** Private constructor to prevent instantiation. */
    private UjormServiceProvider() {
    }

    /** Holder class for lazy-loading the singleton instances. */
    private static final class Holder {
        private static final EntityManagerService EMS_INSTANCE = EntityManagerService.ofSingleton(config);
        private static final ResultSetMapperService RSM_INSTANCE = ResultSetMapperService.ofSingleton(config);
    }

    //--- EntityManagerService ---

    /**
     * Provides the singleton instance of the EntityManagerService.
     * @return EntityManagerService
     */
    public static EntityManagerService managerService() {
        return Holder.EMS_INSTANCE;
    }

    /**
     * Gets the Entity Manager for the specified domain class.
     * @param domainClass An original domain class
     * @param idType Optional value class (Note: currently unused in the underlying implementation)
     * @param <D> Domain type
     * @param <V> Value type
     * @return EntityManager instance
     */
    @SuppressWarnings("unchecked")
    public static <D, V> EntityManager<D, V> em(@NotNull Class<D> domainClass, @Nullable Class<V> idType) {
        return managerService().entityManagerFromSingleton(domainClass, idType);
    }

    /**
     * Creates a Crud operation object for the given domain class.
     * @param domainClass An original domain class
     * @param connection Database connection
     * @param idType Optional value class (Note: currently unused in the underlying implementation)
     * @param <D> Domain type
     * @param <V> Value type
     * @return Crud instance
     */
    public static <D, V> Crud<D, V> crud(@NotNull Class<D> domainClass, @NotNull Connection connection, @Nullable Class<V> idType) {
        return em(domainClass, idType).crud(connection);
    }

    //--- ResultSetMapperService ---

    /**
     * Provides the singleton instance of the ResultSetMapperService.
     * @return ResultSetMapperService
     */
    public static ResultSetMapperService mapperService() {
        return Holder.RSM_INSTANCE;
    }

    /**
     * Gets the ResultSet Mapper for the specified domain class.
     * @param domainClass An original domain class
     * @param <D> Domain type
     * @return ResultSetMapper instance
     */
    public static <D> ResultSetMapper<D> map(@NotNull Class<D> domainClass) {
        return mapperService().getMapper(domainClass);
    }

}