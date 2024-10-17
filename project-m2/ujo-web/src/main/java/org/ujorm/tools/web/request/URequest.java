package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.web.ao.Reflections;

import java.io.Reader;
import java.util.Map;

public interface URequest {

    /** Request Reader */
    Reader getReader();

    /** Parametr provider */
    @NotNull
    String[] getParameters(final String key);

    /** Convert the HttpServletRequest to the URequest */
    static URequest ofRequest(@Nullable final Object httpServletRequest) {
        Reflections.setCharacterEncoding(httpServletRequest, UContext.CHARSET.name());
        return new URequest() {
            Map<String, String[]> paramMap = null;

            @Override
            public Reader getReader() {
                return Reflections.getServletReader(httpServletRequest);
            }

            @Override
            public String[] getParameters(String key) {
                if (httpServletRequest != null) {
                    if (paramMap == null) {
                        paramMap = Reflections.getParameterMap(httpServletRequest);
                    }
                    final String[] result = paramMap.get(key);
                    return result != null ? result : URequestImpl.emptyTexts;

                } else {
                    return URequestImpl.emptyTexts;
                }
            }
        };
    }

}
