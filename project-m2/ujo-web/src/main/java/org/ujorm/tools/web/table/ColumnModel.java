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
    private Direction direction = Direction.NONE;
    
    public ColumnModel(@Nonnull Direction direction, int index) {
        this(index, x -> null, "", null);
        setSortable(direction);
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

    @Nonnull
    public Direction getDirection() {
        return direction;
    }

    public boolean isFiltered() {
        return param != null;
    }
    
    public final void setSortable(@Nonnull final Direction direction) {
        this.sortable = true;
        setDirection(direction);
    }

    public final void setDirection(@Nonnull final Direction direction) {
        this.direction = Assert.notNull(direction, "direction");
    }

    /**
     * Write the content to an appendable text stream
     */
    public String toCode(final boolean opposite) {
        try {
            return toCode(opposite, new StringBuilder(4)).toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Write the content to an appendable text stream
     */
    public Appendable toCode(final boolean opposite, @Nonnull final Appendable writer) throws IOException {
        writer.append(direction.safeEquals(opposite ? Direction.ASC : Direction.DESC) ? "-" : "");
        writer.append(String.valueOf(index));
        return writer;
    }

    @Override
    public String toString() {
        return MsgFormatter.format("[{}]:{}:{}", index, title, sortable ? direction.name() : "-");
    }

    @Nonnull
    protected static ColumnModel ofCode(@Nonnull final String paramValue) {
        if (NUMBER.matcher(paramValue).matches()) {
            final Direction direction = Direction.of(paramValue.charAt(0) != '-');
            final int index = Math.abs(Integer.parseInt(paramValue));
            return new ColumnModel<>(direction, index);
        } else {
            return new ColumnModel<>(Direction.NONE, -1);
        }
    }

}
