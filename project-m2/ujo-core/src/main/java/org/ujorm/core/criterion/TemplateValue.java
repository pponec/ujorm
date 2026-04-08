/*
 * Copyright 2013-2022 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.core.criterion;

import org.jetbrains.annotations.NotNull;
import org.ujorm.tools.common.Array;

import java.util.List;

/**
 * A template value for custom SQL criteria.
 * <p>
 * This template allows defining custom SQL expressions with placeholders that are dynamically
 * replaced during the query building process. The expected placeholders are:
 * <ul>
 * <li>{@code {0}} : Replaced by the corresponding database column name.</li>
 * <li>{@code {1}} : Replaced by all items of the parameter list, separated by commas.
 * Typically used in SQL expressions like {@code name IN (1, 2, 3)}.</li>
 * </ul>
 *
 * @param <V> The type of the values in the parameter list
 * @author Pavel Ponec
 */
public record TemplateValue<V>(

        /** Returns the template by the format {@code "{0} > {1} OR {0} != {2}"}.
         * @return the SQL template string
         */
        String template,

        /** Returns the criterion right value list used for placeholder replacement.
         * @return the list of parameter values
         */
        Array<V> values

) {

    @NotNull
    public Array<V> valuesNonNull() {
        return values != null ? values : Array.of();
    }

    @Override
    public String toString() {
        return template + ' ' + values;
    }
}