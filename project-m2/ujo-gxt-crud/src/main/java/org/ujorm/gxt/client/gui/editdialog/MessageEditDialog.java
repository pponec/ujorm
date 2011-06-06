package org.ujorm.gxt.client.gui.editdialog;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ujorm.gxt.client.ClientClassConfig;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.PropertyMetadataProvider;
import org.ujorm.gxt.client.commons.Icons;
import org.ujorm.gxt.client.controller.MessageControllerAsync;

public abstract class MessageEditDialog<CUJO extends Cujo> extends EditWindow<CUJO> {

    public static final String MESSAGE_DIALOG = "message_dialog";
    /** A text maximal length (from meta-model) to creating a TextArea
     * instead of TextField component.  */
    protected static final int TEXT_AREA_LIMIT = 180;
    protected LayoutContainer panel;
    protected LayoutContainer rowPanel;
    protected TextField<String> keyTextField;
    protected Label label;
    /** Insert or update ? */
    protected boolean newState;
    /** The client business object to edit. */
    protected ModelData message;
    /** A result of a BO saving. */
    protected PropertyMetadataProvider metadataProvider;
    //
    private MessageControllerAsync messageController;
    private Map<String, TextArea> textAreas;

    public MessageEditDialog(ModelData message, boolean newState) {
        this.message = message;
        this.newState = newState;
        this.metadataProvider = ClientClassConfig.getInstance().getPropertyMedatata();
    }

    public abstract String translate (String parent, String name);

    public Map<String, TextArea> getTextAreas() {
        if (textAreas == null) {
            textAreas = new HashMap<String, TextArea>();
        }
        return textAreas;
    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        //
        messageController = MessageControllerAsync.Pool.get();
        setIcon(newState ? Icons.Pool.add() : Icons.Pool.edit());
        setHeading(newState ? translate(MESSAGE_DIALOG, "newMessage") : translate(MESSAGE_DIALOG, "editMessage"));
        setScrollMode(Scroll.AUTOY);
        setLayout(new FitLayout());
        setClosable(true);
        setModal(true);
        setWidth(400);
        setHeight(400);

        rowPanel = new LayoutContainer();
        rowPanel.setLayout(new RowLayout(Orientation.HORIZONTAL));
        rowPanel.setHeight(30);

        getTextAreas();

        panel = new LayoutContainer();
        panel.setScrollMode(Scroll.AUTOY);
        panel.setLayout(new RowLayout(Orientation.VERTICAL));

        label = new Label(translate(MESSAGE_DIALOG, "key") + ":");
        label.addStyleName("label-align-right");
        String value = (String) message.get("key");
        keyTextField = new TextField<String>();
        if (!newState) {
            keyTextField.setEnabled(false);
        }
        keyTextField.setFieldLabel(translate(MESSAGE_DIALOG, "key"));
        keyTextField.setAllowBlank(false);
        keyTextField.setValue(value != null ? value : "");
        keyTextField.setAutoWidth(true);

        rowPanel.add(label, new RowData(0.2, 0, new Margins(5)));
        rowPanel.add(keyTextField, new RowData(0.75, 0, new Margins(5)));
        panel.add(rowPanel);

        messageController.listLocales(new AsyncCallback<List<String>>() {

            @Override
            public void onSuccess(List<String> result) {
                for (String locale : result) {
                    label = new Label(translate(MESSAGE_DIALOG, locale) + ":");
                    label.addStyleName("label-align-right");
                    String value = (String) message.get(locale);
                    TextArea translationTextArea = createTextArea(translate(MESSAGE_DIALOG, locale));
                    translationTextArea.setValue(value != null ? value : "");
                    textAreas.put(locale, translationTextArea);
                    translationTextArea.setAutoWidth(true);

                    rowPanel = new LayoutContainer();
                    rowPanel.setLayout(new RowLayout(Orientation.HORIZONTAL));
                    rowPanel.setHeight(70);
                    rowPanel.add(label, new RowData(0.2, 0, new Margins(5)));
                    rowPanel.add(translationTextArea, new RowData(0.75, 0, new Margins(5)));
                    panel.add(rowPanel);
                }

                final Button okButton = newOkButton(newState);
                actionSaveOrUpdate(okButton);

                okButton.addStyleName("form-control-margin");
                RowData newRowData = new RowData();
                newRowData.setMargins(new Margins(5));

                BorderLayoutData eastData = new BorderLayoutData(LayoutRegion.EAST);
                eastData.setSize(100);
                eastData.setMargins(new Margins(5, 22, 5, 5));

                rowPanel = new LayoutContainer();
                rowPanel.setLayout(new BorderLayout());
                rowPanel.add(okButton, eastData);
                panel.add(rowPanel, newRowData);

                add(panel);
                layout();
            }

            @Override
            public void onFailure(Throwable caught) {
                // TODO:
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

    }

    /** Create a TextArea Component */
    protected TextArea createTextArea(String field) {
        return createTextArea(field, 100);
    }

    /** Create a TextArea Component */
    protected TextArea createTextArea(String field, int height) {
        TextArea result = new TextArea();
        result.setHeight(height);
        result.setPreventScrollbars(true);
        result.setFieldLabel(translate(MESSAGE_DIALOG, field));

        return result;
    }

    /** New instance of OK button. */
    protected Button newOkButton(boolean newState) {
        Button result = new Button(newState ? translate(MESSAGE_DIALOG, "create") : translate(MESSAGE_DIALOG, "update"));
        result.setIcon(Icons.Pool.ok());
        return result;
    }

    private void actionSaveOrUpdate(final Button okButton) {
        if (newState) {
            okButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {

                    for (String textAreaKey : textAreas.keySet()) {
                        String value = textAreas.get(textAreaKey).getValue();
                        if (value == null || value.isEmpty()) {
                            Info.display(translate(MESSAGE_DIALOG, "warning"), translate(MESSAGE_DIALOG, "fillAll"));
                            return;
                        }
                    }

                    createMessage();
                    changedData = true;
                    messageController.save(message, new AsyncCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {
                            hide();
                            String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();
                            refreshClientMessages((String) message.get("key"), (String) message.get(currentLocale));

                            return;
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            // TODO:
                            throw new UnsupportedOperationException("Not supported yet.");
                        }
                    });
                }
            });
        } else {
            okButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {

                    for (String textAreaKey : textAreas.keySet()) {
                        String value = textAreas.get(textAreaKey).getValue();
                        if (value == null || value.isEmpty()) {
                            Info.display(translate(MESSAGE_DIALOG, "warning"), translate(MESSAGE_DIALOG, "fillAll"));
                            return;
                        }
                    }

                    createMessage();
                    changedData = true;
                    messageController.update(message, new AsyncCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {
                            hide();
                            String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();
                            refreshClientMessages((String) message.get("key"), (String) message.get(currentLocale));

                            return;
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            // TODO:
                            throw new UnsupportedOperationException("Not supported yet.");
                        }
                    });
                }
            });
        }
    }

    /** Vlastni implementace */
    protected void refreshClientMessages(String key, String value) {

    }

    private void createMessage() {
        message.set("key", keyTextField.getValue());
        for (String locale : textAreas.keySet()) {
            message.set(locale, textAreas.get(locale).getValue());
        }
    }



    public void setValues(Map<String, String> result) {
        if (result != null && !result.isEmpty()) {
            for (String key : result.keySet()) {
                final String value = result.get(key);
                message.set(key, value);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="GET/SET">
    public boolean isNewState() {
        return newState;
    }

    public void setNewState(boolean newState) {
        this.newState = newState;
    }
    // </editor-fold>
}
