package org.ujorm.tools.web.ao;

import java.io.CharArrayReader;
import java.io.Reader;
import java.util.*;

/** TODO: request.setCharacterEncoding(charset); */
public final class URequest {

    private final ManyMap map ;

    private final Reader reader;

    public URequest(ManyMap map, Reader reader ) {
        this.map = map;
        this.reader = reader;
    }

    public URequest() {
        this(new ManyMap(), new CharArrayReader(new char[0]));
    }

    public Reader getReader() {
        return reader;
    }

    public String[] getParameterValues(final String key) {
        return map.get(key);
    }

    public void setParameter(String name, String value) {
        map.put(name, value);
    }

    public static class ManyMap {
        /** Internal map to store keys and their associated lists of values */
        private final Map<String, List<String>> map = new HashMap<>();

        /** Method to add a value to the specified key */
        public void put(String key, String value) {
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
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

    public static ManyMap createMap() {
        return new ManyMap();
    }
}
