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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.ujorm2.Key;
import org.ujorm.tools.Assert;

/**
 *
 * @author Pavel Ponec
 */
public class KeyFactory<D> implements Serializable {

    private final Class<D> domainClass;

    private List<Key<D,?>> keys = new ArrayList<>();

    public KeyFactory(@Nonnull final Class<D> domainClass) {
        this.domainClass = Assert.notNull(domainClass, "domainClass");
    }

    /** Create new Key */
    public <VALUE> Key<D, VALUE> newKey(String name, Function<D, VALUE> reader, BiConsumer<D, VALUE> writer) {
        return null;
    }

    /** Create new Key */
    public <KEY extends Key, VALUE> KEY newRelation(String name, Function<D, VALUE> reader, BiConsumer<D, VALUE> writer) {
        return null;
    }

    public Class<D> getDomainClass() {
        return domainClass;
    }

    public List<Key<D, ?>> getKeys() {
        return keys;
    }



}
