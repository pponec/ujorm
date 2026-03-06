package org.ujorm.mapper.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.mapper.impl.Config;
import org.ujorm.mapper.impl.Context;
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
    public <D, V> EntityManager<D,V> entityManagerFromSingleton(
            @NotNull Class<D> domainClass,
            @Nullable Class<V> idType) throws UnsupportedOperationException{
        if (!context.config().isEnabledUjormServiceProvider()) {
            throw new UnsupportedOperationException("Access is disabled by configuration");
        }
        return entityManager(domainClass, idType);
    }

    /** Get Entity Manager */
    @NotNull
    @SuppressWarnings("unchecked")
    public <D, V> EntityManager<D,V> entityManager(
            @NotNull Class<D> domainClass,
            @Nullable Class<V> idType) {
        var result = (EntityManager<D,V>) map.get(domainClass);
        if (result == null) {
            synchronized (domainClass) {
                result = (EntityManager<D,V>) map.get(domainClass);
                if (result == null) {
                    result = EntityManager.of(domainClass);
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
        if (!config.isEnabledUjormServiceProvider()) {
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