/*
 *  Copyright 2018-2018 Pavel Ponec
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

package org.ujorm.tools.msg;

import java.io.CharArrayWriter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Print values to an output
 * @author Pavel Ponec
 */
public class SimpleValuePrinter extends ValueFormatter {

    /** Writer */
    protected final CharArrayWriter out;

    /** Constructor */
    public SimpleValuePrinter(final int size) {
        this(new CharArrayWriter(size));
    }

    /** Constructor */
    public SimpleValuePrinter(@Nonnull final CharArrayWriter out) {
        this("?", "\"", out);
    }

    /** Constructor */
    public SimpleValuePrinter(@Nonnull final String mark, @Nonnull final String textBorder, @Nonnull final CharArrayWriter out) {
        super(mark, textBorder);
        this.out = out;
    }

    /** Append value */
    @Nonnull
    public SimpleValuePrinter appendValue(@Nullable final Object value) {
        if (value instanceof Object[]) {
            boolean first = true;
            for (Object object : (Object[]) value) {
                if (first) {
                    first = !first;
                } else {
                    out.append(", ");
                }
                appendValue(object);
            }
        } else {
           super.writeValue(value, out, true);
        }
        return this;
    }

    /** Standarad writter */
    public CharArrayWriter getWriter() {
        return out;
    }

    /** Writer result */
    @Override @Nonnull
    public String toString() {
        return out.toString();
    }

}
