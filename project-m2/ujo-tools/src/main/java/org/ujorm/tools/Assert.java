/*
 *  Copyright 2017-2017 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.tools;

import java.util.Collection;
import javax.annotation.Nullable;
import static org.ujorm.tools.MsgFormatter.format;
import static org.ujorm.tools.MsgFormatter.format;
import static org.ujorm.tools.MsgFormatter.format;
import static org.ujorm.tools.MsgFormatter.format;

/**
 * Assertion utils, where all method can throw the {@code IllegalArgumentException} exception only.
 * For a message format see the {@link MsgFormatter#format(java.lang.Object...)} method description.
 * @author Pavel Ponec
 */
public abstract class Assert {

    /** Static methods are available only */
    private Assert() {
    }

    /** Checks if the argument is {@code true}. */
    public static final void isTrue(final boolean value) {
        isTrue(value, (Object[]) null);
    }

    /** Checks if the argument is {@code true}. */
    public static final void isTrue(final boolean value, Object... message) {
        if (!value) {
            throw new IllegalArgumentException(format(message), new NullPointerException());
        }
    }

    /** Checks if the argument is not {@code null}. */
    public static final void isNotNull(@Nullable final Object value) throws IllegalArgumentException {
        isNotNull(value, (Object[]) null);
    }

    /** Checks if the argument is not {@code null}. */
    public static void isNotNull(@Nullable final Object value, Object... message)
            throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(format(message), new NullPointerException());
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final byte... array)
            throws IllegalArgumentException {
        hasLength(array, (Object[]) null);
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final byte[] array, Object... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(final char... array)
            throws IllegalArgumentException {
        hasLength(array, (Object[]) null);
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final char[] array, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final Object... values)
            throws IllegalArgumentException {
        hasLength(values, (Object[]) null);
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final Object[] values, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final Collection<?> values)
            throws IllegalArgumentException {
        hasLength(values, (Object[]) null);
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final Collection<?> values, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (!Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void hasLength(@Nullable final CharSequence value)
            throws IllegalArgumentException {
        hasLength(value, (Object[]) null);
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
        isFalse(value, (Object[]) null);
    }

    /** Checks if the argument is {@code false}. */
    public static final void isFalse(final boolean value, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (value) {
            throw new IllegalArgumentException(format(message), new NullPointerException());
        }
    }

    /** Checks if the argument is {@code null}. */
    public static final void isNull(@Nullable final Object value) throws IllegalArgumentException {
        isNull(value, (Object[]) null);
    }

    /** Checks if the argument is {@code null}. */
    public static void isNull(@Nullable final Object value, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (value != null) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final byte... array)
            throws IllegalArgumentException {
        isEmpty(array, (Object[]) null);
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(final byte[] array, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final char... array)
            throws IllegalArgumentException {
        isEmpty(array, (Object[]) null);
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final char[] array, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (Check.hasLength(array)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final Object... values)
            throws IllegalArgumentException {
        isEmpty(values, (Object[]) null);
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final Object[] values, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final Collection<?> values)
            throws IllegalArgumentException {
        isEmpty(values, (Object[]) null);
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final Collection<?> values, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (Check.hasLength(values)) {
            throw new IllegalArgumentException(format(message));
        }
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final CharSequence value)
            throws IllegalArgumentException {
        isEmpty(value, (Object[]) null);
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static void isEmpty(@Nullable final CharSequence value, @Nullable final Object... message)
            throws IllegalArgumentException {
        if (Check.hasLength(value)) {
            throw new IllegalArgumentException(format(message));
        }
    }

}
