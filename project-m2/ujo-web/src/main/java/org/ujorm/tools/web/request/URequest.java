package org.ujorm.tools.web.request;

import org.jetbrains.annotations.NotNull;
import java.io.Reader;

public interface URequest {

    /** Request Reader */
    @NotNull
    Reader getReader();

    /** Parametr provider */
    @NotNull
    String[] getParameterValues(final String key);

}
