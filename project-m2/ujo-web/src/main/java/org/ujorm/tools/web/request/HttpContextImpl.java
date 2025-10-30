package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.web.ao.Reflections;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/** A default implementation of the HTTP servlet request context */
public class HttpContextImpl implements HttpContext{

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private final URequest uRequest;
    private final Appendable writer;

    public HttpContextImpl(URequest uRequest, Appendable writer) {
        this.uRequest = uRequest;
        this.writer = writer;
    }

    public HttpContextImpl(URequest uRequest) {
        this(uRequest, new StringBuilder());
    }

    public HttpContextImpl() {
        this(URequestImpl.of(), new StringBuilder());
    }

    /** An abstract API of the HTTP request */
    public URequest request() {
        return uRequest;
    }

    /** Returns a writer of the HTTP response*/
    @Override
    public Appendable writer() {
        return writer;
    }

    /** Returns the last parameter or the null value. */
    public String getParameter(@NotNull String key) {
        return getParameter(key, null);
    }

    /** Returns the parameter names */
    public Set<String> getParameterNames() {
        return uRequest.getParameterNames();
    }

    /** Returns the last parameter */
    @Override
    public String getParameter(@NotNull String key, String defaultValue) {
        final URequest uRequest = request();
        if (uRequest != null) {
            String[] params = uRequest.getParameters(key);
            return params.length > 0 ? params[params.length - 1] : defaultValue;
        }
        return defaultValue;
    }

    /** Create a default HTTP Context */
    public static HttpContext ofServlet(
            @Nullable final Object httpServletRequest,
            @NotNull final Object httpServletResponse) {
        verifyClass(httpServletRequest, "HttpServletRequest");
        verifyClass(httpServletResponse, "HttpServletResponse");
        Reflections.setCharacterEncoding(httpServletResponse, CHARSET.name());
        final Appendable writer = Reflections.getServletWriter(httpServletResponse);
        final URequest ureq = httpServletRequest != null ? URequest.ofRequest(httpServletRequest) : URequestImpl.of();
        return new HttpContextImpl(ureq, writer);
    }

    /**
     * Verifies that the given instance has the expected simple class name.
     *
     * @param instance         the object to verify, may be {@code null}
     * @param simpleClassName  the expected simple class name
     * @throws IllegalArgumentException if the instance is not {@code null} and its class name does not match the expected name
     */
    private static void verifyClass(@Nullable Object instance, @NotNull String simpleClassName) {
        if (instance != null && !instance.getClass().getSimpleName().contains(simpleClassName)) {
            throw new IllegalArgumentException("Expected class name '%s' but received '%s'."
                    .formatted(simpleClassName, instance.getClass().getSimpleName()));
        }
    }
}
