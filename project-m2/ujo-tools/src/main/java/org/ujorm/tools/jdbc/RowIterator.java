/*
 * Copyright 2008 - 2022 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/RowIterator.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.jdbc;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.set.LoopingIterator;

/**
 * An Iterator for the ResultSet items.
 *
 * <h3>Usage</h3>
 * <pre class="pre">
 *     new RowIterator(ps).toStream().forEach((RsConsumer)(resultSet) -> {
 *         int value = resultSet.getInt(1);
 *         System.out.println(" value: " + value);
 *     });
 * </pre>
 *
 * @see JdbcBuilder
 * @author Pavel Ponec
 * @since 1.86
 */
public class RowIterator implements LoopingIterator<ResultSet> {

    /** Prepared Statement */
    @NotNull
    private final PreparedStatement ps;
    /** ResultSet */
    @Nullable
    private ResultSet rs;
    /** It the cursor ready for reading? After a row reading the value will be set to false. */
    private boolean cursorReady = false;
    /** Has a resultset a next row? */
    private boolean hasNext = false;

    public RowIterator(@NotNull final PreparedStatement ps) {
        this.ps = ps;
    }

    /** The last checking closes all resources. */
    @Override
    public boolean hasNext() throws IllegalStateException {
        if (!cursorReady) try {
            if (rs == null) {
                rs = ps.executeQuery();
            }
            hasNext = rs.next();
            if (!hasNext) {
                try {
                    close();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
            cursorReady = true;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return hasNext;
    }

    @Override
    public ResultSet next() {
        if (hasNext()) {
            cursorReady = false;
            return rs;
        }
        throw new NoSuchElementException();
    }

    /** Close all resources */
    @Override
    public void close() throws IOException {
        if (rs != null) {
            try (PreparedStatement tempPs = ps; ResultSet tempRs = rs) {
                cursorReady = true;
                hasNext = false;
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
