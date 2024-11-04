package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.web.ao.Reflections;

import java.io.Reader;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface URequest {

    /** Request Reader */
    Reader getReader();

    /** Parameter provider */
    @NotNull
    String[] getParameters(final String key);

    /** Parameter provider */
    @NotNull
    Set<String> getParameterNames();

    /** Convert the HttpServletRequest to the URequest */
    static URequest ofRequest(@Nullable final Object httpServletRequest) {
        Reflections.setCharacterEncoding(httpServletRequest, RContext.CHARSET.name());
        return new URequest() {
            Map<String, String[]> paramMap = null;

            @Override
            public Reader getReader() {
                return Reflections.getServletReader(httpServletRequest);
            }

            @Override
            public String[] getParameters(String key) {
                if (httpServletRequest != null) {
                    final Map<String, String[]> paramMap = getMap(httpServletRequest);
                    final String[] result = paramMap.get(key);
                    return result != null ? result : URequestImpl.emptyTexts;

                } else {
                    return URequestImpl.emptyTexts;
                }
            }

            @Override @NotNull
            public Set<String> getParameterNames() {
                return getMap(httpServletRequest).keySet();
            }

            @NotNull
            private Map<String, String[]> getMap(@NotNull Object httpServletRequest) {
                if (paramMap == null) {
                    paramMap = Reflections.getParameterMap(httpServletRequest);
                }
                return paramMap != null ? paramMap : Collections.emptyMap();
            }
        };
    }

}
