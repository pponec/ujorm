package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class UContext {
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private final URequest servletRequest;
    private final Appendable servletResponse;

    public UContext(URequest servletRequest, Appendable servletResponse) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }

    public UContext(URequest servletRequest) {
        this(servletRequest, new StringBuilder());
    }

    public UContext() {
        this(URequestImpl.of(), new StringBuilder());
    }

    public URequest request() {
        return servletRequest;
    }

    public Appendable response() {
        return servletResponse;
    }

    /** HTTP Servlet Factory */
    public static UContext ofServlet(HttpServletResponse resp) {
        return ofServlet(null, resp);
    }

    /** HTTP Servlet Factory */
    public static UContext ofServlet(@Nullable HttpServletRequest req, @NotNull HttpServletResponse resp) {
        try {
            resp.setCharacterEncoding(CHARSET.name());
            final Appendable writer = resp.getWriter();
            final URequest ureq = req != null ? URequest.of(req) : URequestImpl.of();
            return new UContext(ureq, writer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** UContext from a map */
    public static UContext of(ManyMap map) {
        return new UContext(URequestImpl.ofMap(map), new StringBuilder());
    }
}
