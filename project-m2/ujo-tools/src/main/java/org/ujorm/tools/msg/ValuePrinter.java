/*
 *  Copyright 2018-2026 Pavel Ponec
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

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Print values to an output
 * @author Pavel Ponec
 */
public class ValuePrinter extends ValueFormatter {

    /** Writer */
    protected final Appendable out;

    /** Constructor */
    public ValuePrinter(final int size) {
        this(new StringBuilder(size));
    }

    /** Constructor */
    public ValuePrinter(@NotNull final Appendable out) {
        this("?", "\"", out);
    }

    /** Constructor */
    public ValuePrinter(@NotNull final String mark, @NotNull final String textBorder, @NotNull final Appendable out) {
        super(mark, textBorder);
        this.out = out;
    }

    /** Append value */
    @NotNull
    public ValuePrinter appendValue(@Nullable final Object value) throws IOException {
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
    public Appendable getWriter() {
        return out;
    }

    /** Writer result */
    @Override @NotNull
    public String toString() {
        return out.toString();
    }

}
