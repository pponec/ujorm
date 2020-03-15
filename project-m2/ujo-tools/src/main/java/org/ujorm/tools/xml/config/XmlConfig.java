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

import org.ujorm.tools.xml.config.impl.DefaultXmlConfig;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;

/**
 * Configuraion of HtmlPage
 * @author Pavel Ponec
 */
public interface XmlConfig {

    /** Doctype */
    @Nonnull
    public String getDoctype();

    /**
     * Charset
     * @return the charset
     */
    @Nonnull
    public Charset getCharset();

    /**
     * Level of the root element, the value may be negative.
     * @return the firstLevel
     */
    public int getFirstLevel();

    /**
     * New line
     * @return the newLine
     */
    public String getIndentation();

    /** A replacement text instead of the {@code null} value */
    @Nonnull
    public String getDefaultValue();

    /**
     * Use a DOM model
     * @return the dom
     */
    public boolean isDom();

    /**
     * HTTP cache is allowed
     * @return
     */
    public boolean isCacheAllowed();

    /**
     * Create a new default config
     * @return
     */
    public static DefaultXmlConfig ofDefault() {
        return new DefaultXmlConfig();
    }

}
