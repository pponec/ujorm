package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import java.io.CharArrayReader;
import java.io.Reader;
import java.util.Set;

public final class URequestImpl implements URequest {

    static final String[] emptyTexts = new String[0];

    private final ManyMap map;
    private final Reader reader;

    public URequestImpl(@NotNull ManyMap map, @NotNull Reader reader) {
        this.map = map;
        this.reader = reader;
    }

    @NotNull
    @Override
    public Reader reader() {
        return reader;
    }

    @NotNull
    @Override
    public String[] parameters(final CharSequence key) {
        var result = map.get(key.toString());
        return result != null ? result : emptyTexts;
    }

    @Override
    public @NotNull Set<String> parameterNames() {
        return map.keySet();
    }

    public void setParameter(@NotNull String name, @NotNull String value) {
        map.put(name, value);
    }

    public static URequestImpl ofMap(@NotNull ManyMap map) {
        return new URequestImpl(map, new CharArrayReader(new char[0]));
    }
}