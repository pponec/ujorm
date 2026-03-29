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
    Reader reader();

    /** Parameter provider */
    @NotNull
    String[] parameters(final CharSequence key);

    /** Returns the last parameter */
    default String parameter(@NotNull CharSequence key, @Nullable String defaultValue) {
        return parameter(key, Function.identity(), defaultValue);
    }

    /** Returns the last parameter */
    default <T> T parameter(@NotNull CharSequence key, @NotNull Function<String, T> converter, @NotNull T defaultValue) {
        final var params = parameters(key);
        if (params.length > 0) try {
                return converter.apply(params[params.length - 1]);
        } catch (Exception e) { /* continue */ }
        return defaultValue;
    }

    /** Parameter provider */
    @NotNull
    Set<String> parameterNames();

    /** Convert the HttpServletRequest to the URequest */
    static URequest ofRequest(@Nullable final Object httpServletRequest) {
        Reflections.setCharacterEncoding(httpServletRequest, HttpContextImpl.CHARSET.name());
        return new URequest() {
            Map<String, String[]> paramMap = null;

            @Override
            public Reader reader() {
                return Reflections.getServletReader(httpServletRequest);
            }

            @Override
            public String[] parameters(final CharSequence key) {
                if (httpServletRequest != null) {
                    final Map<String, String[]> paramMap = getMap(httpServletRequest);
                    final String[] result = paramMap.get(key.toString());
                    return result != null ? result : URequestImpl.emptyTexts;
                } else {
                    return URequestImpl.emptyTexts;
                }
            }

            @Override @NotNull
            public Set<String> parameterNames() {
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
