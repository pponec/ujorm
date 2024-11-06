package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.web.ao.Reflections;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/** Servlet request context */
public class RContext {
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private final URequest uRequest;
    private final Appendable writer;

    public RContext(URequest uRequest, Appendable writer) {
        this.uRequest = uRequest;
        this.writer = writer;
    }

    public RContext(URequest uRequest) {
        this(uRequest, new StringBuilder());
    }

    public RContext() {
        this(URequestImpl.of(), new StringBuilder());
    }

    public URequest request() {
        return uRequest;
    }

    public Appendable writer() {
        return writer;
    }

    /** Returns the last parameter, or the null value. */
    public String getParameter(@NotNull String key) {
        return getParameter(key, null);
    }

    /** Returns the parameter names */
    public Set<String> getParameterNames() {
        return uRequest.getParameterNames();
    }

    /** Returns the last parameter */
    public String getParameter(@NotNull String key, String defaultValue) {
        final URequest uRequest = request();
        if (uRequest != null) {
            String[] params = uRequest.getParameters(key);
            return params.length > 0 ? params[params.length - 1] : defaultValue;
        }
        return defaultValue;
    }

    /** HTTP Servlet Factory */
    public static RContext ofServletResponse(Object httpServletResponse) {
        return ofServlet(null, httpServletResponse);
    }

    /** HTTP Servlet Factory */
    public static RContext ofServlet(@Nullable Object httpServletRequest, @NotNull Object httpServletResponse) {
        Reflections.setCharacterEncoding(httpServletResponse, CHARSET.name());
        final Appendable writer = Reflections.getServletWriter(httpServletResponse);
        final URequest ureq = httpServletRequest != null ? URequest.ofRequest(httpServletRequest) : URequestImpl.of();
        return new RContext(ureq, writer);
    }

    /** UContext from a map */
    public static RContext of(ManyMap map) {
        return new RContext(URequestImpl.ofMap(map), new StringBuilder());
    }

    /** UContext from a map */
    public static RContext of() {
        return of (new ManyMap());
    }
}
