package org.ujorm.orm.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.orm.Config;
import org.ujorm.orm.impl.Context;
import org.ujorm.orm.service.CommonService;
import java.util.concurrent.ConcurrentHashMap;

/** Service provides meta models of domain objects */
public class EntityManagerService {
    /** A mapping a domain class to the domain handler object.
     * <p>
     * Note: This map is used with Double-Checked Locking in the {@code getHandler} method
     * instead of its {@code computeIfAbsent()} method to prevent severe issues during runtime:
     * <ul>
     *   <li><b>Long-running operation:</b> The handler creation generates and compiles
     *   Java source code dynamically. Using {@code computeIfAbsent()} would lock the map's bucket for a
     *   long time, blocking other unrelated threads.</li>
     *   <li><b>Recursive evaluation (Deadlock risk):</b> Domain models often reference other domain classes.
     *   A recursive call to {@code getHandler} during handler creation inside {@code computeIfAbsent()} would
     *   likely lead to thread deadlocks or {@code IllegalStateException}.</li>
     * </ul>
     * The {@code ConcurrentHashMap} is still strictly required to guarantee memory visibility and safe,
     * lock-free reads during the initial non-synchronized check.
     */
    private final ConcurrentHashMap<Class<?>, EntityManager<?,?>> map;
    private final Context context;

    public EntityManagerService(Context context) {
        this.map = new ConcurrentHashMap<>();
        this.context = context;
    }

    /** Get Entity Manager */
    @NotNull
    public <D, V> EntityManager<D,V> entityManagerFromSingleton(
            @NotNull Class<D> domainClass,
            @Nullable Class<V> idType) throws UnsupportedOperationException{
        if (!context.config().isEnabledUjormServiceProvider()) {
            throw new UnsupportedOperationException("Access is disabled by configuration");
        }
        return entityManager(domainClass, idType);
    }

    /**
     * Get Entity Manager
     * @param domainClass Domain class
     * @param ignoredIdType Only for generic typing, the value is ignored.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <D, V> EntityManager<D,V> entityManager(
            @NotNull Class<D> domainClass,
            @Nullable Class<V> ignoredIdType) {
        var result = (EntityManager<D,V>) map.get(domainClass);
        if (result == null) {
            synchronized (map) {
                result = (EntityManager<D,V>) map.get(domainClass);
                if (result == null) {
                    result = EntityManager.of(domainClass);
                    map.put(domainClass, result);
                }
            }
        }
        return result;
    }

    public static EntityManagerService of() {
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
                new CommonService());
        return new EntityManagerService(context);
    }

}