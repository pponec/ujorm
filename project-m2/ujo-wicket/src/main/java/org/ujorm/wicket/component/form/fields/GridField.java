/*
 *  Copyright 2014-2022 Pavel Ponec
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
package org.ujorm.wicket.component.form.fields;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.wicket.CssAppender;

/**
 * GridField field with a Label including a feedback message.
 * @author Pavel Ponec
 */
public class GridField<T> extends Field<T> {

    private static final long serialVersionUID = 20130621L;

    public <U extends Ujo> GridField(Key<U,T> key) {
        super(key.getName(), key, null);
    }

    public <U extends Ujo> GridField(String componentId, Key<U,T> key, String cssClass) {
        super(componentId, key, cssClass);
    }

    /** On initialize */
    @Override
    protected void onInitialize() {
        super.onSuperInitialize();
        add(new CssAppender(getCssClass()));

        if (cssClass!=null) {
            add(new CssAppender(cssClass));
        }

         add(createLabel(getInput()));
         // TODO ....
    }

    /** Create Form inputComponent */
    @Override
    protected FormComponent createInput(String componentId, IModel<T> model) {
        final TextField<String> result = new TextField<String>(componentId, (IModel) model);
        result.setEnabled(false);
        return result;
    }

    /** Create Label and assign the CSS class {@code required} for the mandatory Field */
    @Override
    protected Component createLabel(final Component inp) {
        return new Label("label", "GRIID"); // TODO
    }

}
