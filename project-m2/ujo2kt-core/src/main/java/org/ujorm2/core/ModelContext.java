/*
 *  Copyright 2020-2020 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.ujorm2.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.msg.MsgFormatter;

/**
 * A store of domain object models
 * @author Pavel Ponec
 */
public class ModelContext {

    /** An entity Domain class To Metamodel Map */
    private final HashMap<Class, AbstractDomainModel> map = new HashMap<>(8);

    /** A Temporary proxyDomainModels  */
    private List<ProxyDomain> proxyDomainsModels = new ArrayList<>();

    /** An provider of public API */
    private final Tools tools = new Tools();

    public ProxyDomain newModel() {
        final ProxyDomain result = new ProxyDomain();
        proxyDomainsModels.add(result);
        return result;
    }

    /** Close the domain store - including assigned models */
    public void close(@Nonnull final Object motherObject) {
        if (proxyDomainsModels != null) {
            try {
                final List<Field> fields = getFields(motherObject, proxyDomainsModels);
                for (int i = 0, max = proxyDomainsModels.size(); i < max; i++) {
                    final ProxyDomain proxyDomain = proxyDomainsModels.get(i);
                    final Field field = fields.get(i);
                    field.setAccessible(true);
                    final Class modelClass = getClassFromGenerics(field, true);
                    final AbstractDomainModel modelInstance = (AbstractDomainModel) modelClass.newInstance();
                    modelInstance.setContext(this); // assign a model context
                    proxyDomain.setModel(modelInstance);
                    Class entityClass = modelInstance.getDirectKey().getKeyFactory().getDomainClass();
                    map.put(entityClass, modelInstance);  // ERR
                }
            } catch (SecurityException | ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            } finally {
                // Close the object:
                proxyDomainsModels = null;
            }
        }
    }

    /** Get a (unique) direct domain model */
    @Nullable
    public AbstractDomainModel getDomainModel(@Nonnull final Class<AbstractDomainModel> domainClass) {
        return map.get(domainClass);
    }

    public Stream<AbstractDomainModel> getDomainModels() {
        return map.values().stream();
    }

    /** Provides an extended API */
    public final Tools getTools() {
        return tools;
    }

    // --- STATIC UTILS ---

    /** Get all final fileds from items on the same order */
    protected static List<Field> getFields(@Nonnull final Object container, @Nonnull final List<?> items) {
        final List<Field> result = new ArrayList<>(items.size());
        int counter = 0;
        try {
            fields:
            for (Field field : container.getClass().getDeclaredFields()) {
                if (Modifier.isFinal(field.getModifiers())) {
                    field.setAccessible(true);
                    final Object value = field.get(container);
                    for (int i = items.size() - 1; i >= 0; i--) {
                        if (value == items.get(i)) {
                            counter++;
                            result.add(i, field);
                            continue fields;
                        }
                    }
                }
            }
        } catch (SecurityException | ReflectiveOperationException e) {
            throw new IllegalStateException("Incorrect inicialization from the " + container.getClass(), e);
        }
        if (counter != items.size()) {
            throw new IllegalStateException("Incorrect inicialization from the " + container.getClass());
        }
        return result;
    }

    /** Returns a class of generic parameters
     * @param field Base field
     * @param firstPosition Argument {@code true} takes the first generic position or the value {@code false} takes the last one
     * @return type
     * @throws IllegalArgumentException
     */
    static protected Class getClassFromGenerics(@Nonnull final Field field, final boolean firstPosition) throws IllegalArgumentException {
        try {
            final ParameterizedType type = (ParameterizedType) field.getGenericType();
            final Type[] types = type.getActualTypeArguments();
            final Type rawType = types[firstPosition ? 0 : types.length - 1];
            final Type result = !firstPosition && rawType instanceof ParameterizedType
                    ? ((ParameterizedType) rawType).getRawType()
                    : rawType;
            return (result instanceof Class)
                    ? (Class) result
                    : Class.class;
        } catch (Exception e) {
            String msg = MsgFormatter.format("The generic scan failed on the field '{}'", field.getName());
            throw new IllegalStateException(msg, e);
        }
    }

    // --- Inner class()  ---

    public final class Tools {

        private Tools() {
        }

    }

}
