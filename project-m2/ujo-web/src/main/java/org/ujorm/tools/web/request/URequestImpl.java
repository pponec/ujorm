package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;

import java.io.CharArrayReader;
import java.io.Reader;
import java.util.Set;

public final class URequestImpl implements URequest{
    static final String[] emptyTexts = new String[0];

    private final ManyMap map ;

    private final Reader reader;

    public URequestImpl(@NotNull ManyMap map, @NotNull Reader reader ) {
        this.map = map;
        this.reader = reader;
    }


    @NotNull
    public Reader getReader() {
        return reader;
    }

    @NotNull
    public String[] getParameters(final String key) {
        final String[] result = map.get(key);
        return result != null ? result : emptyTexts;
    }

    @Override
    public @NotNull Set<String> getParameterNames() {
        return map.keySet();
    }

    public void setParameter(@NotNull String name, @NotNull String value) {
        map.put(name, value);
    }

    public static URequestImpl ofMap(@NotNull ManyMap map) {
        return new URequestImpl(map, new CharArrayReader(new char[0]));
    }

    public static URequestImpl of() {
        return new URequestImpl(new ManyMap(), new CharArrayReader(new char[0]));
    }
}
