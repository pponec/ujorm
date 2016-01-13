/*
 *  Copyright 2012-2016 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.wicket;

import java.io.Serializable;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * CSS Appender
 * @author Pavel Ponec
 */
final public class CssAppender extends AttributeAppender {

    /** CSS class */
    public static final String CSS_CLASS = "class";

    /** The constructor with a String argument */
    public <V extends CharSequence & Serializable> CssAppender(V value) {
        this(Model.of(value));
    }

    /** The constructor with a Model argument */
    public CssAppender(IModel<?> valueModel) {
        super(CSS_CLASS, valueModel, " ");
    }

    /** Returns a CSS class */
    public String getCssClass() {
        return (String) getReplaceModel().getObject();
    }

    /** The CssAppender factory */
    public static CssAppender of(final String cssName) {
        return new CssAppender(cssName);
    }
}
