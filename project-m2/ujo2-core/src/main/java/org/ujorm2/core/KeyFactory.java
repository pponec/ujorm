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
import org.ujorm2.Key;

/**
 *
 * @author Pavel Ponec
 */
public class KeyFactory<D> /* implements Serializable , Closeable*/ {

    private final Class<? extends D> domainClass;

    private final ArrayList<XKey> keys = new ArrayList<>();

    @Nullable
    private ModelContext context;

    public KeyFactory(@Nonnull final Class<? extends D> domainClass) {
        this.domainClass = Assert.notNull(domainClass, "domainClass");
    }

    /** Create new Key */
    public <K> XKey<K> newKey(Function<D, K> reader, BiConsumer<D, K> writer) {
        XKey result = new XKey();
        keys.add(result);
        return result;
    }

    /** Create new Key */
    @Deprecated
    public <K> Key<D, K> newKeyOld(Function<D, K> reader, BiConsumer<D, K> writer) {
        final KeyImpl<D, K> result = new KeyImpl(domainClass);
//        final KeyImpl.PropertyWriter keyWriter = result.getPropertyWriter();
//        keyWriter.setWriter(writer);
//        keyWriter.setReader(reader);
//
//        keys.add(result);
        return result;
    }

    /** Create new Key */
    public <K extends AbstractDomainModel, VALUE> K newRelation(Function<D, VALUE> reader, BiConsumer<D, VALUE> writer) {
        // TODO:
        //final Object result = context.getDomainModel(domainClass);


        return (K) Mockito.mock(AbstractDomainModel.class);
    }

    public Class<? extends D> getDomainClass() {
        return domainClass;
    }

    public Stream<Key<D, ?>> getKeys() {
        return keys.stream().map(t -> t.get());
    }

    /** Close the factory */
    public void close(@Nonnull final ModelContext context) {
        this.context = Assert.notNull(context, "modelProvider");
        final List<Field> fields = context.getFields(domainClass, keys);
        for (int i = 0, max = keys.size(); i < max; i++) {
            final KeyImpl key = (KeyImpl) keys.get(i).get();
            final KeyImpl.PropertyWriter writer = key.getPropertyWriter();
            final Field field = fields.get(i);

            if (key.getIndex() < 0) {
                writer.setIndex(i);
            }
            if (Check.isEmpty(key.getName())) {
                writer.setName(field.getName());
            }
            if (key.getValueClass() == null) {
                writer.setValueClass(context.getClassFromGenerics(field, false));
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

}
