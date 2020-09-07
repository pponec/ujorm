/*
 * Copyright 2018-2020 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.set;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.common.ObjectUtils;

/**
 * Implementation of the Map interface where methods
 * {@code hash() } and {@code equals() } can be customized for all the Map.
 * @author Pavel Ponec
 */
public class CustomMap<K, V> implements Map<K, V>, Serializable {

    /** Original implementation */
    @Nonnull
    private final HashMap<MapKeyProxy<K>, V> impl;

    /** Factory to create a {@code proxyMapKey} instance */
    @Nonnull
    private final Function<K, MapKeyProxy<K>> keyFactory;

    /** The same mapper as a {@link  HashMap} */
    public CustomMap() {
        this((K key) -> new DefaultMapKey(key));
    }

    /** Mapper with a required equals and hasCode maker */
    public CustomMap(@Nonnull final Function<K, MapKeyProxy<K>> keyFactory) {
        this(new HashMap<>(), keyFactory);
    }

    /** Full configuration mapper */
    public CustomMap(@Nonnull final HashMap<MapKeyProxy<K>, V> impl, @Nonnull final Function<K, MapKeyProxy<K>> keyFactory) {
        this.impl = impl;
        this.keyFactory = keyFactory;
    }

    @Override
    public int size() {
        return impl.size();
    }

    @Override
    public boolean isEmpty() {
        return impl.isEmpty();
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return impl.containsKey(keyFactory.apply((K) key));
    }

    @Override
    public boolean containsValue(@Nullable final Object value) {
        return impl.containsValue(value);
    }

    @Override
    public V get(@Nonnull final Object key) {
        return impl.get(keyFactory.apply((K) key));
    }

    @Override
    public V put(@Nullable final K key, @Nullable final V value) {
        return impl.put(keyFactory.apply(key), value);
    }

    @Override
    public V remove(@Nullable final Object key) {
        return impl.remove(keyFactory.apply((K) key));
    }

    @Override
    public void putAll(@Nonnull final Map<? extends K, ? extends V> m) {
        for (K k : m.keySet()) {
            impl.put(keyFactory.apply(k), m.get(k));
        }
    }

    @Override
    public void clear() {
        impl.clear();
    }

    @Override
    public Set<K> keySet() {
        final Set<K> result = new HashSet<>(impl.size());
        for (MapKeyProxy<K> mapKey : impl.keySet()) {
            result.add(mapKey.getOriginal());
        }
        return result;
    }

    /** Returns a set fo the proxy key */
    public Set<MapKeyProxy<K>> keySetProxy() {
        return impl.keySet();
    }

    @Override
    public Collection<V> values() {
        return impl.values();
    }

    /** @deprecated Method is not implemented yet. */
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    //  ---- Temp class ---

    /**
     * A default implementation of the Map with customized hash() and equals() functions.
     * @author Pavel Ponec
     */
    protected static class DefaultMapKey<K> implements MapKeyProxy<K> {

        @Nullable
        protected final K originalKey;

        public DefaultMapKey(@Nullable final K originalKey) {
            this.originalKey = originalKey;
        }

        @Override
        public int hashCode() {
            return originalKey != null ? originalKey.hashCode() : 0;
        }

        @Override
        public boolean equals(@Nullable final Object proxyValue) {
            return this == proxyValue
                    || ObjectUtils.check(proxyValue, DefaultMapKey.class, v
                            -> Objects.equals(originalKey, v.getOriginal()));
        }

        /** Get original key */
        @Override
        public K getOriginal() {
            return originalKey;
        }

        @Override
        public String toString() {
            return String.valueOf(originalKey);
        }
    }
}
