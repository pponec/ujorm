package org.ujorm.mapper;

import org.jetbrains.annotations.Nullable;
import org.ujorm.mapper.generator.ClassGenerator;
import org.ujorm.mapper.core.DomainHandler;

import java.util.concurrent.ConcurrentHashMap;

/** Service provides meta models of domain objects */
public class DomainHandlerService {

    public static final String PACKAGE_PREFIX = "org.ujorm.gen_.";

    private final ConcurrentHashMap<Class<?>, DomainHandler> map = new ConcurrentHashMap<>();
    private final ClassGenerator classGenerator = new ClassGenerator();

    public <D> DomainHandler<D> getHandler(Class<D> domainModel) {
        var result = map.get(domainModel);
        if (result == null) {
            result = createModel(domainModel);
            map.put(domainModel, result);
        }
        return result;
    }

    private DomainHandler createModel(Class<?> domainModel) {
        var packageName = getModelImplPath(domainModel);
        var simpleClassName = domainModel.getSimpleName() + '_';
        var fullClassName = packageName + '.' + simpleClassName;
        var clazz = loadClassName(fullClassName);
        if (clazz == null) {
            synchronized (domainModel) {
                clazz = loadClassName(fullClassName);
                if (clazz == null) {
                    try {
                        clazz = classGenerator.createClass(createClassTemplate(fullClassName), fullClassName);
                    } catch (Exception ex) {
                        throw new IllegalStateException("Cant create %s class for the domain: %s".formatted(fullClassName, domainModel));
                    }
                }
            }
        }
        try {
            return (DomainHandler) clazz.getConstructor().newInstance();
        } catch (Throwable ex) {
            throw new IllegalStateException("Cant create instance for the meta-model class: " + clazz.getName());
        }
    }

    @Nullable
    private Class<?> loadClassName(String fullClassName) {
        try {
            return Class.forName(fullClassName);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    private String createClassTemplate(String domainModel) {
        return ""; // TODO
    }

    private String getModelImplPath(Class<?> domainModel) {
        return PACKAGE_PREFIX + domainModel.getPackageName();
    }

}
