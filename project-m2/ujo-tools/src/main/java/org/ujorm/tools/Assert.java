/*
 * Copyright 2017-2026 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Assertion utils, where all methods can throw the {@code IllegalArgumentException} or {@code IllegalStateException}.
 * The error message is provided by a {@link Supplier<String>}, which allows lazy evaluation.
 * <h4>Examples</h4>
 * <pre class="pre">
 *   Assert.isTrue(true, () -> "Error message");
 *   Assert.isTrue(true, () -> format("TEST:%s%s".formatted("A", "B")));
 *   Assert.isTrue(30, (x) -> x > 20, () -> "Wrong No");
 *   Assert.notNull("ABC", null);
 *   Assert.hasLength("ABC", null);
 *   Assert.hasLength(new char[]{'A','B','C'}, null);
 *   Assert.hasLength(new StringBuilder().append("ABC"), null);
 *   Assert.hasLength(Arrays.asList("A", "B", "C"), null);
 *
 *   Assert.isFalse(false, null);
 *   Assert.isFalse(15, (x) -> x > 20, null);
 *   Assert.isNull(null, null);
 *   Assert.isEmpty("", null);
 *   Assert.isEmpty(new char[0], null);
 *   Assert.isEmpty(new StringBuilder(), null);
 *   Assert.isEmpty((List&lt;?&gt;) null, null);
 *
 *   Assert.isTrue(true, () -> format("TEST:%s%s".formatted("A", "B")));
 *   Assert.isTrue(true, () -> String.format("TEST:%s%s".formatted("A", "B")));
 * </pre>
 *
 * @see <a href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/Assert.html">Spring Assert</a>
 * @see <a href="https://commons.apache.org/proper/commons-lang/javadocs/api-3.1/org/apache/commons/lang3/Validate.html">Apache Commons Validate</a>
 * @see <a href="https://google.github.io/guava/releases/19.0/api/docs/com/google/common/base/Preconditions.html">Guava Preconditions</a>
 * @author Pavel Ponec
 * @since 1.73
 * @author Pavel Ponec
 * @since 1.73
 */
public abstract class Assert {

    /** Static methods are available only */
    private Assert() {
    }

    /**
     * Checks if the argument is {@code true}.
     * @throws IllegalStateException When the condition is false
     */
    public static void state(final boolean condition, @Nullable final Supplier<String> message)
            throws IllegalStateException {
        if (!condition) {
            throw new IllegalStateException(getMessage(message));
        }
    }

    /**
     * Checks if the argument is not {@code null}.
     * @return The original value
     */
    @NotNull
    public static <V> V notNullState(@Nullable final V value, @Nullable final Supplier<String> message)
            throws IllegalStateException {
        if (value == null) {
            throw new IllegalStateException(getMessage(message));
        }
        return value;
    }

    /** Checks if the argument is {@code true}. */
    public static void isTrue(final boolean condition, @Nullable final Supplier<String> message)
            throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(getMessage(message));
        }
    }

    /** Checks if the value is not {@code null} and the predicate is valid. */
    public static <V> void isTrueRequired(
            @Nullable final V condition,
            @NotNull final Predicate<V> predicate,
            @Nullable final Supplier<String> message) {
        if (condition == null || !predicate.test(condition)) {
            throw new IllegalArgumentException(getMessage(message));
        }
    }

    /** Checks if the predicate is valid. */
    public static <V> void isTrue(
            @Nullable final V value,
            @NotNull final Predicate<V> predicate,
            @Nullable final Supplier<String> message) {
        if (!predicate.test(value)) {
            throw new IllegalArgumentException(getMessage(message));
        }
    }

    /**
     * Return a result with <strong>presented value</strong>.
     * @return An {@code Optional} object with the original value
     */
    @NotNull
    public static <V> Optional<V> isPresented(@Nullable final V value, @Nullable final Supplier<String> message) {
        return Optional.ofNullable(value);
    }

    /**
     * Checks if the argument is not {@code null}.
     * @return The original value
     */
    @NotNull
    public static <V> V notNull(@Nullable final V value, @Nullable final Supplier<String> message)
            throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(getMessage(message));
        }
        return value;
    }


    /**
     * Checks if the argument is not {@code null}.
     * @return The original value
     * @deprecated Use the method {@link Objects#requireNonNull(Object, String)} rather.
     */
    @Deprecated
    @NotNull
    public static <V> V notNull(@Nullable final V value, String message) {
        return Objects.requireNonNull(value, message);
    }

    /**
     * Checks if the value of a supplier is not {@code null}.
     * @return The original value
     */
    @NotNull
    public static <V> V notNullValue(
            @NotNull final Supplier<V> supplier,
            @Nullable final Supplier<String> message)
            throws IllegalArgumentException {
        try {
            return notNull(supplier.get(), message);
        } catch (Exception e) {
            throw new IllegalArgumentException(getMessage(message), e);
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    @NotNull
    public static byte[] hasLength(@Nullable final byte[] array, @Nullable final Supplier<String> message) {
        if (!Check.hasLength(array)) {
            throw new IllegalArgumentException(getMessage(message));
        }
        return array;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    @NotNull
    public static char[] hasLength(@Nullable final char[] array, @Nullable final Supplier<String> message) {
        if (!Check.hasLength(array)) {
            throw new IllegalArgumentException(getMessage(message));
        }
        return array;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    @NotNull
    public static <V> V[] hasLength(@Nullable final V[] array, @Nullable final Supplier<String> message) {
        if (!Check.hasLength(array)) {
            throw new IllegalArgumentException(getMessage(message));
        }
        return array;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    @NotNull
    public static <V> Collection<V> hasLength(@Nullable final Collection<V> value, @Nullable final Supplier<String> message) {
        if (!Check.hasLength(value)) {
            throw new IllegalArgumentException(getMessage(message));
        }
        return value;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    @NotNull
    public static <V, K> Map<K, V> hasLength(@Nullable final Map<K, V> value, @Nullable final Supplier<String> message) {
        if (!Check.hasLength(value)) {
            throw new IllegalArgumentException(getMessage(message));
        }
        return value;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    @NotNull
    public static <V extends CharSequence> V hasLength(@Nullable final V value, @Nullable final Supplier<String> message) {
        if (!Check.hasLength(value)) {
            throw new IllegalArgumentException(getMessage(message));
        }
        return value;
    }

    /** Checks if the argument is {@code false}. */
    public static void isFalse(final boolean condition, @Nullable final Supplier<String> message) {
        if (condition) {
            throw new IllegalArgumentException(getMessage(message));
        }
    }

    /** Checks if the argument is {@code null}. */
    public static void isNull(@Nullable final Object value, @Nullable final Supplier<String> message) {
        if (value != null) {
            throw new IllegalArgumentException(getMessage(message));
        }
    }

    /** Checks if the argument is empty or {@code null}. */
    public static void isEmpty(@Nullable final Collection<?> values, @Nullable final Supplier<String> message) {
        if (Check.hasLength(values)) {
            throw new IllegalArgumentException(getMessage(message));
        }
    }

    /** Checks if the argument is empty or {@code null}. */
    public static void isEmpty(@Nullable final CharSequence value, @Nullable final Supplier<String> message) {
        if (Check.hasLength(value)) {
            throw new IllegalArgumentException(getMessage(message));
        }
    }


    /** Evaluation of the message supplier with a safety net */
    private static String getMessage(@Nullable final Supplier<String> message) {
        try {
            return message != null ? message.get() : null;
        } catch (Exception e) {
            return "Message evaluation failed: " + e.getMessage();
        }
    }
}