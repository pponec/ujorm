package org.ujorm.tools.web.request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Set;
import java.util.function.Function;
import org.ujorm.tools.xml.config.XmlConfig;

/** HTTP servlet request context */
public interface AbstractExchangeContext {

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

    /** An alias for the method {@link #parameter(CharSequence)} */
    default String getParameter(@NotNull CharSequence key) {
        return parameter(key);
    }

    // --- DEPRECATED METHODS ---

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

    /** Jakarta Servlet factory. */
    static AbstractExchangeContext ofServlet(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
        return HttpContext.of(httpServletRequest, httpServletResponse, XmlConfig.ofDefault());
    }

    /** Jakarta Servlet factory. */
    static AbstractExchangeContext ofServlet(
            @NotNull HttpServletRequest httpServletRequest,
            @NotNull HttpServletResponse httpServletResponse,
            @NotNull XmlConfig config
    ) {
        return HttpContext.of(httpServletRequest, httpServletResponse, config);
    }

    /** Jakarta Servlet factory for response-only use-cases. */
    static AbstractExchangeContext ofServletResponse(@NotNull HttpServletResponse httpServletResponse) {
        return ofServlet((Object) null, (Object) httpServletResponse);
    }

    /**
     * Generic servlet-like factory (reflection-based).
     * Use this method for compatibility with non-Jakarta servlet APIs.
     */
    static AbstractExchangeContext ofServlet(@Nullable Object httpServletRequest, @NotNull Object httpServletResponse) {
        return ofServlet(httpServletRequest, httpServletResponse, XmlConfig.ofDefault());
    }

    /**
     * Generic servlet-like factory (reflection-based).
     * Use this method for compatibility with non-Jakarta servlet APIs.
     */
    static AbstractExchangeContext ofServlet(
            @Nullable Object httpServletRequest,
            @NotNull Object httpServletResponse,
            @NotNull XmlConfig config
    ) {
        final var charset = config.getCharset();
        if (httpServletRequest != null) {
            ServletBridge.setCharacterEncoding(httpServletRequest, charset.name());
        }
        ServletBridge.prepareHtmlResponse(httpServletResponse, charset, true);
        var writer = ServletBridge.getServletWriter(httpServletResponse);
        var request = httpServletRequest != null ? URequest.ofRequest(httpServletRequest, config) : URequest.of();
        return new ExchangeContext(request, writer);
    }
}