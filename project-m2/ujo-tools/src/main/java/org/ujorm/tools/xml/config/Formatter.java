/*
 * Copyright 2020-2020 Pavel Ponec,
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

import javax.annotation.Nonnull;
import org.ujorm.tools.xml.ApiElement;

/**
 * A value formatter.
 * @author Pavel Ponec
 */
public interface Formatter {

    /**
     * Format an objet value to XML or HTML page, special character will be escaped.
     * @param value A data value
     * @param attribute The value of an element attribute is required.
     * @param element A related element
     * @return A target sequence.
     */
    CharSequence format(@Nonnull Object value, boolean attribute, @Nonnull ApiElement element);

}
