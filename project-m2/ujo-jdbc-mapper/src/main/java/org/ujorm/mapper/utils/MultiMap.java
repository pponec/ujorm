package org.ujorm.mapper.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/** A concise generic MultiMap implementation. */
public class MultiMap<K, V> {

    private final Map<K, List<V>> map = new HashMap<>();
    private final int initialListCapacity;

    public MultiMap(int initialListCapacity) {
        this.initialListCapacity = initialListCapacity;
    }

    public MultiMap() {
        this(16);
    }

    /** Adds a value to the specified key. */
    public void put(@NotNull K key, @NotNull V value) {
        map.computeIfAbsent(key, k -> new ArrayList<>(initialListCapacity)).add(value);
    }

    /** Retrieves all values or an empty list. */
    @NotNull
    public List<V> get(@NotNull K key) {
        return map.getOrDefault(key, Collections.emptyList());
    }

    @NotNull
    public Set<K> keySet() {
        return map.keySet();
    }
}