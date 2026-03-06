package org.ujorm.mapper.core;

import org.jetbrains.annotations.NotNull;
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

    public EntityManagerService() {
        this.map = new ConcurrentHashMap<>();
        this.context = new Context(
                Config.ofDefault(),
                DomainHandlerProvider.provider(),
                new CommonService());
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
}