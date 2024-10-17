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
        try {
            final Class<?> requestClass = httpServletRequest.getClass();
            final Method getReaderMethod = requestClass.getMethod("getReader");
            final Reader result = (Reader) getReaderMethod.invoke(httpServletRequest);
            return result;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Writer getServletWriter(Object httpServletResponse) {
        try {
            final Class<?> responseClass = httpServletResponse.getClass();
            final Method getWriterMethod = responseClass.getMethod("getWriter");
            final Writer result = (Writer) getWriterMethod.invoke(httpServletResponse);
            return result;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String[]> getParameterMap(Object httpServletRequest) {
        try {
            final Class<?> requestClass = httpServletRequest.getClass();
            final Method getParameterMapMethod = requestClass.getMethod("getParameterMap");
            final Map<String, String[]> result = (Map<String, String[]>)
                    getParameterMapMethod.invoke(httpServletRequest);
            return result;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setCharacterEncoding(@Nullable Object httpServletRequest, @NotNull String charset) {
        if (httpServletRequest != null) try {
            final Class<?> requestClass = httpServletRequest.getClass();
            final Method setCharsetEncoding = requestClass.getMethod("setCharacterEncoding", String.class);;
            setCharsetEncoding.invoke(httpServletRequest, charset);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
