package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.web.ao.Reflections;

import java.io.Reader;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface URequest {

    /** Request Reader */
    Reader getReader();

    /** Parameter provider */
    @NotNull
    String[] getParameters(final String key);

    /** Returns the last parameter */
    default String getParameter(@NotNull String key, @Nullable String defaultValue) {
        return getParameter(key, defaultValue, Function.identity());
    }

    /** Returns the last parameter */
    default <T> T getParameter(@NotNull String key, @NotNull T defaultValue, @NotNull Function<String, T> converter) {
        final var params = getParameters(key);
        if (params.length > 0) try {
                return converter.apply(params[params.length - 1]);
        } catch (Exception e) { /* continue */ }
        return defaultValue;
    }

    /** Parameter provider */
    @NotNull
    Set<String> getParameterNames();

    /** Convert the HttpServletRequest to the URequest */
    static URequest ofRequest(@Nullable final Object httpServletRequest) {
        Reflections.setCharacterEncoding(httpServletRequest, HttpContextImpl.CHARSET.name());
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
