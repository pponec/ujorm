package org.ujorm.tools.web.request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

/** An implementation of the Jakarta HTTP Servlet request context. */
public class HttpContextImpl extends ExchangeContext {

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    protected HttpContextImpl(@NotNull URequest uRequest, @NotNull Appendable writer, @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        super(uRequest, writer);
        this.request = request;
        this.response = response;
    }

    /** Original HTTP Jakarta Servlet Request */
    @NotNull
    public HttpServletRequest getServletRequest() {
        return request;
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
        var result = getServletRequest().getContextPath();
        return result.isEmpty() ? "/" : (result + "/");
    }

    /**
     * Create a default HTTP Context by the Jakarta Servlet API
     */
    public static @NotNull HttpContextImpl of(
            @Nullable final HttpServletRequest request,
            @NotNull final HttpServletResponse response) {
        try {
            request.setCharacterEncoding(ExchangeContext.CHARSET);
            response.setCharacterEncoding(ExchangeContext.CHARSET);
            var map = manyMap(request.getParameterMap());
            var req = new URequestImpl(map, request.getReader());
            var writer = response.getWriter();
            return new HttpContextImpl(req, writer, request, response);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** * Converts a map of arrays to a map of lists using forEach */
    private static ManyMap manyMap(@NotNull Map<String, String[]> map) {
        var result = new ManyMap();
        map.forEach((key, values) ->  result.put(key, values));
        return result;
    }
}