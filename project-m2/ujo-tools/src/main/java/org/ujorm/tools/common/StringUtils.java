/*
 * Copyright 2021-2021 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/JdbcBuilder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.ujorm.tools.Assert;

/**
 *
 * @author Pavel Ponec
 */
public abstract class StringUtils {

    private StringUtils() {
    }

    /** Read a content of the resource encoded by UTF-8.
     * A line separator can be modifed in the result.
     */
    @Nonnull
    public static String read(@Nonnull final String resource) {
        Assert.hasLength(resource, "Resource is not available: {}", resource);
        try (InputStream is = StringUtils.class.getResourceAsStream(resource)) {
            return read(is);
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Reading the resource failed: " + resource, e);
        }
    }

    /** Read a content of the resource encoded by UTF-8.
     * A line separator can be modifed in the result.
     */
    @Nonnull
    public static String read(InputStream is) {
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    /**
     * Read a closeable line stream of the URL.
     * A line separator can be modifed in the result.
     */
    @Nonnull
    public static Stream<String> readLines(@Nonnull final URL url) throws IOException {
        final InputStream is = url.openStream();
        Assert.notNull(is, "Resource is not available: {}", url);
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().onClose(() -> {
            try {
                is.close();
            } catch (IOException e) {
                throw new IllegalStateException("Can't close: " + url, e);
            }
        });
    }

}
