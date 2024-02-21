/*
 * Copyright 2020-2022 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.web.ao;

import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.ujorm.tools.web.Element;

/**
 * Table column value writer
 * @param <T> Domain Object
 * @author Pavel Ponec
 */
@FunctionalInterface
public interface Column<T> extends Function<T, Object> {

    /**
     *  The method can be implemented for sortable columns
     * @param t A domaim objject
     * @return Returns a constatn {@code "?"}, for a Sortable columns must return a serializable object.
     */
    @Override
    default Object apply(@NotNull T t) {
        return "?";
    }

    /** Write a custom content of the table cell
     *
     * @param parent An element of the table detail.
     * @param value Value to write.
     */
    void write(@NotNull Element parent, T value);

}
