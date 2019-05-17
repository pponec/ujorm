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
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.ujorm.tools.msg.MsgFormatter.format;

/**
 * Assertion utils, where all method can throw the {@code IllegalArgumentException} exception only.
 * For a message format see the {@link MsgFormatter#format(T)} method description.
 * <h3>See the next correct asserts</h3>
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

    /** If the value Checks if the argument is {@code true}.
     * @throws IllegalStateException When the condtion is false */
    public static final  <M> void validState(final boolean condition, @Nullable final M... message)
            throws IllegalStateException {
        if (!condition) {
            throw new IllegalStateException(format(message), new NullPointerException());
        }
    }

    /** Checks if the argument is {@code true}. */
    public static final void isTrue(final boolean condition) throws IllegalArgumentException {
        Assert.isTrue(condition, NO_MESSAGE);
    }

    /** Checks if the argument is {@code true}. */
    public static final  <M> void isTrue(final boolean condition, @Nullable final M... message)
            throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(format(message), new NullPointerException());
        }
    }

    /** Checks if the value is not {@code null} and result of the the method
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html#test-T-">Predicate.test()</a> is {@code true}. */
    public static <V,M> void isTrue
        ( @Nullable final V condition
        , @Nonnull final Predicate<V> predicate
        , @Nullable final M... message)
    {
        if (condition == null || !predicate.test(condition)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the value is result of the the method
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html#test-T-">Predicate.test()</a> is {@code true}.
     * An argument of the {@code Predicable#test()} method can be {@code null}. */
    public static <V,M> void isTrueNullable
        ( @Nullable final V value
        , @Nonnull  final Predicate<V> predicate
        , @Nullable final M... message)
    {
        if (!predicate.test(value)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Return a result with <strong>presented value</strong> or throw an exception. */
    @Nonnull
    public static <V,M> Optional<V> getPresented(@Nullable final V value, @Nullable final M... message)
            throws IllegalArgumentException {
        return Optional.of(notNull(value, message));
    }

    /** Checks if the argument is not {@code null}. */
    @Nonnull
    public static final <V> V notNull(@Nullable final V value)
            throws IllegalArgumentException {
        return notNull(value, NO_MESSAGE);
    }

    /** Checks if the argument is not {@code null}. */
    @Nonnull
    public static <V,M> V notNull(@Nullable final V value, @Nullable final M... message)
            throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(format(message), new NullPointerException());
        }
        return value;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    @Nonnull
    public static <M> byte[] hasLength(@Nullable final byte[] array, @Nullable final M... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
        assert array != null; // A code for static analyzer only
        return array;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    @Nonnull
    public static <M> char[] hasLength(@Nullable final char[] array, @Nullable final M... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
        assert array != null; // A code for static analyzer only
        return array;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    @Nonnull
    public static <M> Object[] hasLength(@Nullable final Object[] array, @Nullable final M... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
        assert array != null; // A code for static analyzer only
        return array;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    @Nonnull
    public static <V,M> Collection<V> hasLength(@Nullable final Collection<V> value, @Nullable final M... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(value)) {
            throw new IllegalArgumentException(format(message));
        }
        assert value != null; // A code for static analyzer only
        return value;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    @Nonnull
    public static <K,V,M>  Map<K,V> hasLength(@Nullable final Map<K,V> value, @Nullable final M... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(value)) {
            throw new IllegalArgumentException(format(message));
        }
        assert value != null; // A code for static analyzer only
        return value;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    @Nonnull
    public static <M> CharSequence hasLength(@Nullable final CharSequence value, @Nullable final M... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(value)) {
            throw new IllegalArgumentException(format(message));
        }
        assert value != null; // A code for static analyzer only
        return value;
    }

    // ---- NEGATIONS ----
    /** Checks if the argument is {@code false}. */
    public static final void isFalse(final boolean condition)
            throws IllegalArgumentException {
        isFalse(condition, NO_MESSAGE);
    }

    /** Checks if the argument is {@code false}. */
    public static final void isFalse(final boolean condition, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (condition) {
            throw new IllegalArgumentException(format(message), new NullPointerException());
        }
    }

    /** Checks if the argument is not {@code null} and result of the the method
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html#test-T-">Predicate.test()</a> is {@code false}. */
    public static <V,M> void isFalse
        ( @Nullable final V value
        , @Nonnull  final Predicate<V> predicate
        , @Nullable final M... message)
    {
        if (value == null || predicate.test(value)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument of the the method
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html#test-T-">Predicate.test()</a> is {@code false}.
     * An argument of the {@code Predicable#test()} method can be {@code null}.
     */
    public static <V,M> void isFalseNullable
        ( @Nullable final V value
        , @Nonnull  final Predicate<V> predicate
        , @Nullable final M... message)
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
    public static <M> void isNull(@Nullable final Object value, @Nullable final M... message)
            throws IllegalArgumentException {
        if (value != null) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static <M> void isEmpty(final byte[] array, @Nullable final M... message)
            throws IllegalArgumentException {
        if (Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static <M> void isEmpty(@Nullable final char[] array, @Nullable final M... message)
            throws IllegalArgumentException {
        if (Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static <M> void isEmpty(@Nullable final Object[] values, @Nullable final M... message)
            throws IllegalArgumentException {
        if (Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static <M> void isEmpty(@Nullable final Collection<?> values, @Nullable final M... message)
            throws IllegalArgumentException {
        if (Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static <M> void isEmpty(@Nullable final Map<?,?> values, @Nullable final M... message)
            throws IllegalArgumentException {
        if (Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static <M> void isEmpty(@Nullable final CharSequence value, @Nullable final M... message)
            throws IllegalArgumentException {
        if (Check.hasLength(value)) {
            throw new IllegalArgumentException(format(message));
        }
    }

}
