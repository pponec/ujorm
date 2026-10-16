package org.ujorm.core;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.generator.*;
import org.ujorm.core.impl.AbstractUjo;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/** Service provides meta models of domain objects */
public class DomainHandlerService {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(DomainHandlerService.class.getName());
    /**
     * A GraalVM native image provides neither the reflection of a domain class nor a compiler,
     * and it is built from one build, so the check of an outdated handler is skipped there.
     */
    private static final boolean NATIVE_IMAGE = System.getProperty("org.graalvm.nativeimage.imagecode") != null;
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
    private final ConcurrentHashMap<Class<?>, DomainHandler<?>> domainMap = new ConcurrentHashMap<>();

    /** Protected constructor */
    protected DomainHandlerService() {}

    @NotNull
    @SuppressWarnings("unchecked")
    public <D> DomainHandler<D> getHandler(Class<D> domainClass) {
        var result = (DomainHandler<D>) domainMap.get(domainClass);
        if (result == null) {
            synchronized (domainMap) {
                result = (DomainHandler<D>) domainMap.get(domainClass);
                if (result == null) {
                    result = createHandler(domainClass);
                    domainMap.put(domainClass, result);
                }
            }
        }
        return result;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private <D> DomainHandler<D> createHandler(Class<D> domainClass) {
        var handlerClassName = ClassName.ofGenerated(domainClass);
        var handlerClass = handlerClassName.classForName(domainClass.getClassLoader());
        if (handlerClass != null && !NATIVE_IMAGE && isOutdated(domainClass, handlerClass, handlerClassName)) {
            var msg = ("The pre-compiled handler %s doesn't match the domain class, so build the project cleanly"
                    + " or run the %s again.").formatted(handlerClassName, HandlerPrecompiler.class.getSimpleName());
            if (!ClassGenerator.isCompilerAvailable()) {
                throw new IllegalStateException(msg);
            }
            LOGGER.warning(msg + " The handler is generated at runtime now.");
            handlerClass = null;
        }
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

    /**
     * Compares the model hash of the pre-compiled handler with the current domain class.
     * A handler without the hash comes from an older release, so it is outdated too.
     * The check costs a reflection of the domain class and a generation of its source code, without a compilation.
     */
    boolean isOutdated(Class<?> domainClass, Class<?> handlerClass, ClassName handlerClassName) {
        var expected = new JavaSourceGenerator().getModelHash(DomainModel.of(domainClass), handlerClassName);
        try {
            return !expected.equals(handlerClass.getField(JavaSourceGenerator.MODEL_HASH_FIELD).get(null));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return true; // An incompatible handler of an older release fails on its initialization
        }
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

    /** Create new Instance
     * @see DomainHandlerProvider#getHandler(Class)
     */
    public static DomainHandlerService of() {
        return new DomainHandlerService();
    }
}