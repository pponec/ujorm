package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Set;
import java.util.function.Function;

/** HTTP servlet request context */
public interface HttpContext {

    /** An abstract API of the HTTP request */
    URequest request();

    Appendable writer();

    /** Returns the last parameter or the null value. */
    String parameter(@NotNull CharSequence key);

    /** Returns the parameter name set */
    Set<String> parameterNames();

    /** Returns the last parameter */
    String parameter(@NotNull CharSequence key, @Nullable String defaultValue);

    /** Returns the type safe last parameter or the default value. */
    <T> T parameter(@NotNull CharSequence key, @NotNull Function<String, T> converter, @Nullable T defaultValue);

    /** Returns the type safe last parameter or the default value. */
    default <T> T parameter(@NotNull CharSequence key, @NotNull Function<String, T> converter) {
        return parameter(key, converter, null);
    }

    // --- DEPRECATED METHODS ---

    /** @deprecated Use a method without the prefix `get` */
    @Deprecated
    default String getParameter(@NotNull CharSequence key) {
        return parameter(key);
    }

    /** @deprecated Use a method without the prefix `get` */
    @Deprecated
    default Set<String> getParameterNames() {
        return parameterNames();
    }

    /** @deprecated Use a method without the prefix `get` */
    @Deprecated
    default String getParameter(@NotNull CharSequence key, @Nullable String defaultValue) {
        return parameter(key, defaultValue);
    }

    /** @deprecated Use a method without the prefix `get` */
    @Deprecated
    default <T> T getParameter(@NotNull CharSequence key, @Nullable T defaultValue, @NotNull Function<String, T> converter) {
        return parameter(key, converter, defaultValue);
    }

    // --- STATIC METHODS ---

    /** HTTP Servlet Factory */
    static HttpContext ofServletResponse(Object httpServletResponse) {
        return ofServlet(null, httpServletResponse);
    }

    /** Create a default HTTP Context */
    static HttpContext ofServlet(@Nullable Object httpServletRequest, @NotNull Object httpServletResponse) {
        return ExchangeContext.ofServlet(httpServletRequest, httpServletResponse);
    }

    /** UContext from a map */
    static HttpContext of() {
        return of(new StringBuilder());
    }

    static @NotNull HttpContext of(@NotNull Appendable writer) {
        return of(URequest.of(), writer);
    }

    /** Create a default HTTP context from the ManyMap */
    static @NotNull HttpContext of(@NotNull ManyMap map) {
        return of(URequestImpl.ofMap(map), new StringBuilder());
    }

    /** Create a default HTTP context from a map */
    static @NotNull HttpContext of(URequest request, @NotNull Appendable writer) {
        return new ExchangeContext(request, writer);
    }
}