/*
 *  Copyright 2017-2018 Pavel Ponec
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

package org.ujorm.criterion;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.tools.msg.ValuePrinter;

/**
 * Print values to an output
 * @author Pavel Ponec
 */
public class SimpleValuePrinter extends ValuePrinter {

    /** Constructor */
    public SimpleValuePrinter(int size) {
        super(size);
    }

    /** Constructor */
    public SimpleValuePrinter(@Nonnull final Appendable out) {
        super(out);
    }

    /** Constructor */
    public SimpleValuePrinter(@Nonnull final String mark, @Nonnull final String textBorder, @Nonnull final Appendable out) {
        super(mark, textBorder, out);
    }

    /** Append value */
    @Nonnull
    public SimpleValuePrinter append(final char c) {
        try {
            out.append(c);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    /** Append value */
    @Nonnull
    public SimpleValuePrinter append(@Nullable final Object value) {
        try {
            out.append(value != null ? value.toString() : null);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    /** Append value */
    @Nonnull
    public SimpleValuePrinter append(@Nullable final CharSequence value) {
        try {
            out.append(value);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    /** Append value */
    @Nonnull
    public SimpleValuePrinter appendValue(@Nullable final Object value) {
        try {
            if (value == null) {
               out.append((String) null);
            } else if (value instanceof Key) {
                out.append((Key) value);
            } else if (value instanceof Ujo) {
                final Ujo ujo = (Ujo) value;
                final Key firstProperty = ujo.readKeys().get(0);
                final Object firstValue = firstProperty.of(ujo);

                out.append(ujo.getClass().getSimpleName());
                out.append('[');
                out.append(firstProperty);
                out.append('=');
                appendValue(firstValue);
                out.append(']');
            } else {
                super.writeValue(value, out, true);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    /** Writer result */
    @Override @Nonnull
    public String toString() {
        return out.toString();
    }

}
