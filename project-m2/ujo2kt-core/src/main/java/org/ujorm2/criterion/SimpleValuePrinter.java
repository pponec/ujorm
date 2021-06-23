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

package org.ujorm2.criterion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm2.Key;
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
    public SimpleValuePrinter(@Nonnull final StringBuilder out) {
        super(out);
    }

    /** Constructor */
    public SimpleValuePrinter(@Nonnull final String mark, @Nonnull final String textBorder, @Nonnull final StringBuilder out) {
        super(mark, textBorder, out);
    }

    /** Append value */
    @Nonnull
    public SimpleValuePrinter append(final char c) {
        out.append(c);
        return this;
    }

    /** Append value */
    @Nonnull
    public SimpleValuePrinter append(@Nullable final Object value) {
        out.append(value != null ? value.toString() : null);
        return this;
    }

    /** Append value */
    @Nonnull
    public SimpleValuePrinter append(@Nullable final CharSequence value) {
        out.append(value);
        return this;
    }

    /** Append value */
    @Nonnull
    public SimpleValuePrinter appendValue(@Nullable final Object value) {
             throw new UnsupportedOperationException("TODO");

    }

    /** Standarad writter */
    public StringBuilder getWriter() {
        return out;
    }

    /** Writer result */
    @Override @Nonnull
    public String toString() {
        return out.toString();
    }

}
