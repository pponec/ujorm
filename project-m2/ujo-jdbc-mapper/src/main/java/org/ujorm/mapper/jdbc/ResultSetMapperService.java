package org.ujorm.mapper.jdbc;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.ujorm.core.DomainHandlerService;
import org.ujorm.core.generator.ClassName;
import org.ujorm.mapper.impl.Config;

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

}
