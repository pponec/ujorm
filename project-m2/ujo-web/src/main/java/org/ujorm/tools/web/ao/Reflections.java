package org.ujorm.tools.web.ao;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/** Reflection methods for the Servlet Request & Response classes */
public final class Reflections {

    public static Reader getServletReader(Object httpServletRequest) {
        return (Reader) getAttribute(httpServletRequest, "getReader");
    }

    public static Writer getServletWriter(Object httpServletResponse) {
        return (Writer) getAttribute(httpServletResponse,  "getWriter");
    }

    public static Map<String, String[]> getParameterMap(Object httpServletRequest) {
        return (Map<String, String[]>) getAttribute(httpServletRequest,  "getParameterMap");
    }

    public static void setCharacterEncoding(
            @Nullable Object httpServletRequest,
            @NotNull String charset) {
        final String methodName = "setCharacterEncoding";
        if (httpServletRequest != null) try {
            final Class<?> requestClass = httpServletRequest.getClass();
            final Method setCharsetEncoding = requestClass.getMethod(methodName, String.class);;
            setCharsetEncoding.invoke(httpServletRequest, charset);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            final String msg = String.format("Method does not exists: %s.%s()",
                    httpServletRequest.getClass().getSimpleName(), methodName);
            throw new RuntimeException(msg, e);
        }
    }

    private static Object getAttribute(Object servletRequest, String methodName) {
        try {
            final Method getReaderMethod = servletRequest.getClass().getMethod(methodName);
            return getReaderMethod.invoke(servletRequest);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            final String msg = String.format("Method does not exists: %s.%s()",
                    servletRequest.getClass().getSimpleName(), methodName);
            throw new RuntimeException(msg, e);
        }
    }
}
