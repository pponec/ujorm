/*
 * Copyright 2017-2017 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.ujorm.tools.MsgFormatter.format;

/**
 * Assertion utils, where all method can throw the {@code IllegalArgumentException} exception only.
 * For a message format see the {@link MsgFormatter#format(java.lang.Object...)} method description.
 * See the next correct asserts:
 * <pre class="pre">
 *  Assert.isTrue(true, "TEST:{}{}", "A", "B");
 *  Assert.isTrue(30, (x) -> x > 20, "Wrong No");
 *  Assert.notNull("ABC");
 *  Assert.hasLength("ABC");
 *  Assert.hasLength(new char[]{'A','B','C'});
 *  Assert.hasLength(new StringBuilder().append("ABC"));
 *  Assert.hasLength(Arrays.asList("A", "B", "C"));
 *
 *  Assert.isFalse(false);
 *  Assert.isFalse(15, (x) -> x > 20);
 *  Assert.isNull (null);
 *  Assert.isEmpty("");
 *  Assert.isEmpty(new char[0]);
 *  Assert.isEmpty(new StringBuilder());
 *  Assert.isEmpty((List) null);
 * </pre>
 * @see https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/Assert.html
 * @see https://commons.apache.org/proper/commons-lang/javadocs/api-3.1/org/apache/commons/lang3/Validate.html
 * @see https://google.github.io/guava/releases/19.0/api/docs/com/google/common/base/Preconditions.html
 * @author Pavel Ponec
 * @since 1.73
 */
public abstract class Assert {

    /** No messge constant */
    public static final Object[] NO_MESSAGE = null;

    /** Static methods are available only */
    private Assert() {
    }

    /** Checks if the argument is {@code true}. */
    public static final void isTrue(final boolean value) throws IllegalArgumentException {
        Assert.isTrue(value, NO_MESSAGE);
    }

    /** Checks if the argument is {@code true}. */
    public static final void isTrue(final boolean value, final Object... message)
            throws IllegalArgumentException {
        if (!value) {
            throw new IllegalArgumentException(format(message), new NullPointerException());
        }
    }

    /** Checks if the value is not {@code null} and result of the the method
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html#test-T-">Predicate.test()</a> is {@code true}. */
    public static <T> void isTrue
        ( @Nullable final T value
        , @Nonnull final Predicate<T> predicate
        , final Object... message)
    {
        if (value == null || !predicate.test(value)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the value is result of the the method
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html#test-T-">Predicate.test()</a> is {@code true}. */
    public static <T> void isTrueCommon
        ( @Nullable final T value
        , @Nonnull final Predicate<T> predicate
        , final Object... message)
    {
        if (!predicate.test(value)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not {@code null}. */
    public static final void notNull(@Nullable final Object value)
            throws IllegalArgumentException {
        notNull(value, NO_MESSAGE);
    }

    /** Checks if the argument is not {@code null}. */
    public static void notNull(@Nullable final Object value, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(format(message), new NullPointerException());
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final byte[] array, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final char[] array, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final Object[] values, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final Collection<?> values, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final Map<?,?> values, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final CharSequence value, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(value)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    // ---- NEGATIONS ----
    /** Checks if the argument is {@code false}. */
    public static final void isFalse(final boolean value)
            throws IllegalArgumentException {
        isFalse(value, NO_MESSAGE);
    }

    /** Checks if the argument is {@code false}. */
    public static final void isFalse(final boolean value, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (value) {
            throw new IllegalArgumentException(format(message), new NullPointerException());
        }
    }

    /** Checks if the argument is not {@code null} and result of the the method
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html#test-T-">Predicate.test()</a> is {@code false}. */
    public static <T> void isFalse
        ( @Nullable final T value
        , @Nonnull final Predicate<T> predicate
        , final Object... message)
    {
        if (value == null || predicate.test(value)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument of the the method
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html#test-T-">Predicate.test()</a> is {@code false}. */
    public static <T> void isFalseCommon
        ( @Nullable final T value
        , @Nonnull final Predicate<T> predicate
        , final Object... message)
    {
        if (predicate.test(value)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is {@code null}. */
    public static final void isNull(@Nullable final Object value) throws IllegalArgumentException {
        isNull(value, NO_MESSAGE);
    }

    /** Checks if the argument is {@code null}. */
    public static void isNull(@Nullable final Object value, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (value != null) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(final byte[] array, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final char[] array, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final Object[] values, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final Collection<?> values, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final Map<?,?> values, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final CharSequence value, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (Check.hasLength(value)) {
            throw new IllegalArgumentException(format(message));
        }
    }

}
