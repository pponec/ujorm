/*
 * Copyright 2019-2022 Pavel Ponec
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

package tools.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Optional;
import java.util.function.Function;

/**
 * Static methods
 * @author Pavel Ponec
 */
public abstract class ObjectUtils {

    private ObjectUtils() {
    }

    /** Run a function in case the value is an instance of the required class.
     *
     * <br>Usage:
     * <pre class="pre">
     *   Object input = "ABC";
     *   int result = ObjectUtils.iof(input, String.class, v -> v.length()).orElse(0);
     *   assertEquals(3, result);
     * </pre>
     */
    public static final <V,R> Optional<R> iof(
            @Nullable final Object value,
            @NotNull final Class<V> requiredClass,
            @NotNull final Function<V,R> function) {
        return requiredClass.isInstance(value)
                ? Optional.ofNullable(function.apply((V) value))
                : Optional.empty();
    }

    /** Check the result of a function in case the value have got the same class as the required one.
     *
     * <br>Usage:
     * <pre class="pre">
     *   Object input = "ABC";
     *   int result = ObjectUtils.check(input, String.class, v -> v.length()).orElse(0);
     *   assertEquals(3, result);
     * </pre>
     */
    public static final <V> boolean check(
            @Nullable final Object value,
            @NotNull final Class<V> requiredClass,
            @NotNull final Function<V,Boolean> function) {
        return value != null
                && value.getClass() == requiredClass
                && function.apply((V) value);
    }

    /** Convert appendable to object type of PrintWriter */
    @NotNull
    public static PrintWriter toPrintWriter(@NotNull final Appendable appendable) {
        final Writer myWriter = new Writer() {
            @Override
            public void flush() throws IOException {}
            @Override
            public void close() throws IOException {}
            @Override
            public void write(final char[] cbuf, final int off, final int len) throws IOException {
                for (int i = 0, max = off + len; i < max; i++) {
                    appendable.append(cbuf[i]);
                }
            }
        };
        return new PrintWriter(myWriter, false);
    }

}
