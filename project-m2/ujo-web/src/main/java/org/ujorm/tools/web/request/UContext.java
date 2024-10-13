package org.ujorm.tools.web.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class UContext {

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
            final Charset charset = StandardCharsets.UTF_8;
            if (req != null) {
                req.setCharacterEncoding(charset.name());
            }
            final Reader reader = req != null  ? req.getReader() : new CharArrayReader(new char[0]);
            final PrintWriter writer = resp.getWriter();

            resp.setCharacterEncoding(charset.name());
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setHeader("Expires", "0");

            final URequest ureq = new URequest() {
                @Override
                public Reader getReader() {
                    return reader;
                }

                @Override
                public String[] getParameterValues(String key) {
                    if (req != null) {
                        final String[] result = req.getParameterValues(key);
                        return result != null ? result : URequestImpl.emptyTexts;

                    } else {
                        return URequestImpl.emptyTexts;
                    }
                }
            };
            return new UContext(ureq, writer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }
}
