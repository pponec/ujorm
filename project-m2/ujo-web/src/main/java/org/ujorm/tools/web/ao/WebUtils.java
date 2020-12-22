/*
 * Copyright 2020-2020 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.web.ao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import org.ujorm.tools.Check;

/**
 * Static method for common use
 *
 * @author Pavel Ponec
 */
public abstract class WebUtils {

        /** Check if any attribute is typeof the Renderer */
    public static final boolean isType(final Class type, final @Nonnull Object... items) {
        boolean result = false;
        for (Object item : items) {
            if (type.isInstance(item)) {
                result = true;
                break;
            }
        }
        return result;
    }

   /** Returns an URL of the servlet of the root where a default value is an empty string */
    @Nonnull
    public static String urlOfServlet(@Nonnull final Class<? extends HttpServlet> servlet, String... params) {
        final String result = urlOfServlet(servlet);
        if (Check.hasLength(params) && params[0] != null) {
            return result + "?" + String.join("&", params);
        }
        return result;
    }

    /**
     * Find a first URL from {@link WebServlet} annotation.
     * 
     * Returns an URL of the servlet of the root
     * @param servlet Servlet type of HttpServlet
     * @param defaultUrl A default result
     * @return A URL link.
     */
    @Nonnull
    public static String urlOfServlet(
            @Nonnull final Class<? extends HttpServlet> servlet,
            @Nonnull final String defaultUrl
    ) {
        final WebServlet[] webServlets = servlet.getAnnotationsByType(WebServlet.class);
        for (WebServlet webServlet : webServlets) {
            for (String link : webServlet.value()) {
                if (Check.hasLength(link)) {
                    return link;
                }
            }
        }
        return defaultUrl;
    }

    /** Returns a stream of lines form URL resource
     * 
     * @param url An URL link to a resource
     * @return The customer is responsible for closing the stream. 
     *         During closing, an IllegalStateException may occur due to an IOException.
     * @throws IOException 
     */
    public static Stream<String> rowsOfUrl(@Nonnull final URL url) throws IOException  {
        final InputStream is = url.openConnection().getInputStream();
        final Stream<String> result = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines();
        result.onClose(() -> {
            try {
                is.close();
            } catch (IOException e) {
                throw new IllegalStateException("Can't close: " + url, e);
            }
        });
        return result;
    }

}
