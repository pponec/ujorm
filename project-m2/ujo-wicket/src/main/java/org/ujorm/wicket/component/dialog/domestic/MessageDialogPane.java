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
package org.ujorm.wicket.component.dialog.domestic;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Ujo;
import static org.ujorm.wicket.CssAppender.*;

/**
 * Common Message Dialog.
 * <h4>Usage</h4>
 * <pre>{@code add((removeDialog = MessageDialogPane.create("removeDialog", 290, 160)).getModalWindow()); }</pre>
 * @author Pavel Ponec
 */
public class MessageDialogPane<T extends Ujo> extends AbstractDialogPane<T> {
    private static final long serialVersionUID = 20130621L;

    /** CSS alert */
    private static final String ALERT_CSS = "alert-text";

    /** Error message */
    private boolean feedback;

    public MessageDialogPane(ModalWindow modalWindow, IModel<String> model, boolean autoClose) {
        super(modalWindow, new Model(), autoClose);
        repeater.add(new MultiLineLabel(repeater.newChildId(), model) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                final String cssClass = feedback
                        ? ALERT_CSS + " help-inline"
                        : ALERT_CSS;
                tag.put(CSS_CLASS, cssClass);
            }
        });
    }

    /** Set a dialog message */
    public void setMessage(IModel<String> message) {
        repeater.get(0).setDefaultModel(message);
        feedback = false;
    }

    /** {@inheritDoc} */
    @Override
    protected void setFeedback(IModel<String> message) {
        if (message != null) {
            setMessage(message);
            feedback = true;
        }
    }

    /** Create the default message dialog with an autoclose feature. */
    public static MessageDialogPane create(String componentId, int width, int height) {
        return create(componentId, width, height, true);
    }

    /** Create the default message dialog */
    public static MessageDialogPane create(String componentId, int width, int height, boolean autoClose) {
        final IModel<String> model = Model.of("");
        final ModalWindow modalWindow = new ModalWindow(componentId, model);
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);

        final MessageDialogPane result = new MessageDialogPane(modalWindow, model, autoClose);
        modalWindow.setInitialWidth(width);
        modalWindow.setInitialHeight(height);
        //modalWindow.setCookieName(componentId + "-modalDialog");

        return result;
    }

    @Override
    protected void onShowBefore(AjaxRequestTarget target) {
        super.onShowBefore(target);
        final IModel<T> message = getModel();
        if (message != null && ((Object) message.getObject()) instanceof String) {
            setMessage((IModel) message);
        }
    }

//    /** Show the dialog with a message */
//    @Override
//    @SuppressWarnings("unchecked")
//    public void show(AjaxRequestTarget target, IModel<String> title, IModel<T> message, String actionButtonProperty) {
//        if (message != null && ((Object) message.getObject()) instanceof String) {
//            setMessage((IModel) message);
//        }
//        super.show(target, title, message, actionButtonProperty);
//    }
}
