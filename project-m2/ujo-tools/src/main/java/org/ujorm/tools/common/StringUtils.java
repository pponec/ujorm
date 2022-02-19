/*
 * Copyright 2021-2022 Pavel Ponec
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Assert;

/**
 * Methods to reading an text resource to the {@code String}.
 *
 * <h3>Usage</h3>
 * <pre class="pre">
 * try (Stream&lt;String&gt; stream = StringUtils.readLines(StringUtils.class,  "text", "dummy.txt")) {
 *     String[] result = stream.toArray(String[]::new);
 *     assertEquals("abc", result[0]);
 * }
 * </pre>
 *
 * @author Pavel Ponec
 */
public class StringUtils {

     /** File separator (one character is required) */
     public static final String SEPARATOR = "/";

    /** A messge template: "Resource is not available: {}"  */
    private static final String NO_RESOURCE_MSG = "Resource is not available: ";

    /** Charset of the resource */
    @NotNull
    private final Charset charset;

    /** Class loader */
    @NotNull
    private final Class<?> classOfLoader;

    /** With a charset UTF-8 */
    public StringUtils() {
        this(StandardCharsets.UTF_8);
    }

    public StringUtils(@NotNull final Charset charset) {
        this(charset, StringUtils.class);
    }

    public StringUtils(@NotNull final Charset charset, @NotNull final Class<?> classOfLoader) {
        this.charset = Assert.notNull(charset, "charset");
        this.classOfLoader = Assert.notNull(classOfLoader, "classOfLoader");
    }

    /** Read a content of the resource encoded by UTF-8.
     * A line separator can be modifed in the result.
     */
    @NotNull
    public String readBody(@NotNull final String... resource) {
        return readBody(null, resource);
    }

    /** Read a content of the resource encoded by UTF-8.
     * A line separator can be modifed in the result.
     */
    @NotNull
    public String readBody(@NotNull final Class<?> basePackage, @NotNull final String... resourcePath) {
        final String resource = buildResource(basePackage, resourcePath);
        try (InputStream is = classOfLoader.getResourceAsStream(resource)) {
            return readBody(is);
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException(NO_RESOURCE_MSG + resource, e);
        }
    }

    /** Read a content of the resource encoded by UTF-8.
     * A line separator can be modifed in the result.
     */
    @NotNull
    public String readBody(@NotNull final InputStream is) {
        return new BufferedReader(new InputStreamReader(is, charset))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    /** Read a content of the resource encoded by UTF-8.
     * A line separator can be modifed in the result.
     * @return The result must be closed.
     */
    @NotNull
    public Stream<String> readRows(@Nullable final Class<?> basePackage, @NotNull final String... resourcePath) {
        final String resource = buildResource(basePackage, resourcePath);
        try {
            return readRows(classOfLoader.getResource(resource));
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException(NO_RESOURCE_MSG + resource, e);
        }
    }

    /** Read a closeable line stream of the URL.
     * A line separator can be modifed in the result.
     * @return The result must be closed.
     */
    @NotNull
    public Stream<String> readRows(@NotNull final URL url) throws IOException {
        final InputStream is = url.openStream();
        if (is == null) {
            throw new IllegalStateException("Can't open: " + url);
        } else return readRows(is).onClose(()-> {
            try {
                is.close();
            } catch (IOException e) {
                throw new IllegalStateException("Can't close: " + url, e);
            }
        });
    }

    /** Read a closeable line stream of the URL.
     * A line separator can be modifed in the result
     * @return The result must be closed.
     */
    @NotNull
    public Stream<String> readRows(@NotNull final InputStream is) throws IOException {
        return new BufferedReader(new InputStreamReader(is, charset)).lines();
    }

    /** Build a resource */
    protected String buildResource(@Nullable final Class<?> basePackage, @NotNull final String... resourcePath) {
        final String endPath = resourcePath.length == 1 ? resourcePath[0] : String.join(SEPARATOR, resourcePath);
        return endPath.startsWith(SEPARATOR)
             ? endPath
             : String.join(SEPARATOR, "",
                    basePackage.getPackage().getName().replace('.', SEPARATOR.charAt(0)),
                    endPath);

    }

    // --- STATIC METHODS ---

    /** Read a content of the resource encoded by UTF-8.
     * A line separator can be modifed in the result.
     */
    @NotNull
    public static String read(@NotNull final Class<?> basePackage, @NotNull final String... resourcePath) {
        return new StringUtils(StandardCharsets.UTF_8, basePackage).readBody(basePackage, resourcePath);
    }

    /** Read a content of the resource encoded by UTF-8.
     * A line separator can be modifed in the result.
     */
    @NotNull
    public static String read(@NotNull final InputStream is) {
        return new StringUtils().readBody(is);
    }

    /** Read a closeable line stream of the URL.
     * A line separator can be modifed in the result.
     * @return The result must be closed.
     */
    @NotNull
    public static Stream<String> readLines(@NotNull final URL url) throws IOException {
        return new StringUtils().readRows(url);
    }

    /** Read a content of the resource encoded by UTF-8.
     * A line separator can be modifed in the result.
     * @return The result must be closed.
     */
    @NotNull
    public static Stream<String> readLines(@Nullable final Class<?> basePackage, @NotNull final String... resourcePath) {
        return new StringUtils(StandardCharsets.UTF_8, basePackage).readRows(basePackage, resourcePath);
    }

    /** Read a content of the resource encoded by UTF-8.
     * A line separator can be modifed in the result.
     * @return The result must be closed.
     */
    @NotNull
    public static Stream<String> readLines(@NotNull final String... resourcePath) {
        return readLines(StringUtils.class, resourcePath);
    }

}
