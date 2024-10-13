package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;

import java.io.CharArrayReader;
import java.io.Reader;

public final class URequestImpl implements URequest{
    static final String[] emptyTexts = new String[0];

    private final ManyMap map ;

    private final Reader reader;

    public URequestImpl(ManyMap map, Reader reader ) {
        this.map = map;
        this.reader = reader;
    }

    public URequestImpl() {
        this(new ManyMap(), new CharArrayReader(new char[0]));
    }

    @NotNull
    public Reader getReader() {
        return reader;
    }

    @NotNull
    public String[] getParameterValues(final String key) {
        final String[] result = map.get(key);
        return result != null ? result : emptyTexts;
    }

    public void setParameter(String name, String value) {
        map.put(name, value);
    }
}
