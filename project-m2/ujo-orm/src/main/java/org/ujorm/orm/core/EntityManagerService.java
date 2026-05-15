package org.ujorm.orm.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.orm.Config;

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
    private final ConcurrentHashMap<Class<?>, EntityManager<?,?>> entityMap;
    private final TableModelService tableModelService;
    private final Config config;

    public EntityManagerService(TableModelService tableModelService, Config config) {
        this.entityMap = new ConcurrentHashMap<>();
        this.tableModelService = tableModelService;
        this.config = config;
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
        var result = (EntityManager<D,V>) entityMap.get(domainClass);
        if (result == null) {
            synchronized (entityMap) {
                result = (EntityManager<D,V>) entityMap.get(domainClass);
                if (result == null) {
                    result = EntityManager.of(domainClass, tableModelService, config);
                    entityMap.put(domainClass, result);
                }
            }
        }
        return result;
    }

    public static EntityManagerService of(TableModelService tableModelService, Config config) {
        return new EntityManagerService(tableModelService, config);
    }

}