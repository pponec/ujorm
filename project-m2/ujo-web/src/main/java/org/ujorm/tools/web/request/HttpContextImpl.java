package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.web.ao.Reflections;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Function;

/** A default implementation of the HTTP servlet request context */
public class HttpContextImpl implements HttpContext {

    /** Default charset */
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private final URequest uRequest;
    private final Appendable writer;

    /** Default constructor */
    public HttpContextImpl(URequest uRequest, Appendable writer) {
        this.uRequest = uRequest;
        this.writer = writer;
    }

    /** Constructor with a default StringBuilder */
    public HttpContextImpl(URequest uRequest) {
        this(uRequest, new StringBuilder());
    }

    /** An abstract API of the HTTP request */
    @Override
    public URequest request() {
        return uRequest;
    }

    /** Returns a writer of the HTTP response */
    @Override
    public Appendable writer() {
        return writer;
    }

    /** Returns the last parameter or the null value */
    @Override
    public String parameter(@NotNull CharSequence key) {
        return parameter(key, (String) null);
    }

    /** Returns the parameter names */
    @Override
    public Set<String> parameterNames() {
        return uRequest.parameterNames();
    }

    /** Returns the last parameter */
    @Override
    public String parameter(@NotNull CharSequence key, @Nullable String defaultValue) {
        return parameter(key, Function.identity(), defaultValue);
    }

    /** Returns the last parameter */
    @Override
    public <T> T parameter(@NotNull CharSequence key, @NotNull Function<String, T> converter, @Nullable T defaultValue) {
        return uRequest.parameter(key, converter, defaultValue);
    }

    /** Return a text of the writer object */
    @Override
    public String toString() {
        return writer.toString();
    }

    /** Create a default HTTP Context by independent API (Jakarta vs Javax) */
    public static HttpContext ofServlet(
            @Nullable final Object req,
            @NotNull final Object resp) {
        Reflections.setCharacterEncoding(resp, CHARSET.name());
        var writer = Reflections.getServletWriter(resp);
        var ureq = req != null ? URequest.ofRequest(req) : URequest.of();
        return new HttpContextImpl(ureq, writer);
    }
}