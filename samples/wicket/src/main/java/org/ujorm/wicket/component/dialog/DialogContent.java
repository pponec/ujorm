/*
 * Copyright 2013, Pavel Ponec
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
package org.ujorm.wicket.component.dialog;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.ujorm.wicket.CssAppender;

/**
 * Common Message Dialog
 * @author Pavel Ponec
 */
public class DialogContent extends AbstractContent {
    private static final long serialVersionUID = 20130621L;

    public DialogContent(ModalWindow modalWindow, IModel<String> model) {
        super(modalWindow, model);

        /** Add message */
        final Label label = new Label(repeater.newChildId(), model);
        label.add(new CssAppender("alert-text"));
        repeater.add(label);
    }
}
