/*
 * Copyright 2020-2020 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.web.table;

import java.io.IOException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Sorted column
 * 
 * @author Pavel Ponec
 */
public class SortedColumn {
    private static final Pattern NUMBER = Pattern.compile("-?\\d+");
    
    private final boolean ascending;
    private final int index;

    public SortedColumn(@Nullable Boolean ascending, int orderColumn) {
        this.ascending = ascending != null && ascending;
        this.index = orderColumn;
    }

    public int getIndex() {
        return index;
    }

    public boolean isAscending() {
        return ascending;
    }

    @Override
    public String toString() {
        try {
            return toAppendable(new StringBuilder(4)).toString();
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
    }
    
    /** Write the content to an appendable text stream */
    public Appendable toAppendable(@Nonnull final Appendable writer) throws IOException {
        writer.append(ascending ? "" : "-");
        writer.append(String.valueOf(index));
        return writer;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SortedColumn other = (SortedColumn) obj;
        if (this.ascending != other.ascending) {
            return false;
        }
        if (this.index != other.index) {
            return false;
        }
        return true;
    }
    
    @Nullable
    public static SortedColumn of(@Nonnull final String value) {
        return NUMBER.matcher(value).matches()
            ? new SortedColumn(value.charAt(0) != '-', Math.abs(Integer.parseInt(value)))
            : null;
    }
    
}
