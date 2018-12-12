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
package org.ujorm.ujoservlet.tools;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServlet;
import org.ujorm.tools.msg.MsgFormatter;
import org.ujorm.tools.xml.AbstractElement;
import org.ujorm.tools.xml.Html;

/**
 * Common services with static methdos
 * @author Pavel Ponec
 */
public abstract class ApplService {

    /** A HTML code page. Try the {@code  Charset.forName("windows-1250")} for example. */
    public static final Charset CODE_PAGE = StandardCharsets.UTF_8;

    /** Template for a source link */
    public static final String SOURCE_LINK_TEMPLATE = "https://github.com/pponec/ujorm/blob/master"
            + "/samples/servlet/src/main/java/{}.java#L{}";

    /** Static methods only */
    private ApplService() {
    }

    /** Create a link to the source code on the GitHub */
    private static String getSourceLink(Class<? extends HttpServlet> servletClass, short firstLine) {
        return MsgFormatter.format(SOURCE_LINK_TEMPLATE, servletClass.getName().replace('.', '/'), firstLine);
    }

    /** Add a common footer for DOM */
    public static void addFooter(final AbstractElement parent, HttpServlet servlet, short showLine) throws IOException {
        AbstractElement footer = parent.addElement(Html.DIV)
                .setAttrib(Html.A_CLASS, "footer");
        footer.addTextWithSpace("See a")
                .addElement(Html.A)
                .setAttrib(Html.A_HREF, getSourceLink(servlet.getClass(), showLine))
                .setAttrib(Html.A_TARGET, Html.V_BLANK)
                .addText(servlet.getClass().getSimpleName());
        footer.addTextWithSpace("source class of the Ujorm framework.");
    }
}
