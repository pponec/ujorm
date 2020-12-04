package org.ujorm.ujoservlet.ajax.ao;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

public interface HttpParam extends CharSequence {

    @Override
    default int length() {
        return toString().length();
    }

    @Override
    default char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    default CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }


    default String value(@Nonnull final HttpServletRequest request) {
        return value(request, "");
    }

    default String value(@Nonnull final HttpServletRequest request, @Nonnull final String defaultValue) {
        final String result = request.getParameter(toString());
        return result != null ? result : defaultValue;
    }

}
