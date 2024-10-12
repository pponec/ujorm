package org.ujorm.tools.web.request;

import java.io.CharArrayReader;
import java.io.Reader;

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


    public static ManyMap createMap() {
        return new ManyMap();
    }
}
