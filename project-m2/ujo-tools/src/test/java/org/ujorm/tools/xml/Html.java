/*
 * Copyright 2018-2020 Pavel Ponec, https://github.com/pponec
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

package org.ujorm.tools.xml;

/** Some HTML constants, but this not a whole list of HTML elements,
* attributes and allowed values. */
public interface Html {

    // --- Element names ---

    /** Body element */
    String HTML_DOCTYPE = "<!DOCTYPE html>";
    /** Body element */
    String HTML = "html";
    /** Head element */
    String HEAD = "head";
    /** Meta element */
    String META = "meta";
    /** Body element */
    String BODY = "body";
    /** Title element */
    String TITLE = "title";
    /** Link element */
    String LINK = "link";
    /** Style element */
    String STYLE = "style";
    /** Javascript element */
    String SCRIPT = "script";
    /** Divission element */
    String DIV = "div";
    /** Span element */
    String SPAN = "span";
    /** Paragraph element */
    String P = "p";
    /** Form element */
    String FORM = "form";
    /** Heading element level 1 */
    String H1 = "h1";
    /** Heading element level 2 */
    String H2 = "h2";
    /** Heading element level 3 */
    String H3 = "h3";
    /** Table element */
    String TABLE = "table";
    /** Table header line */
    String THEAD = "thead";
    /** Table header cell */
    String TH = "th";
    /** Table row element */
    String TR = "tr";
    /** Table detail element */
    String TD = "td";
    /** Label element */
    String LABEL = "label";
    /** Input element */
    String INPUT = "input";
    /** Select element */
    String SELECT = "select";
    /** Option element */
    String OPTION = "option";
    /** Anchor element */
    String A = "a";
    /** Unordered list element */
    String UL = "ul";
    /** Ordered list element */
    String OL = "ol";
    /** List item element */
    String LI = "li";

    // --- Attribute names ---

    String A_ACTION = "action";
    String A_CELLPADDING = "cellpadding";
    String A_CELLSPACING = "cellspacing";
    String A_CHARSET = "charset";
    String A_CHECKED = "checked";
    String A_CLASS = "class";
    String A_CONTENT = "content";
    String A_FOR = "for";
    String A_HREF = "href";
    String A_ID = "id";
    String A_LANG = "lang";
    String A_MAXLENGTH = "maxlength";
    String A_MEDIA = "media";
    String A_METHOD = "method";
    String A_NAME = "name";
    String A_READONLY = "readonly";
    String A_REL = "rel";
    String A_SELECTED = "selected";
    String A_SRC = "src";
    String A_TARGET = "target";
    String A_TYPE = "type";
    String A_VALUE = "value";

    // --- Some attribute values ---

    String V_BLANK = "_blank";
    String V_CHECKBOX = "checkbox";
    String V_GET = "get";
    String V_HIDDEN = "hidden";
    String V_POST = "post";
    String V_RESET = "reset";
    String V_SUBMIT = "submit";
    String V_STYLESHEET = "stylesheet";
    String V_TEXT = "text";
    String V_TEXT_CSS = "text/css";
}
