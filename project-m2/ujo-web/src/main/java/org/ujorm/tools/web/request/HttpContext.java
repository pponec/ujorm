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
        return parameter(key, converter, (T) null);
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
}
