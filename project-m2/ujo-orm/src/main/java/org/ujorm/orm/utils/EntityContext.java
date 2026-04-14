package org.ujorm.orm.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.DomainHandlerService;
import org.ujorm.orm.Config;
import org.ujorm.orm.Crud;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.core.EntityManagerService;
import org.ujorm.orm.core.TableModelService;

import java.sql.Connection;
import java.util.logging.Level;

/** Entity Content. */
public class EntityContext {

    /** ORM configuration */
    private final Config config;
    /** Handler commonService */
    private final DomainHandlerService domainService;
    /** Returns the table model service for caching domain models. */
    private final TableModelService tableModelService;
    /** Entity Manager Service */
    private final EntityManagerService entityManagerService ;

    public EntityContext(Config config, DomainHandlerService domainService, TableModelService tableModelService, EntityManagerService entityManagerService) {
        this.config = config;
        this.domainService = domainService;
        this.tableModelService = tableModelService;
        this.entityManagerService = entityManagerService;
    }

    public Config config() {
        return config;
    }

    public <D> DomainHandler<D> handler(Class<D> domainClass) {
        return domainService.getHandler(domainClass);
    }

    public TableModelService tableModelService() {
        return tableModelService;
    }

    /** Find Entity Manager */
    public <D,V> EntityManager<D,V> entityManager(Class<D> domainClass) {
        return entityManager(domainClass, null);
    }

    /** Find Entity Manager */
    public <D,V> EntityManager<D,V> entityManager(Class<D> domainClass, @Nullable Class<V> idTypeIgnored) {
        return entityManagerService.entityManager(domainClass, idTypeIgnored);
    }

    /** Find Crud Manager */
    public <D,V> Crud<D,V> crud(Class<D> domainClass, Class<V> idType, Connection dbConnection) {
        return entityManager(domainClass, idType).crud(dbConnection);
    }

    /** Find Crud Manager */
    public <D,V> Crud<D,V> crud(Class<D> domainClass, Connection dbConnection) {
        return crud(domainClass, null, dbConnection);
    }

    // --- STATIC ---

    /** Factory method */
    public static EntityContext of(DomainHandlerService domainService, Config config) {
        var tableModelService = new TableModelService(domainService, config);
        var entityManagerService = new EntityManagerService(tableModelService, config);
        return new EntityContext(config, domainService, tableModelService, entityManagerService);
    }

    /** Factory method for user configuration. */
    public static EntityContext of(Config config) {
        return of(DomainHandlerProvider.provider(), config);
    }

    /** Common factory method creating context with default configuration. */
    public static EntityContext ofDefault() {
        return of(Config.ofDefault());
    }


    /** Factory method for logging SQL statements with the INFO level. */
    public static EntityContext ofSqlInfo() {
        var config = new Config()
                .setValue(Config.logSqlLevel, Level.INFO);
        return of(config.lock());
    }

    /**
     * Factory method for logging SQL statements and their parameters at the specified level.
     *
     * @param logLevel the logging level for SQL output
     * @return an entity context with SQL and parameter logging enabled
     */
    public static EntityContext ofSqlLogsWithParams(@NotNull Level logLevel) {
        var result = new Config()
                .setValue(Config.logSqlLevel, logLevel)
                .setValue(Config.logSqlParams, true);
        return of(result.lock());
    }

}
