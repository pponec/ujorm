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
import org.ujorm.tools.msg.MsgFormatter;

/**
 * A store of domain object models
 * @author Pavel Ponec
 */
public class ModelContext {

    /** Domain - Model Map */
    private final HashMap<Class, AbstractDomainModel> map = new HashMap<>(8);

    /** A Temporary proxyDomainModels  */
    private List<XDomain> proxyDomainsModels = new ArrayList<>();

    /** An provider of public API */
    private final Tools tools = new Tools();

    public XDomain newModel() {
        final XDomain result = new XDomain();
        proxyDomainsModels.add(result);
        return result;
    }

    /** Close the domain store - including assigned models */
    public void close(@Nonnull final Object motherObject) {
        if (proxyDomainsModels != null) {
            try {
                final List<Field> fields = getFields(motherObject, proxyDomainsModels);
                for (int i = 0, max = proxyDomainsModels.size(); i < max; i++) {
                    final XDomain proxyDomain = proxyDomainsModels.get(i);
                    final Field field = fields.get(i);
                    field.setAccessible(true);
                    final Class modelClass = getClassFromGenerics(field, true);
                    final AbstractDomainModel abstractDomainModel = (AbstractDomainModel) modelClass.newInstance();
                    abstractDomainModel.getDirecKey().setContext(this);
                    map.put(modelClass, proxyDomain.get());
                    proxyDomain.close(abstractDomainModel);
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
    public AbstractDomainModel getDomainModel(@Nonnull final Class<AbstractDomainModel> domainClass) {
        AbstractDomainModel result = map.get(domainClass);
        if (result == null) {
            try {
                result = domainClass.newInstance();
            } catch (InstantiationException | IllegalAccessException  e) {
                throw new IllegalStateException("Can't reate an instance of the " + domainClass);
            }
            map.put(domainClass, result);
        }

        return result;
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
    protected List<Field> getFields(@Nonnull final Object container, @Nonnull final List<?> items) {
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
    protected Class getClassFromGenerics(@Nonnull final Field field, final boolean firstPosition) throws IllegalArgumentException {
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
