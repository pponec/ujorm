package org.ujorm.orm.jdbc;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.ujorm.orm.Config;

import java.util.concurrent.ConcurrentHashMap;

/** Service provides meta models of domain objects */
@RequiredArgsConstructor
public class ResultSetMapperService {
    /** A mapping a domain class to the domain handler object.
     * <p>
     * Note: This map is used with Double-Checked Locking in the {@code getMapper} method
     * instead of its {@code computeIfAbsent()} method to prevent severe issues during runtime:
     * <ul>
     *   <li><b>Long-running operation:</b> The handler creation generates and compiles
     *   Java source code dynamically. Using {@code computeIfAbsent()} would lock the map's bucket for a
     *   long time, blocking other unrelated threads.</li>
     *   <li><b>Recursive evaluation (Deadlock risk):</b> Domain models often reference other domain classes.
     *   A recursive call to {@code getMapper} during handler creation inside {@code computeIfAbsent()} would
     *   likely lead to thread deadlocks or {@code IllegalStateException}.</li>
     * </ul>
     * The {@code ConcurrentHashMap} is still strictly required to guarantee memory visibility and safe,
     * lock-free reads during the initial non-synchronized check.
     */
    private final ConcurrentHashMap<Class<?>, ResultSetMapper> map = new ConcurrentHashMap<>();
    private final Config configuration;

    @NotNull @SuppressWarnings("unchecked")
    public <D> ResultSetMapper<D> getMapper(@NotNull Class<D> domainClass) {
        var result = (ResultSetMapper<D>) map.get(domainClass);
        if (result == null) {
            synchronized (map) {
                result = (ResultSetMapper<D>) map.get(domainClass);
                if (result == null) {
                    result = createMapper(domainClass);
                    map.put(domainClass, result);
                }
            }
        }
        return result;
    }

    @NotNull
    private <D> ResultSetMapper<D> createMapper(Class<D> domainModel) {
       return ResultSetMapper.of(domainModel, configuration);
    }

    public static ResultSetMapperService of(@NotNull Config config) {
        return new ResultSetMapperService(config);
    }

    public static ResultSetMapperService of() {
        return new ResultSetMapperService(Config.ofDefault());
    }

}
