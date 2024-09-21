/*
 * Copyright 2018-2022 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.ujoservlet.benchmark.dom4j;

import jakarta.servlet.http.HttpServlet;
import org.dom4j.Element;
import org.ujorm.tools.msg.MsgFormatter;
import org.ujorm.tools.web.Html;
import org.ujorm.ujoservlet.tools.ApplService;

/**
 * Common static tools for Dom4j library
 * @author Pavel Ponec
 */
public abstract class ApplDom4jService {

    /** Template for a source link */
    private static final String SOURCE_LINK_TEMPLATE = ApplService.SOURCE_LINK_TEMPLATE;

    /** Static methods only */
    private ApplDom4jService() {
    }

    /** Create a link to the source code on the GitHub */
    private static String getSourceLink(Class<? extends HttpServlet> servletClass, short firstLine) {
        return MsgFormatter.format(SOURCE_LINK_TEMPLATE, servletClass.getName().replace('.', '/'), firstLine);
    }

    /** Add a common footer for Dom4j library */
    public static void addFooterDom4j(final Element parent, HttpServlet servlet, short showLine) {
        Element footer = parent.addElement(Html.DIV)
                .addAttribute(Html.A_CLASS, "footer");
        footer.addText("See a ")
                .addElement(Html.A)
                .addAttribute(Html.A_HREF, getSourceLink(servlet.getClass(), showLine))
                .addAttribute(Html.A_TARGET, Html.V_BLANK)
                .addText(servlet.getClass().getSimpleName());
        footer.addText(" source class of the Ujorm framework.");
    }
}
