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
package org.ujorm.ujoservlet.ajax.ao;

import java.security.SecureRandom;
import org.jetbrains.annotations.NotNull;
import org.ujorm.tools.xml.builder.XmlPrinter;
import org.ujorm.tools.xml.config.XmlConfig;

/**
 *
 * @author Pavel Ponec
 */
public class Service {

    /** Max text length */
    private static final int MAX_LENGTH = 1_100;

    /** Create a CSS */
    @NotNull
    public CharSequence[] getCss() {
        return new CharSequence[] { ""
                , "body   { margin-left:20px; background-color: #f3f6f7;}"
                , "h1, h2 { color: SteelBlue;}"
                , "form   { width: 500px;}"
                , ".subtitle{ font-size: 10px; color: silver;}"
                , "textarea { height: 120px; margin: 3px 0;}"
                , ".out   { min-height: 100px; margin-top: 10px;"
                + " height: inherit; white-space: pre-wrap;}"
                , ".out span { background-color: yellow;}"
                , ".out .error { background-color: white; color: red;}"
        };
    }

    /**
     * Highlights the original text according to the regular expression
     * using HTML element {@code <span>}.
     *
     * @param regexp An regular expression
     * @param text An original text
     * @return Raw HTML text.
     */
    public Message highlight(String regexp, String text) {
        try {
            if (text.length() > MAX_LENGTH) {
                String msg = String.format("Shorten text to a maximum of %s characters.",
                        MAX_LENGTH);
                throw new IllegalArgumentException(msg);
            }
            SecureRandom random = new SecureRandom();
            String begTag = "_" + random.nextLong();
            String endTag = "_" + random.nextLong();
            String rawText = text.replaceAll(
                    "(" + regexp + ")",
                    begTag + "$1" + endTag);
            XmlPrinter printer = new XmlPrinter(new StringBuilder(), XmlConfig.ofDoctype(""));
            printer.write(rawText, false);
            return Message.of(printer.toString()
                    .replaceAll(begTag, "<span>")
                    .replaceAll(endTag, "</span>")
            );
        } catch (Exception | OutOfMemoryError e) {
            return Message.of(e);
        }
    }
}
