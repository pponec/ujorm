package org.ujorm.core;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.generator.ClassGenerator;
import org.ujorm.core.generator.ClassName;
import org.ujorm.core.generator.DomainModel;
import org.ujorm.core.generator.JavaSourceGenerator;
import org.ujorm.core.impl.AbstractUjo;

import java.util.concurrent.ConcurrentHashMap;

/** Service provides meta models of domain objects */
public class DomainHandlerService {
    /** A mapping a domain class to the domain handler object. */
    private final ConcurrentHashMap<Class<?>, DomainHandler> map = new ConcurrentHashMap<>();

    @NotNull
    public <D> DomainHandler<D> getHandler(Class<D> domainClass) {
        var result = (DomainHandler<D>) map.get(domainClass);
        if (result == null) {
            result = createHandler(domainClass);
            map.put(domainClass, result);
        }
        return result;
    }

    @NotNull
    private <D> DomainHandler<D> createHandler(Class<D> domainModel) {
        var handlerClassName = ClassName.ofGenerated(domainModel);
        var handlerClass = handlerClassName.classForName();
        if (handlerClass == null) {
            synchronized (domainModel) {
                handlerClass = handlerClassName.classForName();
                if (handlerClass == null) {
                    try {
                        handlerClass = createClass(domainModel, handlerClassName);
                    } catch (Exception ex) {
                        var msg = "Cant create %s class for the domain: %s".formatted(handlerClassName, domainModel);
                        throw new IllegalStateException(msg, ex);
                    }
                }
            }
        }
        try {
            return (DomainHandler<D>) handlerClass.getConstructor().newInstance();
        } catch (Throwable ex) {
            throw new IllegalStateException("Cant create instance for the meta-model class: " + handlerClass.getName());
        }
    }

    private <D> Class<D> createClass(Class<D> domainModel, ClassName targetClassName) {
        var meta = DomainModel.of(domainModel);
        var src = new JavaSourceGenerator().getSourceCode(meta, targetClassName);
        var result = new ClassGenerator().createClass(src, targetClassName);
        return (Class<D>) result;
    }

    /** Convert Ujo object from the domain */
    public <D> AbstractUjo<D> toUjo(D domainObject) {
        if (domainObject != null) return null;
        var handler = (DomainHandler<D>) getHandler(domainObject.getClass());
        return AbstractUjo.of(domainObject, handler);
    }

}
