package org.ujorm.mapper.core;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.mapper.impl.Config;
import org.ujorm.mapper.impl.Context;
import org.ujorm.mapper.jdbc.ResultSetMapperService;
import org.ujorm.mapper.service.CommonService;

import java.util.concurrent.ConcurrentHashMap;

/** Service provides meta models of domain objects */
public class EntityManagerService {
    /** A mapping a domain class to the domain handler object. */
    private final ConcurrentHashMap<Class<?>, EntityManager<?,?>> map;
    private final Context context;

    public EntityManagerService(Context context) {
        this.map = new ConcurrentHashMap<>();
        this.context = context;
    }

    /** Get Entity Manager */
    @NotNull
    @SuppressWarnings("unchecked")
    public <D> EntityManager<D,?> entityManagerFromSingleton(@NotNull Class<D> domainClass) throws UnsupportedOperationException{
        if (!context.config().isEnabledEntityManagerProvider()) {
            throw new UnsupportedOperationException("Access is disabled by configuration");
        }
        return entityManager(domainClass);
    }

    /** Get Entity Manager */
    @NotNull
    @SuppressWarnings("unchecked")
    public <D> EntityManager<D,?> entityManager(@NotNull Class<D> domainClass) {
        var result = (EntityManager<D,?>) map.get(domainClass);
        if (result == null) {
            synchronized (domainClass) {
                result = (EntityManager<D,?>) map.get(domainClass);
                if (result == null) {
                    result = EntityManager.of(domainClass, null);
                    map.put(domainClass, result);
                }
            }
        }
        return result;
    }

    public static final EntityManagerService of() {
        var context = new Context(
                Config.ofDefault(),
                DomainHandlerProvider.provider(),
                new CommonService());
        return new EntityManagerService(context);
    }

    public static final EntityManagerService ofSingleton(Config config) {
        if (!config.isEnabledEntityManagerProvider()) {
            throw new UnsupportedOperationException("Access is disabled by configuration");
        }
        var context = new Context(
                config,
                DomainHandlerProvider.provider(),
                new CommonService()
        );
        return new EntityManagerService(context);
    }

}