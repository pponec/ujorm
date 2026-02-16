package org.ujorm.core.impl;

import org.jetbrains.annotations.Nullable;
import org.ujorm.mapper.ClassGenerator;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public class MetaModelProvider {

    private final ConcurrentHashMap<Class<?>, AbstractMetaModel> map = new ConcurrentHashMap<>();
    private final ClassGenerator classGenerator = new ClassGenerator();

    public AbstractMetaModel getModel(Class<?> domainModel) {
        var result = map.get(domainModel);
        if (result == null) {
            result = createModel(domainModel);
            map.put(domainModel, result);
        }
        return result;
    }

    private AbstractMetaModel createModel(Class<?> domainModel) {
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
            return (AbstractMetaModel) clazz.getConstructor().newInstance();
        } catch (Throwable ex) {
            throw new IllegalStateException("Cant create instnce for the meta-model class: " + clazz.getName());
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
        return "ujorm." + domainModel.getPackageName();
    }

}
