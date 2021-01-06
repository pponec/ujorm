/*
 * Copyright 2021-2021 Pavel Ponec, https://github.com/pponec
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
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.msg.MsgFormatter;
import org.ujorm.tools.web.ao.HttpParameter;

/**
 * Table column model
 *
 * @author Pavel Ponec
 */
public class ColumnModel<D, V> {
    
    private static final NullPointerException x = null;
    
    /** Number pattern */ 
    private static final Pattern NUMBER = Pattern.compile("-?\\d+");

    private final int index;
    @Nonnull
    private final Function<D, V> column;
    @Nonnull
    private final CharSequence title;
    @Nullable
    private final HttpParameter param;
    /** Is the column sortable? */
    private boolean sortable = false;
    @Nonnull
    private Direction ascending = Direction.BOTH;
    
    public ColumnModel(@Nonnull Direction ascending, int index) {
        this(index, x -> null, "", null);
        setAscending(ascending);
    }

    public ColumnModel(final int index, @Nonnull final Function<D, V> column, @Nonnull final CharSequence title, @Nonnull final HttpParameter param) {
        this.index = index;
        this.column = Assert.notNull(column, "column");;
        this.title = Assert.notNull(title, "title");
        this.param = param;
    }

    public int getIndex() {
        return index;
    }

    @Nonnull
    public Function<D, V> getColumn() {
        return column;
    }

    @Nonnull
    public CharSequence getTitle() {
        return title;
    }

    @Nullable
    public HttpParameter getParam() {
        return param;
    }

    public boolean isSortable() {
        return sortable;
    }

    @Nullable
    public Direction getAscending() {
        return ascending;
    }

    public boolean isFiltered() {
        return param != null;
    }
    
    public void setSortable(@Nonnull Direction ascending) {
        this.sortable = true;
        setAscending(ascending);
    }

    // TODO pop?
    public final void setAscending(@Nonnull Direction ascending) {
        this.ascending = Assert.notNull(ascending, "ascending");
    }

    /**
     * Switch the order
     */
    public void switchOrder(Direction ascending) {
        this.ascending = ascending.switchIt();
    }

    /**
     * Write the content to an appendable text stream
     */
    public String toCode() {
        try {
            return toCode(new StringBuilder(4)).toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    /**
     * Write the content to an appendable text stream
     */
    public Appendable toCode(@Nonnull final Appendable writer) throws IOException {
        writer.append(ascending.safeEquals(Direction.UP) ? "-" : "");
        writer.append(String.valueOf(index));
        return writer;
    }

    @Override
    public String toString() {
        return MsgFormatter.format("[{}]:{}:{}", index, title, sortable ? ascending.name() : "-");
    }

    @Nonnull
    protected static ColumnModel ofCode(@Nonnull final String paramValue) {
        if (NUMBER.matcher(paramValue).matches()) {
            final Direction ascending = Direction.of(paramValue.charAt(0) != '-');
            final int index = Math.abs(Integer.parseInt(paramValue));
            return new ColumnModel<>(ascending, index);
        } else {
            return new ColumnModel<>(Direction.BOTH, -1);
        }
    }

}
