/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.web;

import java.nio.charset.Charset;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Configuraion of HtmlPage
 * @author Pavel Ponec
 */
public interface HtmlConfig {


    /** Html title */
    @Nonnull
    public CharSequence getTitle();

    /** Get language of the HTML page */
    public Optional<CharSequence> getLanguage();

    /**
     * Charset
     * @return the charset
     */
    @Nonnull
    public Charset getCharset();

    /**
     * Nice format or the HTML result
     * @return the niceFormat
     */
    public boolean isNiceFormat();

    /**
     * Level of th root
     * @return the firstLevel
     */
    public int getFirstLevel();

    /**
     * New line
     * @return the newLine
     */
    public String getIndentationSpace();

    /**
     * New line
     * @param indentationSpace the newLine to set
     */
    public void setIndentationSpace(String indentationSpace);

    /**
     * Use a DOM model
     * @return the dom
     */
    public boolean isDom();

    /**
     * Css ling with required order
     * @return the cssLinks
     */
    @Nonnull
    public CharSequence[] getCssLinks();

}
