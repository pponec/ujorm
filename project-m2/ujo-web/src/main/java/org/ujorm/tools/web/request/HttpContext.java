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
    String parameter(@NotNull CharSequence key, String defaultValue);

    /** Returns the type safe last parameter or the default value. */
    <T> T parameter(@NotNull CharSequence key, @NotNull Function<String, T> converter, @NotNull T defaultValue);

    /** Returns the type safe last parameter or the default value. */
    default <T> T parameter(@NotNull CharSequence key, @NotNull Function<String, T> converter) {
        return parameter(key, converter, null);
    }

    /** HTTP Servlet Factory */
    static HttpContext ofServletResponse(Object httpServletResponse) {
        return ofServlet(null, httpServletResponse);
    }

    /** Create a default HTTP Context */
    static HttpContext ofServlet(@Nullable Object httpServletRequest, @NotNull Object httpServletResponse) {
        return HttpContextImpl.ofServlet(httpServletRequest, httpServletResponse);
    }

    /** Create a default HTTP context from a map */
    static HttpContext of(ManyMap map) {
        return new HttpContextImpl(URequestImpl.ofMap(map), new StringBuilder());
    }

    /** UContext from a map */
    static HttpContext of() {
        return of (new ManyMap());
    }

    // --- DEPRECATED METHODS ---

    /** Returns the last parameter or the null value.
     * @deprecated Use a method without the prefix `get` .*/
    @Deprecated
    default String getParameter(@NotNull CharSequence key) {
        return parameter(key);
    }

    /** Returns the parameter name set.
     * @deprecated Use a method without the prefix `get` .*/
    @Deprecated
    default Set<String> getParameterNames() {
        return parameterNames();
    };

    /** Returns the last parameter .
     * @deprecated Use a method without the prefix `get` .*/
    @Deprecated
    default String getParameter(@NotNull CharSequence key, String defaultValue) {
        return getParameter(key, defaultValue);
    };

    /** Returns the type safe last parameter or the default value..
     * @deprecated Use a method without the prefix `get` .*/
    @Deprecated
    default <T> T getParameter(@NotNull CharSequence key, @NotNull T defaultValue, @NotNull Function<String, T> converter) {
        return parameter(key, converter, defaultValue);
    }
}
