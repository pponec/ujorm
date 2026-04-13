package org.ujorm.orm.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.DomainHandlerService;
import org.ujorm.core.Key;
import org.ujorm.orm.Config;
import org.ujorm.orm.model.ColumnModel;
import org.ujorm.orm.model.TableModel;
import org.ujorm.orm.model.TableModelBuilder;

import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;

/** Service provides meta models of domain objects */
public class TableModelService {
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
    private final ConcurrentHashMap<Class<?>, TableModel<?>> tableMap = new ConcurrentHashMap<>();

    private final DomainHandlerService domainService;

    private final Config config;

    /** Public constructor */
    public TableModelService(DomainHandlerService domainService, Config config) {
        this.domainService = domainService;
        this.config = config;
    }

    /** Public constructor */
    public TableModelService(Config config) {
        this(DomainHandlerProvider.provider(), config);
    }

    /** Protected constructor */
    public TableModelService() {
        this(DomainHandlerProvider.provider(), Config.ofDefault());
    }

    @NotNull
    public <D, V> ColumnModel<D, V> getColumnModel(Key<D, V> key, Connection dbConnection) {
        return getTableModel(key.domainClass(), dbConnection).getColumnOfKey(key);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <D> TableModel<D> getTableModel(Class<D> domainClass, Connection connection) {
        var result = (TableModel<D>) tableMap.get(domainClass);
        if (result == null) {
            synchronized (tableMap) {
                result = (TableModel<D>) tableMap.get(domainClass);
                if (result == null) {
                    result = createTableModel(domainClass, connection);
                    tableMap.put(domainClass, result);
                }
            }
        }
        return result;
    }

    @NotNull
    private <D> TableModel<D> createTableModel(Class<D> domainClass, Connection connection) {
        var handler = domainService.getHandler(domainClass);
        return TableModelBuilder.build(handler, config, connection);
    }

    /** Factory method for an Entity Manager */
    public <D, V> EntityManager<D, V> entityManager(@NotNull Class<D> domainClass, @Nullable Class<V> idType) {
        return EntityManager.of(domainClass, idType, this, config);
    }

    public static TableModelService of() {
        return new TableModelService();
    }
}