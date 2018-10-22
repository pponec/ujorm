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

package org.ujorm.ujoservlet;

/** Some HTML constants,
* but this is certainly not a whole list of HTML elements,
* attributes and allowed values. */
public interface Html {

    // --- Element names ---
    String BODY = "body";
    String META = "meta";
    String DIV = "div";
    String P = "p";
    String FORM = "form";
    String H1 = "h1";
    String TABLE = "table";
    String TR = "tr";
    String TD = "td";
    String LABEL = "label";
    String INPUT = "input";
    String SELECT = "select";
    String OPTION = "option";
    String A = "a";

    // --- Attribute names ---

    String A_CHECKED = "checked";
    String A_CLASS = "class";
    String A_CONTENT = "content";
    String A_FOR = "for";
    String A_HREF = "href";
    String A_ID = "id";
    String A_LANG = "lang";
    String A_MAXLENGTH = "maxlength";
    String A_METHOD = "method";
    String A_NAME = "name";
    String A_READONLY = "readonly";
    String A_SELECTED = "selected";
    String A_TARGET = "target";
    String A_TYPE = "type";
    String A_VALUE = "value";

    // --- HTML Values ---

    String V_BLANK = "_blank";
    String V_CHECKBOX = "checkbox";
    String V_DEVICE = "width=device-width, initial-scale=1.0";
    String V_GET = "get";
    String V_HIDDEN = "hidden";
    String V_POST = "post";
    String V_SUBMIT = "submit";
    String V_TEXT = "text";
    String V_VIEWPORT = "viewport";
}
