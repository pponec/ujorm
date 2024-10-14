package org.ujorm.tools.web.request;

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
        this(new URequestImpl(), new StringBuilder());
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
    public static UContext ofServlet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            resp.setCharacterEncoding(CHARSET.name());
            final PrintWriter writer = resp.getWriter();
            final URequest ureq = req != null ? URequest.of(req) : new URequestImpl();
            return new UContext(ureq, writer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }
}
