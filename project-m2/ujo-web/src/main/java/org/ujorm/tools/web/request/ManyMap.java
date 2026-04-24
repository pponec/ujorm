package org.ujorm.tools.web.request;


import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.util.*;

public final class ManyMap {

    private static final String[] EMPTY_ARRAY = new String[0];

    /** Internal map to store keys and their associated lists of values */
    private final Map<String, List<String>> map;

    public ManyMap(int capacity) {
        map = new HashMap<>(capacity);
    }

    public ManyMap() {
        this(10);
    }

    /** Method to add a value to the specified key */
    public void put(String key, String... values) {
        for (String value: values) {
            map.computeIfAbsent(key, k -> new ArrayList<>(2)).add(value);
        }
    }

    /** Method to retrieve the list of values for a specified key
     * If the key is not found, return an empty list */
    public List<String> getList(String key) {
        return map.getOrDefault(key, Collections.emptyList());
    }

    /** Method to retrieve the list of values for a specified key
     * If the key is not found, return an empty list */
    public String[] get(String key) {
        final List<String> result = map.get(key);
        return result == null || result.isEmpty()
                ? EMPTY_ARRAY
                : result.toArray(String[]::new);
    }

    /** Returns a key set */
    public Set<String> keySet() {
        return map.keySet();
    }

    /** Create new Servlet request */
    public URequest toRequest(Reader reader) {
        return new URequestImpl(this, reader);
    }

    public static @NotNull ManyMap of(Map<String, String> map) {
        ManyMap result = new ManyMap(map.size());
        map.forEach((key, value) -> result.put(key, value));
        return result;
    }
}