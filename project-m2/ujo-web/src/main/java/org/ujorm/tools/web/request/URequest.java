package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;

public interface URequest {

    /** Request Reader */
    @NotNull
    Reader getReader();

    /** Parametr provider */
    @NotNull
    String[] getParameterValues(final String key);

    /** Convert the HttpServletRequest to the URequest */
    static URequest ofRequest(@NotNull HttpServletRequest req) {
        try {
            final Reader reader = req.getReader();
            req.setCharacterEncoding(UContext.CHARSET.name());

            final URequest result = new URequest() {
                @Override
                public Reader getReader() {
                    return reader;
                }

                @Override
                public String[] getParameterValues(String key) {
                    if (req != null) {
                        final String[] result = req.getParameterValues(key);
                        return result != null ? result : URequestImpl.emptyTexts;

                    } else {
                        return URequestImpl.emptyTexts;
                    }
                }
            };
            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
