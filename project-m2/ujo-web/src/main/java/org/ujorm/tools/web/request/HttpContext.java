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
    String getParameter(@NotNull String key);

    /** Returns the parameter names */
    Set<String> getParameterNames();

    /** Returns the last parameter */
    String getParameter(@NotNull String key, String defaultValue);

    /** Returns the type safe last parameter or the default value. */
    <T> T getParameter(@NotNull String key, @NotNull T defaultValue, @NotNull Function<String, T> converter);

    /** HTTP Servlet Factory */
    public static HttpContext ofServletResponse(Object httpServletResponse) {
        return ofServlet(null, httpServletResponse);
    }

    /** Create a default HTTP Context */
    public static HttpContext ofServlet(@Nullable Object httpServletRequest, @NotNull Object httpServletResponse) {
        return HttpContextImpl.ofServlet(httpServletRequest, httpServletResponse);
    }

    /** Create a default HTTP context from a map */
    public static HttpContext of(ManyMap map) {
        return new HttpContextImpl(URequestImpl.ofMap(map), new StringBuilder());
    }

    /** UContext from a map */
    public static HttpContext of() {
        return of (new ManyMap());
    }
}
