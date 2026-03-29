package org.ujorm.core;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.generator.*;
import org.ujorm.core.impl.AbstractUjo;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/** Service provides meta models of domain objects */
public class DomainHandlerService {
    /** A mapping a domain class to the domain handler object. */
    private final ConcurrentHashMap<Class<?>, DomainHandler<?>> map = new ConcurrentHashMap<>();

    /** Enum converter */
    private final EnumMapper enumMapper = new EnumMapper();

    /** Protected constructor */
    protected DomainHandlerService() {}

    /** Get Enum Converter */
    public EnumMapper getEnumMapper() {
        return enumMapper;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <D> DomainHandler<D> getHandler(Class<D> domainClass) {
        var result = (DomainHandler<D>) map.get(domainClass);
        if (result == null) {
            synchronized (domainClass) {
                result = (DomainHandler<D>) map.get(domainClass);
                if (result == null) {
                    result = createHandler(domainClass);
                    map.put(domainClass, result);
                }
            }
        }
        return result;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private <D> DomainHandler<D> createHandler(Class<D> domainClass) {
        var handlerClassName = ClassName.ofGenerated(domainClass);
        var handlerClass = handlerClassName.classForName();
        if (handlerClass == null) {
            try {
                handlerClass = createClass(domainClass, handlerClassName);
            } catch (Exception ex) {
                var msg = "Cant create %s class for the domain: %s".formatted(handlerClassName, domainClass);
                throw new IllegalStateException(msg, ex);
            }
        }
        try {
            return (DomainHandler<D>) handlerClass.getConstructor().newInstance();
        } catch (Throwable ex) {
            throw new IllegalStateException("Cant create instance for the meta-model class: " + handlerClass.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private <D> Class<D> createClass(Class<D> domainClass, ClassName targetClassName) {
        verifyClass(domainClass);
        var meta = DomainModel.of(domainClass);
        var src = new JavaSourceGenerator().getSourceCode(meta, targetClassName);
        var result = new ClassGenerator().createClass(src, targetClassName);
        return (Class<D>) result;
    }

    /** Verify the class is a record or has a non-argument constructor */
    private void verifyClass(Class<?> domainClass) throws IllegalArgumentException {
        var beanConstructor = Arrays
                .stream(domainClass.getConstructors())
                .anyMatch(c -> c.getParameterCount() == 0);
        if (!domainClass.isRecord() && !beanConstructor) {
            throw new IllegalArgumentException("Only Bean and Record domain objects are supported");
        }
    }

    /** Convert Ujo object from the domain */
    @SuppressWarnings("unchecked")
    public <D> AbstractUjo<D> toUjo(D domainObject) {
        if (domainObject == null) {
            return null;
        }
        var handler = getHandler((Class<D>) domainObject.getClass());
        return AbstractUjo.of(domainObject, handler);
    }

    /** Create new domain object and set values if any. */
    public <D> D createDomainInstance(@NotNull Class<D> type, Object... values) {
        return getHandler(type).newDomain(values);
    }

    public static DomainHandlerService of() {
        return new DomainHandlerService();
    }
}