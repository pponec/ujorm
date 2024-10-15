package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import org.ujorm.tools.web.ao.Reflections;

import java.io.Reader;
import java.util.Map;

public interface URequest {

    /** Request Reader */
    @NotNull
    Reader getReader();

    /** Parametr provider */
    @NotNull
    String[] getParameterValues(final String key);

    /** Convert the HttpServletRequest to the URequest */
    static URequest ofRequest(@NotNull final Object httpServletRequest) {
        final Reader reader = Reflections.getServletReader(httpServletRequest);
        Reflections.setCharacterEncoding(httpServletRequest, UContext.CHARSET.name());

        final URequest result = new URequest() {
            final Map<String, String[]> paramMap = Reflections.getParameterMap(httpServletRequest);

            @Override
            public Reader getReader() {
                return reader;
            }

            @Override
            public String[] getParameterValues(String key) {
                if (httpServletRequest != null) {
                    final String[] result = paramMap.get(key);
                    return result != null ? result : URequestImpl.emptyTexts;

                } else {
                    return URequestImpl.emptyTexts;
                }
            }
        };
        return result;
    }

}
