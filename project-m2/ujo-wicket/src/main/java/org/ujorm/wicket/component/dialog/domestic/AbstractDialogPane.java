/*
 * Copyright 2013-2015, Pavel Ponec
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
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEventSink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.validator.ValidationError;
import org.ujorm.validator.ValidationException;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.UjoEvent;

/**
 * Abstract Message Dialog Content
 * @author Pavel Ponec
 */
public abstract class AbstractDialogPane<T> extends GenericPanel<T> {
    private static final long serialVersionUID = 20130621L;
    /** Default logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(AbstractDialogPane.class);

    protected static final String BUTTON_PREFIX = "button.";
    protected static final String ACTION_BUTTON_ID = "actionButton";
    protected static final String CANCEL_BUTTON_ID = "cancelButton";
    protected static final String REPEATER_ID = "repeater";

    /** Dialog form */
    protected final Form<?> form;
    /** Dialog modal window */
    protected final ModalWindow modalWindow;
    /** Dialog repeater */
    protected final RepeatingView repeater;
    /** Action code */
    private String action = "";
    /** Dialog autoclose request */
    protected final boolean autoClose;
    /** Target of the close action */
    protected IEventSink eventTarget;

    public AbstractDialogPane(ModalWindow modalWindow, IModel<? super T> model, boolean autoClose) {
        super(modalWindow.getContentId(), (IModel) model);
        this.modalWindow = modalWindow;
        this.modalWindow.setContent(this);
        this.autoClose = autoClose;
        this.setOutputMarkupPlaceholderTag(true);

        // Form Dialog:
        this.add(form = new Form("dialogForm"));
        // Content Dialog:
        form.add(repeater = new RepeatingView(REPEATER_ID));
    }

    /** Initialization */
    @Override
    protected void onInitialize() {
        super.onInitialize();

        // Form:
        form.setOutputMarkupId(true);
        form.add(createActionButton(ACTION_BUTTON_ID, "save"));
        form.add(createCancelButton(CANCEL_BUTTON_ID, "cancel"));
    }

    /** Action code */
    public String getAction() {
        return action;
    }

    /** Action code */
    public void setAction(String action) {
        this.action = action;
    }

    /** Returns a base model object / entity */
    public T getBaseModelObject() {
        return (T) getDefaultModelObject();
    }

    /** Creates the default Confirmation button */
    protected AjaxButton createActionButton(String id, String propertyName) {
        final AjaxButton result = new AjaxButton
                ( id
                , getButtonModel(propertyName)
                , form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    target.add(form);
                    final UjoEvent<T> uEvent = new UjoEvent<T>(getAction(), false, getBaseModelObject(), target);
                    if (eventTarget != null) {
                        send(eventTarget, Broadcast.EXACT, uEvent);
                    } else {
                        send(getPage(), Broadcast.BREADTH, uEvent);
                    }
                    if (autoClose) {
                       modalWindow.close(target); // the dialog is closed on the success
                    }
                } catch (RuntimeException | OutOfMemoryError e) {
                    setFeedback(e);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }
        };
        result.add(new CssAppender("btn btn-primary"));
        form.setDefaultButton(result);
        return result;
    }

    /** Creates the default Cancel button */
    protected AjaxButton createCancelButton(String id, String propertyName) {
        final AjaxButton result = new AjaxButton
                ( id
                , getButtonModel(propertyName)
                , form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                closeForm(target, form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                closeForm(target, form);
            }
        };
        result.add(new CssAppender("btn"));
        return result;
    }

    /** Close action */
    protected void closeForm(AjaxRequestTarget target, Form<?> form) {
        form.clearInput();
        target.add(form);
        modalWindow.close(target);
    }

    /** Show an emergency message */
    protected void setFeedback(Throwable e) { e.getCause();
        if (e instanceof ValidationException) {
            final ValidationError error = ((ValidationException) e).getError();
            final String defaultMsg = error.getDefaultTemplate() + " [" + error.getLocalizationKey() + "]";
            final String template = getString( error.getLocalizationKey(), null, defaultMsg);
            final String msg = error.getMessage(template, getLocale());
            setFeedback(Model.of(msg));
        } else {
            final String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
            setFeedback(Model.of(msg));
            LOGGER.log(UjoLogger.ERROR, msg, e); // Unchecked exception
        }
    }

    /** Show an common feedback message
     * @param message The {@code null} value clears the message. */
    protected abstract void setFeedback(IModel<String> message);

    /**
     * Show dialog and assign a data from domain object
     * @param target target
     */
    public void show(AjaxRequestTarget target, IModel<T> body) {
        show(target, null, body, null);
    }

    /**
     * Show dialog and assign a data from domain object
     */
    public void show(UjoEvent<T> event, IModel<String> title) {
        setAction(event.getAction());
        show(event.getTarget(), title, event.getUjoModel());
    }

    /**
     * Show dialog and assign a data from domain object
     */
    public void show(UjoEvent<T> event, IModel<String> title, String actionButtonProperty) {
        setAction(event.getAction());
        show(event.getTarget(), title, event.getUjoModel(), actionButtonProperty);
    }

    /**
     * Show dialog and assign a data from domain object
     * @param title Window title
     * @param target target
     * @param body Body model
     */
    public void show(AjaxRequestTarget target, IModel<String> title, IModel<T> body) {
        show(target, title, body, null);
    }

    /**
     * Show dialog and assign a data from domain object
     * @param title Dialog title
     * @param body Dialog body as a default mode
     * @param actionButtonProperty Action button key
     * @param target Target
     * @see #onShowBefore(org.apache.wicket.ajax.AjaxRequestTarget)
     * @see #onShowAfter(org.apache.wicket.ajax.AjaxRequestTarget)
     */
    public final void show
        ( final AjaxRequestTarget target
        , final IModel<String> title
        , final IModel<T> body
        , final String actionButtonProperty) {
        setDefaultModel(body);
        setFeedback((IModel)null);
        if (title != null) {
           getModalWindow().setTitle(title);
        }
        if (actionButtonProperty != null) {
           form.get(ACTION_BUTTON_ID).setDefaultModel(getButtonModel(actionButtonProperty));
        }
        onShowBefore(target);
        getModalWindow().show(target);
        target.add(form);
    }

    /** An action in show before */
    protected void onShowBefore(AjaxRequestTarget target) {
    }

    /** Returns modal WIndow */
    public ModalWindow getModalWindow() {
        return modalWindow;
    }

    /** Close the modal window */
    public void close (AjaxRequestTarget target) {
        modalWindow.close(target);
    }

    /** Close the modal window */
    public final void close (UjoEvent target) {
        close(target.getTarget());
    }

    /** Get Save button key key */
    protected IModel<String> getButtonModel(String propertyName) {
        return new ResourceModel(BUTTON_PREFIX + propertyName, propertyName);
    }

    /** Target of the close action */
    public IEventSink getTarget() {
        return eventTarget;
    }

    /** Target of the close action */
    public void setTarget(IEventSink target) {
        this.eventTarget = target;
    }
}
