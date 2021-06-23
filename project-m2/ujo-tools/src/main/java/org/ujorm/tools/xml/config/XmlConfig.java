/*
 * Copyright 2018-2019 Pavel Ponec, https://github.com/pponec
 * https://github.com/pponec/ujorm/blob/master/samples/servlet/src/main/java/org/ujorm/ujoservlet/tools/Html.java
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

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.xml.config.impl.DefaultXmlConfig;

/**
 * Configuraion of HtmlPage
 * @author Pavel Ponec
 */
public interface XmlConfig {

    /** Doctype */
    @Nonnull
    CharSequence getDoctype();

    /**
     * Charset
     * @return the charset
     */
    @Nonnull
    Charset getCharset();

    /**
     * Level of the root element, the value may be negative.
     * @return the firstLevel
     */
    int getFirstLevel();

    /**
     * New line
     * @return the newLine
     */
    CharSequence getIndentation();

    /** A replacement text instead of the {@code null} value */
    @Nonnull
    CharSequence getDefaultValue();

    /** A new line sequence */
    @Nonnull
    CharSequence getNewLine();

    /**
     * HTTP cache is allowed
     * @return
     */
    boolean isCacheAllowed();

    /**
     * Get a value formatter
     */
    @Nonnull
    Formatter getFormatter();

    /**
     * Create a new default config
     * @return
     */
    public static DefaultXmlConfig ofDefault() {
        return new DefaultXmlConfig();
    }

    /**
     * Create a new default config
     * @return
     */
    public static DefaultXmlConfig ofDoctype(@Nullable final String doctype) {
        final DefaultXmlConfig result = new DefaultXmlConfig();
        result.setDoctype(doctype);
        return result;
    }
}
