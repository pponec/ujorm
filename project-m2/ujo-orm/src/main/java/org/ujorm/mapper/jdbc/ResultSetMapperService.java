package org.ujorm.mapper.jdbc;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.DomainHandlerService;
import org.ujorm.mapper.impl.Config;
import org.ujorm.mapper.impl.ConfigImpl;

import java.util.concurrent.ConcurrentHashMap;

/** Service provides meta models of domain objects */
@RequiredArgsConstructor
public class ResultSetMapperService {
    /** A mapping a domain class to the domain handler object. */
    private final ConcurrentHashMap<Class<?>, ResultSetMapper> map = new ConcurrentHashMap<>();
    private final DomainHandlerService domainService;
    private final Config configuration;

    @NotNull
    public <D> ResultSetMapper<D> getMapper(@NotNull Class<D> domainClass) {
        var result = (ResultSetMapper<D>) map.get(domainClass);
        if (result == null) {
            synchronized (domainClass) {
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
       throw new UnsupportedOperationException("TODO");
    }

    public static final ResultSetMapperService of(@NotNull Config config) {
        return new ResultSetMapperService(DomainHandlerProvider.provider(), config);
    }

    public static final ResultSetMapperService of() {
        return new ResultSetMapperService(DomainHandlerProvider.provider(), new ConfigImpl());
    }

    public static final ResultSetMapperService ofSingleton() {
        var config = new ConfigImpl();
        if (!config.isEnabledEntityManagerProvider()) {
            throw new UnsupportedOperationException("Access is disabled by configuration");
        }
        return of(config);
    }

}
