/*
 *  Copyright 2013 Pavel Ponec
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
package org.ujorm.wicket.component.gridView.columns;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Key;
import org.ujorm.Ujo;

/**
 * Key column for a boolean data type
 * @author Pavel Ponec
 * @param <UJO extends Ujo>
 *            The Model object type
 */
public class KeyColumnBoolean<UJO extends Ujo> extends KeyColumn<UJO, Boolean> {

    private static final long serialVersionUID = 1L;

    /** Class for OK value */
    protected final String cssOkClass;

    public KeyColumnBoolean(Key<UJO, Boolean> property) {
        this(property, "logical", "ok");
    }

    public KeyColumnBoolean(Key<UJO, Boolean> property, String cssClass, String cssOkClass) {
        super(property, cssClass);
        this.cssOkClass = cssOkClass;
    }

    /** Create a Value component */
    @Override
    protected IModel<?> createValueModel(final UJO ujo) {
        final Object value = keySerializable.getFirstValue(ujo);
        String result = value == null
                ? ""
                : Boolean.TRUE.equals(value)
                ? "ok"
                : "-";
        return Model.of(result);
    }

    /** Append css class */
    @Override
    protected void appendCssClass(Component value, UJO ujo) {
        super.appendCssClass(value, ujo);

        if (cssOkClass != null
        &&  Boolean.TRUE.equals(keySerializable.getFirstValue(ujo))) {
            value.add(new AttributeAppender("class", new Model(cssOkClass), " "));
        }
    }

    /** Returns a CSS class of the {@code true} value */
    public String getCssOkClass() {
        return cssOkClass;
    }
    
}
