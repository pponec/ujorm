package org.ujorm.tools.web.request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.xml.config.XmlConfig;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;

/** Jakarta Servlet HTTP request context. */
public class HttpContext extends ExchangeContext {

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    protected HttpContext(
            @NotNull URequest uRequest,
            @NotNull Appendable writer,
            @Nullable HttpServletRequest request,
            @NotNull HttpServletResponse response
    ) {
        super(uRequest, writer);
        this.request = request;
        this.response = response;
    }

    /** Original HTTP Jakarta Servlet Request */
    @NotNull
    public Optional<HttpServletRequest> getServletRequest() {
        return Optional.ofNullable(request);
    }

    /** Original HTTP Jakarta Servlet Response */
    @NotNull
    public HttpServletResponse getServletResponse() {
        return response;
    }

    /** @see HttpServletResponse#sendRedirect(String)  */
    public void sendRedirect(@NotNull String location) throws IOException {
        getServletResponse().sendRedirect(location);
    }

    /** Returns the context path with a trailing slash */
    public String getPathSlash() {
        var result = getServletRequest().map(HttpServletRequest::getContextPath).orElseThrow();
        return result.isEmpty() ? "/" : (result + "/");
    }

    /**
     * Create a default HTTP Context by the Jakarta Servlet API.
     */
    public static @NotNull HttpContext of(
            @NotNull final HttpServletRequest request,
            @NotNull final HttpServletResponse response
    ) {
        return of(request, response, XmlConfig.ofDefault());
    }

    /**
     * Create a default HTTP Context by the Jakarta Servlet API.
     * @deprecated Use the method {@link #of(HttpServletRequest, HttpServletResponse)}
     */
    @Deprecated
    public static @NotNull HttpContext ofServlet(
            @NotNull final HttpServletRequest request,
            @NotNull final HttpServletResponse response
    ) {
        return of(request, response, XmlConfig.ofDefault());
    }

    /**
     * Create a default HTTP Context by the Jakarta Servlet API.
     */
    public static @NotNull HttpContext of(
            @Nullable final HttpServletRequest request,
            @NotNull final HttpServletResponse response,
            @NotNull final XmlConfig config
    ) {
        try {
            final var charset = config.getCharset();
            if (request != null) request.setCharacterEncoding(charset.name());
            ServletBridge.prepareHtmlResponse(response, charset, true);
            var map = request != null ? manyMap(request.getParameterMap()) : manyMap(Map.of());
            var req = new URequestImpl(map, request != null ? request.getReader() : new StringReader(""));
            var writer = response.getWriter();
            return new HttpContext(req, writer, request, response);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Create a default HTTP Context by the Jakarta Servlet API.
     */
    public static @NotNull HttpContext of(@NotNull final HttpServletResponse response) {
        return of(null, response);
    }

    /** * Converts a map of arrays to a map of lists using forEach */
    private static ManyMap manyMap(@NotNull Map<String, String[]> map) {
        var result = new ManyMap();
        map.forEach((key, values) ->  result.put(key, values));
        return result;
    }
}