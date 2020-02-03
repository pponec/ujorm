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

import java.io.Serializable;
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
import org.ujorm.tools.Assert;
import org.ujorm.tools.msg.MsgFormatter;
import org.ujorm2.Key;

/**
 *
 * @author Pavel Ponec
 */
public class KeyFactory<D> implements Serializable /*, Closeable*/ {

    private final Class<D> domainClass;

    private ArrayList<Key<D,?>> keys = new ArrayList<>();

    public KeyFactory(@Nonnull final Class<D> domainClass) {
        this.domainClass = Assert.notNull(domainClass, "domainClass");
    }

    /** Create new Key */
    public <VALUE> Key<D, VALUE> newKey(Function<D, VALUE> reader, BiConsumer<D, VALUE> writer) {
        final KeyImpl<D, VALUE> result = new KeyImpl(domainClass, UjoContext.of(), null);
        final KeyImpl.KeyWriter keyWriter = result.keyWriter();
        keyWriter.setWriter(writer);
        keyWriter.setReader(reader);

        keys.add(result);
        return result;
    }

    /** Create new Key */
    public <KEY extends Key, VALUE> KEY newRelation(Function<D, VALUE> reader, BiConsumer<D, VALUE> writer) {
        final Key<D, VALUE> result = new KeyImpl(domainClass, UjoContext.of(), null);
        keys.add(result);
        return (KEY) result;
    }

    public Class<D> getDomainClass() {
        return domainClass;
    }

    public Stream<Key<D, ?>> getKeys() {
        return keys.stream();
    }

    /** Close the factory */
    public void close() {
        final List<Field> fields = getFields();
        try {
            for (int i = 0, max = keys.size(); i < max; i++) {
                final KeyImpl.KeyWriter writer = ((KeyImpl) keys.get(i)).keyWriter();
                final Field field = findField(writer.key(), fields);

                writer.setIndex(i);
                writer.setName(field.getName());
                writer.setValueClass(getValueClass(field));
                writer.close();
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        keys.trimToSize();
    }

        // --- UTILS ---

    /** Get all UjoPorperty fields */
    public List<Field> getFields() {
        final int modiferPackage = 0;
        final int propertyModifier = Modifier.STATIC | modiferPackage | Modifier.FINAL;
        final Field[] fields = domainClass.getFields();
        final List<Field> result = new ArrayList<>(fields.length);
        for (int j = 0; j < fields.length; j++) {
            final Field field = fields[j];
            if (field.getModifiers() == propertyModifier
                    && Key.class.isAssignableFrom(field.getType())) {
                result.add(field);
            }
        }
        return result;
    }

    /** Find field */
    private Field findField(final Key key, final List<Field> fields)
            throws IllegalAccessException {
        for (Field field : fields) {
            if (field.get(null) == key) {
                return field;
            }
        }

        final String msg = MsgFormatter.format("Can't get a field for the key index #{} - {}.{}"
                , key.getIndex()
                , domainClass.getSimpleName()
                , key.getName());
        throw new IllegalStateException(msg);
    }

    /** Returns array of generic parameters
     * @param field Base field
     * @param valueType Argument {@code true} requires a VALUE class, other it is required DOMAIN class.
     * @return type
     * @throws IllegalArgumentException
     */
    private Class getValueClass(final Field field) throws IllegalArgumentException {
        final boolean valueType = true;
        try {
            final ParameterizedType type = (ParameterizedType) field.getGenericType();
            final Type[] types = type.getActualTypeArguments();
            final Type rawType = types[valueType ? types.length - 1 : 0];
            final Type result = valueType && rawType instanceof ParameterizedType
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
