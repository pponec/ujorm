/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
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

package org.ujorm.tools.dom;

import java.io.IOException;
import javax.annotation.Nonnull;

/**
 * The element writter for a formatting text
 * @see XmlElement
 * @since 1.88
 * @author Pavel Ponec
 */
public interface ElementWriter<T extends Element> {

    /** A special XML character */
    char XML_GT = '>';
    /** A special XML character */
    char XML_LT = '<';
    /** A special XML character */
    char XML_AMP = '&';
    /** A special XML character */
    char XML_QUOT = '\'';
    /** A special XML character */
    char XML_2QUOT = '"';
    /** A special XML character */
    char CHAR_SPACE = ' ';
    /** A new line character */
    char CHAR_NEW_LINE = '\n';
    /** A CDATA beg markup sequence */
    String CDATA_BEG = "<![CDATA[";
    /** A CDATA end markup sequence */
    String CDATA_END = "]]>";
    /** A comment beg sequence */
    String COMMENT_BEG = "<!--";
    /** A comment end sequence */
    String COMMENT_END = "-->";

    /** Render an XML element */
    @Nonnull
    public ElementWriter write(@Nonnull T element) throws IOException;


}
