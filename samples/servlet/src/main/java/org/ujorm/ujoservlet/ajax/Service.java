/*
 * Copyright 2020-2020 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.ujoservlet.ajax;

import java.security.SecureRandom;
import javax.annotation.Nonnull;
import org.ujorm.tools.xml.builder.XmlPrinter;

/**
 *
 * @author Pavel Ponec
 */
public class Service {

    /** Max text length */
    private static final int MAX_LENGTH = 11_000;

    /** Create a CSS */
    @Nonnull
    public CharSequence getCss() {
        return String.join("\n",
                "body   { margin-left:20px; background-color: #f3f6f7;}",
                "h1, h2 { color: SteelBlue;}",
                "form   { width: 500px;}",
                ".regexp{ width: 100%; margin-bottom: 2px;}",
                ".text  { width: 100%; height: 100px;}",
                ".out   { width: 100%; min-height: 100px; border:1px solid gray; "
                        + "margin-top: 10px; background-color: white;}",
                ".out span { background-color: yellow;}",
                ".out.error{ color: red;}"
        );
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
            XmlPrinter printer = new XmlPrinter();
            printer.write(rawText, false);
            return Message.of(printer.toString()
                    .replaceAll(begTag, "<span>")
                    .replaceAll(endTag, "</span>")
                    .replaceAll("&#13;&#10;", "<br/>")
                    .replaceAll("\\s", "&nbsp;")
            );
        } catch (Exception | OutOfMemoryError e) {
            return Message.of(e);
        }
    }
}
