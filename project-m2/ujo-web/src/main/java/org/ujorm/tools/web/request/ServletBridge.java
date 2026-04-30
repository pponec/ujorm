package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Reflection bridge for Servlet request/response implementations (Jakarta or javax). */
public final class ServletBridge {

    private static final Map<Class<?>, RequestMethods> REQUEST_METHODS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ResponseMethods> RESPONSE_METHODS = new ConcurrentHashMap<>();

    private ServletBridge() {
    }

    public static void setCharacterEncoding(@Nullable final Object servletObject, @NotNull final String charset) {
        if (servletObject == null) {
            return;
        }
        try {
            final var method = servletObject.getClass().getMethod("setCharacterEncoding", String.class);
            method.invoke(servletObject, charset);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Missing method setCharacterEncoding(String) on " + servletObject.getClass().getName(), e);
        }
    }

    public static void prepareHtmlResponse(
            @NotNull final Object httpServletResponse,
            @NotNull final Charset charset,
            final boolean noCache
    ) {
        try {
            final var methods = RESPONSE_METHODS.computeIfAbsent(httpServletResponse.getClass(), ResponseMethods::new);
            methods.setEncoding.invoke(httpServletResponse, charset.name());
            methods.setHeader.invoke(httpServletResponse, "Content-Type", "text/html; charset=" + charset.name());

            if (noCache) {
                methods.setHeader.invoke(httpServletResponse, "Cache-Control", "no-cache, no-store, must-revalidate");
                methods.setHeader.invoke(httpServletResponse, "Pragma", "no-cache");
                methods.setHeader.invoke(httpServletResponse, "Expires", "0");
                methods.setHeader.invoke(httpServletResponse, "X-UA-Compatible", "IE=edge");
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to configure servlet response: " + httpServletResponse.getClass().getName(), e);
        }
    }

    public static Writer getServletWriter(@NotNull final Object httpServletResponse) {
        try {
            final var methods = RESPONSE_METHODS.computeIfAbsent(httpServletResponse.getClass(), ResponseMethods::new);
            return (Writer) methods.getWriter.invoke(httpServletResponse);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Missing method getWriter() on " + httpServletResponse.getClass().getName(), e);
        }
    }

    public static void sendRedirect(@NotNull final Object httpServletResponse, @NotNull final String location) {
        try {
            final var methods = RESPONSE_METHODS.computeIfAbsent(httpServletResponse.getClass(), ResponseMethods::new);
            methods.sendRedirect.invoke(httpServletResponse, location);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Missing method sendRedirect(String) on " + httpServletResponse.getClass().getName(), e);
        }
    }

    public static Reader getServletReader(@NotNull final Object httpServletRequest) {
        try {
            final var methods = REQUEST_METHODS.computeIfAbsent(httpServletRequest.getClass(), RequestMethods::new);
            return (Reader) methods.getReader.invoke(httpServletRequest);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Missing method getReader() on " + httpServletRequest.getClass().getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String[]> getParameterMap(@Nullable final Object httpServletRequest) {
        if (httpServletRequest == null) {
            return Collections.emptyMap();
        }
        try {
            final var methods = REQUEST_METHODS.computeIfAbsent(httpServletRequest.getClass(), RequestMethods::new);
            return (Map<String, String[]>) methods.getParameterMap.invoke(httpServletRequest);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Missing method getParameterMap() on " + httpServletRequest.getClass().getName(), e);
        }
    }

    public static String getContextPath(@NotNull final Object httpServletRequest) {
        try {
            final var methods = REQUEST_METHODS.computeIfAbsent(httpServletRequest.getClass(), RequestMethods::new);
            return (String) methods.getContextPath.invoke(httpServletRequest);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Missing method getContextPath() on " + httpServletRequest.getClass().getName(), e);
        }
    }

    private static final class RequestMethods {
        final Method getReader;
        final Method getParameterMap;
        final Method getContextPath;

        RequestMethods(final Class<?> clazz) {
            try {
                this.getReader = clazz.getMethod("getReader");
                this.getParameterMap = clazz.getMethod("getParameterMap");
                this.getContextPath = clazz.getMethod("getContextPath");
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Failed to initialize request methods for " + clazz.getName(), e);
            }
        }
    }

    private static final class ResponseMethods {
        final Method setEncoding;
        final Method setHeader;
        final Method getWriter;
        final Method sendRedirect;

        ResponseMethods(final Class<?> clazz) {
            try {
                this.setEncoding = clazz.getMethod("setCharacterEncoding", String.class);
                this.setHeader = clazz.getMethod("setHeader", String.class, String.class);
                this.getWriter = clazz.getMethod("getWriter");
                this.sendRedirect = clazz.getMethod("sendRedirect", String.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Failed to initialize response methods for " + clazz.getName(), e);
            }
        }
    }
}
