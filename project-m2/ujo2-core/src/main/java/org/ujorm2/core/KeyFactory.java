/*
 * Copyright 2012-2020 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm2.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.mockito.Mockito;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.msg.MsgFormatter;
import org.ujorm2.Key;

/**
 *
 * @author Pavel Ponec
 */
public class KeyFactory<D> /* implements Serializable , Closeable*/ {

    private final Class<? extends D> domainClass;

    private final ArrayList<Key<D,?>> keys = new ArrayList<>();

    @Nullable
    private ModelProvider modelProvider;

    public KeyFactory(@Nonnull final Class<? extends D> domainClass) {
        this.domainClass = Assert.notNull(domainClass, "domainClass");
    }

    /** Create new Key */
    public <K> Key<D, K> newKey(Function<D, K> reader, BiConsumer<D, K> writer) {
        final KeyImpl<D, K> result = new KeyImpl(domainClass);
        final KeyImpl.PropertyWriter keyWriter = result.getPropertyWriter();
        keyWriter.setWriter(writer);
        keyWriter.setReader(reader);

        keys.add(result);
        return result;
    }

    /** Create new Key */
    public <K extends AbstractDomainModel, VALUE> K newRelation(Function<D, VALUE> reader, BiConsumer<D, VALUE> writer) {
         final Object result = modelProvider.getDomainModel(domainClass);
         // TODO.pop ...
         return (K) Mockito.mock(AbstractDomainModel.class);
    }

    public Class<? extends D> getDomainClass() {
        return domainClass;
    }

    public Stream<Key<D, ?>> getKeys() {
        return keys.stream();
    }

    /** Close the factory */
    public void close(@Nonnull final ModelProvider modelProvider) {
        this.modelProvider = Assert.notNull(modelProvider, "modelProvider");
        final List<Field> fields = getFields(domainClass, keys);
        for (int i = 0, max = keys.size(); i < max; i++) {
            final KeyImpl key = (KeyImpl) keys.get(i);
            final KeyImpl.PropertyWriter writer = key.getPropertyWriter();
            final Field field = fields.get(i);

            if (key.getIndex() < 0) {
                writer.setIndex(i);
            }
            if (Check.isEmpty(key.getName())) {
                writer.setName(field.getName());
            }
            if (key.getValueClass() == null) {
                writer.setValueClass(getClassFromGenerics(field, false));
            }
            if (key.getReader() == null) {
                writer.setReader(null); // TODO: use a Java reflection by the: field.getName()
            }
            if (key.getWriter() == null) {
                writer.setWriter(null); // TODO: use a Java reflection by the: field.getName()
            }
            writer.close();
        }

        keys.trimToSize();
    }

    // --- STATIC UTILS ---

    /** Get all final fileds from items on the same order */
    static List<Field> getFields(@Nonnull final Object container, @Nonnull final List<?> items) {
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
    static Class getClassFromGenerics(@Nonnull final Field field, final boolean firstPosition) throws IllegalArgumentException {
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
}
