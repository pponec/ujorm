package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.web.ao.Reflections;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class UContext {
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private final URequest uRequest;
    private final Appendable writer;

    public UContext(URequest uRequest, Appendable writer) {
        this.uRequest = uRequest;
        this.writer = writer;
    }

    public UContext(URequest uRequest) {
        this(uRequest, new StringBuilder());
    }

    public UContext() {
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
    public static UContext ofServletResponse(Object httpServletResponse) {
        return ofServlet(null, httpServletResponse);
    }

    /** HTTP Servlet Factory */
    public static UContext ofServlet(@Nullable Object httpServletRequest, @NotNull Object httpServletResponse) {
        Reflections.setCharacterEncoding(httpServletResponse, CHARSET.name());
        final Appendable writer = Reflections.getServletWriter(httpServletResponse);
        final URequest ureq = httpServletRequest != null ? URequest.ofRequest(httpServletRequest) : URequestImpl.of();
        return new UContext(ureq, writer);
    }

    /** UContext from a map */
    public static UContext of(ManyMap map) {
        return new UContext(URequestImpl.ofMap(map), new StringBuilder());
    }

    /** UContext from a map */
    public static UContext of() {
        return of (new ManyMap());
    }
}
