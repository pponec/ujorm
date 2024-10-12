package org.ujorm.tools.web.context;


import java.io.Reader;
import java.util.*;

public class ManyMap {
    /** Internal map to store keys and their associated lists of values */
    private final Map<String, List<String>> map = new HashMap<>();

    /** Method to add a value to the specified key */
    public void put(String key, String value) {
        map.computeIfAbsent(key, k -> new ArrayList<>(2)).add(value);
    }

    /** Method to retrieve the list of values for a specified key
     * If the key is not found, return an empty list */
    public List<String> getList(String key) {
        return map.getOrDefault(key, Collections.emptyList());
    }

    /** Method to retrieve the list of values for a specified key
     * If the key is not found, return an empty list */
    public String[] get(String key) {
        return getList(key).toArray(new String[0]);
    }

    /** Create new Servlet request */
    public URequest toRequest(Reader reader) {
        return new URequest(this, reader);
    }
}