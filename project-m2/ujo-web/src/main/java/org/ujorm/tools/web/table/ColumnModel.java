/*
 * Copyright 2021-2022 Pavel Ponec, https://github.com/pponec
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
import java.util.Comparator;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.msg.MsgFormatter;
import org.ujorm.tools.web.ao.Column;
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
    @NotNull
    private final Function<D, V> column;
    @NotNull
    private final CharSequence title;
    @Nullable
    private final HttpParameter param;
    /** Is the column sortable? */
    private boolean sortable = false;
    @NotNull
    private Direction direction = Direction.NONE;

    public ColumnModel(@NotNull Direction direction, int index) {
        this(index, x -> null, "", null);
        setSortable(direction);
    }

    public ColumnModel(final int index,
                       @NotNull final Function<D, V> column,
                       @NotNull final CharSequence title,
                       @Nullable final HttpParameter param) {
        this.index = index;
        this.column = Assert.notNull(column, "column");
        this.title = Assert.notNull(title, "title");
        this.param = param;
    }

    public int getIndex() {
        return index;
    }

    @NotNull
    public Function<D, V> getColumn() {
        return column;
    }

    @NotNull
    public CharSequence getTitle() {
        return title;
    }

    @Nullable
    public HttpParameter getParam() {
        return param;
    }

    @NotNull
    public HttpParameter getParam(@NotNull final HttpParameter defaultValue) {
        return param != null ? param : defaultValue;
    }

    public boolean isSortable() {
        return sortable;
    }

    @NotNull
    public Direction getDirection() {
        return direction;
    }

    public boolean isFiltered() {
        return param != null;
    }

    public final void setSortable(@NotNull final Direction direction) {
        this.sortable = true;
        setDirection(direction);
    }

    public final void setDirection(@NotNull final Direction direction) {
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
     * Write the content to an appendable text stream where the default direction is an ASCENDING.
     */
    public Appendable toCode(final boolean opposite, @NotNull final Appendable writer) throws IOException {
        final int coeff = (Direction.ASC.safeEquals(direction) == opposite) ? -1 : 1;
        writer.append(String.valueOf(coeff * (index + 1)));
        return writer;
    }

    /** Get comparator of a sortable column */
    @NotNull
    public Comparator<D> getComparator(@Nullable final Function<D,?> defaultFce) {
        return getComparator(Comparator.comparing((Function)defaultFce));
    }

    /** Get comparator of a sortable column */
    @NotNull
    public Comparator<D> getComparator(@NotNull final Comparator<D> defaultCompar) {
        if (sortable && isIncludeColumnType()) {
            final Comparator<D> compar = Comparator.comparing((Function) column);
            switch (direction) {
                case ASC:
                    return compar;
                case DESC:
                    return compar.reversed();
            }
        }
        return defaultCompar;
    }

    /** Including is more common choice */
    protected boolean isIncludeColumnType() {
        if (true) {
            return true;
        } else {
            return !(column instanceof Column);
        }
    }

    @Override
    public String toString() {
        return MsgFormatter.format("[{}]:{}:{}", index, title, sortable ? direction.name() : "-");
    }

    @NotNull
    public static ColumnModel ofCode(@NotNull final String paramValue) {
        if (NUMBER.matcher(paramValue).matches()) {
            final int intCode = Integer.parseInt(paramValue);
            final Direction direction = Direction.of(intCode > 0);
            return new ColumnModel<>(direction, Math.abs(intCode) - 1);
        } else {
            return new ColumnModel<>(Direction.NONE, -1);
        }
    }

    /** Create a stub column */
    public static <D, V> ColumnModel<D, V> ofStub() {
        return new ColumnModel<D,V>(-1, x -> null, "", null);
    }

}
