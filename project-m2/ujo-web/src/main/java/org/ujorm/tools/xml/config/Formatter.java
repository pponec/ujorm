/*
 * Copyright 2020-2022 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/XmlElement.java
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
package org.ujorm.tools.xml.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.xml.ApiElement;

/**
 * A value formatter.
 * @author Pavel Ponec
 */
public interface Formatter {

    /**
     * Format an objet value to a string ouptut, special characters will be escaped later.
     * @param value A data value
     * @param element A related element
     * @param attributeName A name of the attribute, if any.
     * @return A target non-nnul sequence is required.
     */
    @NotNull
    CharSequence format(@Nullable Object value, @NotNull ApiElement element, @Nullable String attributeName);

}
