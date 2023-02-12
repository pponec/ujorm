/*
 * Copyright 2014-2016, Pavel Ponec
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


import com.googlecode.wicket.jquery.ui.widget.dialog.AbstractDialog;
import com.googlecode.wicket.jquery.ui.widget.dialog.DialogButton;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Ujo;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.tools.LocalizedModel;

/**
 * Common Message Dialog
 * @author Pavel Ponec
 */
public class MessageDialogPanel<T extends Ujo> extends AbstractDialog {
    private static final long serialVersionUID = 20140401L;

    protected final DialogButton btnSure = new DialogButton("btn", LBL_OK);

    /** Message */
    protected static final String MESSAGE = "message";

    /** CSS alert */
    private static final String ALERT_CSS = "alert-text";

    public MessageDialogPanel(String id, IModel<String> title) {
        super(id, title);
    }

    /** Create buttons */
    @Override
    protected List<DialogButton> getButtons() {
        return Collections.singletonList(this.btnSure);
    }


    /** Create message */
    @Override
    protected void onInitialize() {
        super.onInitialize();
        MultiLineLabel message = new MultiLineLabel(MESSAGE, Model.of(""));
        message.setOutputMarkupId(true);
        add(message);
    }

    /** Overwrite it */
    @Override
    public void onClose(IPartialPageRequestHandler requestHandler, DialogButton db) {
    }

    /** Set a dialog message */
    public void setMessage(IModel<String> message) {
        get(MESSAGE).setDefaultModel(message);
    }

    /** Create the default message dialog */
    public static MessageDialogPanel create(String componentId) {
        final MessageDialogPanel result = new MessageDialogPanel(componentId, Model.of("INFO"));
        return result;
    }

    public void show(UjoEvent<?> event, LocalizedModel localizedModel, String delete) {
        super.open(event.getTarget());
        this.setMessage(localizedModel);
        event.addTarget(get(MESSAGE));
    }

}
