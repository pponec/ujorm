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

import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.ujorm.tools.Assert;
import org.ujorm.tools.xml.CommonXmlWriter;

/**
 * Configuraion of HtmlPage
 * @author Pavel Ponec
 */
public class DefaultHtmlConfig extends DefaultXmlConfig implements HtmlConfig {

    /** Title */
    @Nonnull
    private CharSequence title = "Demo";

    /** Css links with a required order */
    @Nonnull
    private CharSequence[] cssLinks = new CharSequence[0];

    /** Language of the HTML page */
    @Nonnull
    private CharSequence language = "en";

    @Nonnull
    private Map<String, Map<String, Object>> headers;

    @Override
    @Nonnull
    public String getDoctype() {
        return nonnull(doctype, CommonXmlWriter.HTML_DOCTYPE);
    }

    @Nonnull
    @Override
    public CharSequence getTitle() {
        return title;
    }

    @Override
    public CharSequence[] getCssLinks() {
        return cssLinks;
    }

    @Override
    public Optional<CharSequence> getLanguage() {
        return Optional.ofNullable(language);
    }

    @Override
    public Map<String, Map<String, Object>> getHeaders() {
        return headers;
    }

    /** Title is a required element by HTML 5 */
    public void setTitle(@Nonnull CharSequence title) {
        this.title = Assert.hasLength(title, "title");
    }

    public void setCssLinks(@Nonnull CharSequence... cssLinks) {
        this.cssLinks = Assert.hasLength(cssLinks, REQUIRED_MSG, "cssLinks");
    }

    public void setLanguage(@Nonnull CharSequence language) {
        this.language = language;
    }

    public void setHeaders(Map<String, Map<String, Object>> headers) {
        this.headers = headers;
    }

}
