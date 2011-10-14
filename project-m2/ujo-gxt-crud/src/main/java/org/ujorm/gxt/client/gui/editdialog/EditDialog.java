/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujorm.gxt.client.gui.editdialog;

import org.ujorm.gxt.client.gui.CujoBox;
import org.ujorm.gxt.client.gui.CujoField;
import org.ujorm.gxt.client.gui.livegrid.LiveGridPanelDialog;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ujorm.gxt.client.CEnum;
import org.ujorm.gxt.client.CPropertyEnum;
import org.ujorm.gxt.client.ClientClassConfig;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoManager;
import org.ujorm.gxt.client.CujoModel;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.PropertyMetadata;
import org.ujorm.gxt.client.PropertyMetadataProvider;
import org.ujorm.gxt.client.ao.ValidationMessage;
import org.ujorm.gxt.client.commons.Icons;
import org.ujorm.gxt.client.controller.LiveGridControllerAsync;
import org.ujorm.gxt.client.controller.MessageControllerAsync;
import org.ujorm.gxt.client.cquery.CCriterion;
import org.ujorm.gxt.client.gui.FloatTextArea;
import org.ujorm.gxt.client.gui.livegrid.LiveGridPanel;
import org.ujorm.gxt.client.gui.livegrid.LiveGridPanelImpl;
import org.ujorm.gxt.client.tools.MessageDialog;

/**
 *
 * @author Pelc Dobroslav
 */
public abstract class EditDialog<CUJO extends Cujo> extends EditWindow<CUJO> {

    public static final int ROWS_PADDING_TOP = 10;
    protected int DIALOG_WIDTH = 600;
    protected int DIALOG_HEIGHT = 400;
    //
    protected int LABEL_WIDTH = 180;
    protected int COMPONENT_WIDTH = 310;
    // Const
    public static final int ICON_HEIGHT = 16;
    public static final int ICON_WIDTH = 16;
    //
    public static final int BUTTON_WIDTH = 22;
    public static final int BUTTON_HEIGHT = 22;
    //
    public static final String label = "label";
    public static final String help = "help";
    public static final String buttons_before = "buttons_before";
    public static final String buttons_behind = "buttons_behind";
    public static final String EDITING = "editing";
    public static final String CREATING = "creating";
    //
    public static final int ROW_PADDING_BOTTOM = 2;
    public static final int ROW_PADDING_LEFT = 4;
    public static final int ROW_PADDING_RIGHT = 4;
    public static final int ROW_PADDING_TOP = 2;
    public static final int WINDOW_BUTTON_HEIGHT = 36;
    //
    public static final int TEXT_AREA_LIMIT = 210;
    // Keys
    protected final String helpKey = "help";
    protected final String labelKey = "label";
    //
    protected CUJO cujo;
    protected Button submit;
    protected boolean newState;
    protected PropertyMetadataProvider metadataProvider;
    protected LayoutContainer panel;
    protected Map<CujoProperty, Field> bindingComponent = new HashMap<CujoProperty, Field>();
    protected Map<CujoProperty, List<Button>> bindingButtons = new HashMap<CujoProperty, List<Button>>();
    protected Map<CujoProperty, List<Field>> bindingPasswords = new HashMap<CujoProperty, List<Field>>();
    protected Map<CujoProperty, Label> bindingLabel = new HashMap<CujoProperty, Label>();
    protected List<CujoProperty> blackList = null;
    protected Map<CujoProperty, EditDialogTool> tools = null;
    protected Runnable afterSubmitCommand;
    protected List<CujoProperty> whiteList = null;
    /** Tato proměnná slouží pro automatickou detekci stavu a hodnot při práci s property, která nepovině rozšiřuje jiné property a je nutné ji umět přepnout zpět na DEFAULT value... */
    protected Map<CujoProperty, List<CujoProperty>> defaultPropertiesMap = new HashMap<CujoProperty, List<CujoProperty>>();
    protected int relations = 1;

    public EditDialog(CUJO cujo, boolean newState) {
        this.cujo = cujo;
        this.newState = newState;
        this.metadataProvider = ClientClassConfig.getInstance().getPropertyMedatata();
    }

    public abstract String translate(String parent, String name);

    protected LayoutContainer createPanelRows(CUJO cujo, int relations) {
        // editační komponenty
        LayoutContainer centerPanel = new LayoutContainer();
        boolean firstRow = true;
        for (CujoProperty cujoProperty : getWhiteList()) {
            if (getTools().get(cujoProperty) != null) {
                centerPanel.add(createPanelRow(cujo, cujoProperty, relations), new MarginData(firstRow ? ROWS_PADDING_TOP : 0, 0, 0, 0));
                firstRow = false;
            }
        }
        return centerPanel;
    }

    protected LayoutContainer initButtonsPanel() {
        HBoxLayout layout = new HBoxLayout();
        layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
        layout.setPack(BoxLayoutPack.END);
        layout.setPadding(new Padding(5));
        //
        LayoutContainer buttonsPanel = new LayoutContainer(layout);
        buttonsPanel.setStyleAttribute("border-bottom-style", "none");
        buttonsPanel.setStyleAttribute("border-left-style", "none");
        buttonsPanel.setStyleAttribute("border-right-style", "none");
        //
        return buttonsPanel;
    }

    protected LayoutContainer initCenterPanel(CUJO cujo, int relations) {
        return createPanelRows(cujo, relations);
    }

    @Override
    /** Metoda má tři klíčové kroky.
     *
     */
    protected void onRender(Element parent, int pos) {
        // berofe render commands
        initDefaultButtonTools();

        // render commands
        super.onRender(parent, pos);
        setIcon(newState ? Icons.Pool.add() : Icons.Pool.edit());
        setHeading(getHeadingTitle());
        setWidth(DIALOG_WIDTH);
        setMinWidth(DIALOG_WIDTH);
        setHeight(DIALOG_HEIGHT);
        setClosable(true);
        setModal(true);
        setLayout(new FillLayout());
        createFormPanel(cujo, relations);
        add(panel);

        // after render commands
        controllDefaultValues();
    }

    protected String getHeadingTitle() {
        return newState ? translate(getViewId(), CREATING) : translate(getViewId(), EDITING);
    }

    public void refreshHeadingTitle() {
        setHeading(getHeadingTitle());
    }

    /** Before call this method, you have to set cujo (data value) and tools (buttons, help)... */
    protected void createFormPanel(CUJO cujo, int relations) {
        // editační komponenty
        LayoutContainer centerPanel = initCenterPanel(cujo, relations);
        // tlačítka

        LayoutContainer buttonPanel = initButtonsPanel();
        addOthersButton(buttonPanel);
        addSubmitButton(buttonPanel);
        // sestavení do panelu
        panel = initFormPanel();

        panel.add(centerPanel);
        ((ContentPanel) panel).setBottomComponent(buttonPanel);
        ((ContentPanel) panel).getBottomComponent().setBorders(true);
    }

    protected LayoutContainer initFormPanel() {
        ContentPanel formPanel = new ContentPanel();
        formPanel.setBodyStyle("overflow: scroll; overflow-x: hidden; -ms-overflow-x: hidden; background-color: transparent;");
        formPanel.setBorders(false);
        formPanel.setHeaderVisible(false);
        formPanel.setBodyBorder(false);
        return formPanel;
    }

    protected LayoutContainer createPanelRow(CUJO cujo, CujoProperty cujoProperty, int relations) {
        LayoutContainer row = initRow();
        addLabel(row, cujoProperty);
        addButtonBefore(row, cujoProperty);
        Field component = addComponent(row, cujo, cujoProperty, getTools().get(cujoProperty), relations);

        int move = 0;
        move += addRelation(row, cujoProperty, component);
        move += addTranslation(row, cujoProperty, component);
        addButtonBehind(row, cujoProperty, component);
        addHelp(row, cujoProperty, move);

        return row;
    }

    protected boolean isPropertyMandatory(PropertyMetadata meta, CujoProperty prop) {
        return meta.isMandatory();
    }

    /** Před přidáním komponenty do panelu "row", jí nastavte šířku: component.setWidth(COMPONENT_WIDTH);. Jinak nebude správně zarovnaná nápověda.*/
    protected Field addComponent(LayoutContainer row, CUJO cujo, CujoProperty cujoProperty, EditDialogTool tool, int relations) {
        boolean editable = tool.isEditable();
        String displayProperty = tool.getDisplayField();

        // Metadata
        PropertyMetadata metadata = getMetadata(cujoProperty);
        boolean mandatory = isPropertyMandatory(metadata, cujoProperty);

        // Výběr komponenty
        Field component = null;
        if (ClientClassConfig.isCujoType(cujoProperty.getType())) {
            component = initCujoEditor(cujo, cujoProperty, metadata, tool, relations);
        } else if (cujoProperty.isTypeOf(Boolean.class)) {
            component = initBooleanEditor(cujoProperty, metadata, editable);
        } else if (cujoProperty.isTypeOf(java.util.Date.class)) {
            component = initDateEditor(cujoProperty, editable, metadata);
        } else if (cujoProperty.isTypeOf(List.class)) {
            component = initListEditor(cujoProperty, displayProperty, metadata);
        } else if (cujoProperty.isTypeOf(CEnum.class)) {
            component = initCEnumEditor(cujoProperty, editable, mandatory);
            // BigDecimal, BigInteger, Byte, Double, Float, Integer, Long, Short
        } else if (cujoProperty.isTypeOf(BigDecimal.class)
                || cujoProperty.isTypeOf(BigInteger.class)
                || cujoProperty.isTypeOf(Byte.class)
                || cujoProperty.isTypeOf(Double.class)
                || cujoProperty.isTypeOf(Float.class)
                || cujoProperty.isTypeOf(Integer.class)
                || cujoProperty.isTypeOf(Long.class)
                || cujoProperty.isTypeOf(Short.class)) {
            component = initNuberEditor(cujoProperty, metadata, editable, mandatory);
        } else {
            component = initStringEditor(cujoProperty, metadata, editable, mandatory);
        }

        bindField(cujoProperty, component);
        component.setWidth(COMPONENT_WIDTH);
        row.add(component);
        return component;
    }

    protected void addLabel(LayoutContainer row, CujoProperty cujoProperty) {
        // Metadata
        PropertyMetadata metadata = getMetadata(cujoProperty);
        boolean mandatory = isPropertyMandatory(metadata, cujoProperty);
        String mandatoryPrefix = mandatory ? "<span style=\"color: RoyalBlue;\" title=\"" + translate(getViewId(), "This-field-is-mandatory") + "\">* </span>" : "";

        Label label = initLabel();
        String message = getLabelText(cujoProperty);
        label.setText(mandatoryPrefix + message + ": ");
        //
        HBoxLayout layout = new HBoxLayout();
        layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
        layout.setPack(BoxLayoutPack.END);
        layout.setPadding(new Padding(3, 4, 0, 4));
        //
        LayoutContainer labelPanel = new LayoutContainer(layout);
        labelPanel.add(label);
        //
        VBoxLayoutData data = new VBoxLayoutData(new Margins(0, 4, 0, 4));
        labelPanel.add(label, data);
        labelPanel.setWidth(LABEL_WIDTH);
        row.add(labelPanel);
        bindLabel(cujoProperty, label);
    }

    protected String getLabelText(CujoProperty property) {
        return translate(labelKey, property.getName());
    }

    /** Panel, do kterého se přidává tlačítko má nastavený HBoxLayout. Pro přidání použijte např. panel.add(submit, new HBoxLayoutData(new Margins(0, 0, 0, 5))); */
    protected void addSubmitButton(LayoutContainer panel) {
        submit = initSubmitButton(newState,
                cujo);
        panel.add(submit, new HBoxLayoutData(new Margins(0, 0, 0, 5)));
    }

    /** Panel, do kterého se přidává tlačítko má nastavený HBoxLayout. Pro přidání použijte např. panel.add(button, new HBoxLayoutData(new Margins(0, 0, 0, 5))); */
    protected void addOthersButton(LayoutContainer panel) {
    }

    protected Button initSubmitButton(final boolean newState, final CUJO cujo) {
        Button submit = newOkButton(newState);

        submit.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {

                beforeValidation(cujo, newState);
                if (!isValid()) {
                    return;
                }
                afterValidation(cujo, newState);
            }
        });
        return submit;
    }

    protected void afterValidation(final CUJO cujo, final boolean newState) {
        beforeSave(cujo, newState);

        AsyncCallback callback = new AsyncCallback<ValidationMessage>() {

            @Override
            public void onFailure(Throwable caught) {
                // TODO:
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onSuccess(ValidationMessage msg) {
                changedData = msg == null;
                hide();
                if (changedData) {
                    afterSubmit(cujo);
                    return;
                }
            }
        };
        doSaveOrUpdate(cujo, newState, callback);
    }

    /** Tato metoda se provolá po úspěšném uložení... */
    protected void afterSubmit(CUJO cujo) {
        if (afterSubmitCommand != null) {
            afterSubmitCommand.run();
        }
    }

    /** Tato metoda zkopíruje hodnoty z komponent do cujo a vrátí true, pokud jsou všechny validní. Pokud nejsou validní, vypíše hlášku... */
    protected boolean isValid() {
        String validMessage = copyValuesFromComponent();
        if (validMessage != null) {
            MessageDialog.getInstance(translate(getViewId(), validMessage)).show();
            return false;
        }
        return true;
    }

    /** Tato metoda aktualizuje komponentu, které je závislá na defaultní hodnotě. */
    protected void refreshDefaultState(final CujoProperty propKey, final Field box) {
        if (cujo.get(propKey) == null) {
            box.setValue(getDefaultValue(getDefaultPropertiesMap().get(propKey)));
            getTools().get(propKey).setSave(false);
        } else {
            getTools().get(propKey).setSave(true);
        }
        checkPropertyState(box, propKey);
    }

    protected void doSaveOrUpdate(CUJO cujo, boolean newState, AsyncCallback callback) {
        getController().saveOrUpdate(cujo, newState, callback);
    }

    /** Tato metoda se zavolá přávě před spuštěním validací. Její účel je doplnit komponenty o hodnoty, které by jinak př validaci nebyly nalezeny a validace by neprošla... */
    protected void beforeValidation(CUJO cujo, boolean newState) {
    }

    /** Tato motoda je poslední, která operuje s daty - pak už jsou předány na server... Pokud nechcete přijít o promazání properties, které podléhají defaultnímu nastavení, při přepsání metody nezapomeňte přepsat rodičovské volání. */
    protected void beforeSave(CUJO cujo, boolean newState) {
        if (getDefaultPropertiesMap() != null && !getDefaultPropertiesMap().keySet().isEmpty()) {
            for (CujoProperty cujoProperty : getDefaultPropertiesMap().keySet()) {
                if (!getTools().get(cujoProperty).isSave()) {
                    cujo.set(cujoProperty, null);
                }
            }
        }
    }

    protected Button intitRelationButton(CujoProperty cujoProperty, LiveGridPanel liveGridPanel, Component component) {
        Object value = cujoProperty.getValue(cujo);
        CUJO cujoValue = null;
        if (value instanceof List) {
            if (!((List) value).isEmpty()) {
                value = ((List) value).get(0);
            }
        } else {
            cujoValue = (CUJO) value;
        }
        final LiveGridPanelDialog selectionDialog = new LiveGridPanelDialog(liveGridPanel, cujoValue, (Field) component);
        Button relationButton = new Button();
        relationButton.setIcon(Icons.Pool.selectionDialog());
        relationButton.setTitle(translate("", "find"));
        relationButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                selectionDialog.show();
            }
        });
        return relationButton;
    }

    protected Button initTranslationButton(CujoProperty cujoProperty, final Field component) {

        Button translationButton = new Button();
        translationButton.setIcon(Icons.Pool.detail());
        translationButton.setTitle(translate("", "createTranslate"));
        translationButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                // TODO...
                String translateKey = component.getValue().toString();
                loadSavedTranslate(translateKey);
            }
        });
        return translationButton;
    }

    public void loadSavedTranslate(final String key) {

        if (key == null || key.length()==0) {
            MessageDialog.getInstance(translate("default", "keyIsEmptyEnterNonemptyKey")).show();
            return;
        }

        final BaseModelData translate = new BaseModelData();
        translate.set("key", key);

        MessageControllerAsync.Pool.get().loadMessageByKey(key, new AsyncCallback<Map<String, String>>() {

            @Override
            public void onSuccess(Map<String, String> result) {
                if (result.isEmpty()) {
                    final MessageEditDialog messageEditDialog = new MessageEditDialog(translate, true) {

                        @Override
                        public String translate(String parent, String name) {
                            return EditDialog.this.translate(parent, name);
                        }

                        @Override
                        protected void refreshClientMessages(String key, String value) {
                            refreshTranslation(key, value);
                        }
                    };
                    messageEditDialog.setValues(result);
                    messageEditDialog.show();
                } else {
                    final MessageEditDialog messageEditDialog = new MessageEditDialog(translate, false) {

                        @Override
                        public String translate(String parent, String name) {
                            return EditDialog.this.translate(parent, name);
                        }

                        @Override
                        protected void refreshClientMessages(String key, String value) {
                            refreshTranslation(key, value);
                        }
                    };
                    messageEditDialog.setValues(result);
                    messageEditDialog.show();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                // TODO:
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
    }

    protected void refreshTranslation(String key, String value) {
    }

    /** Prozatin neni nastavena hodnota mandatory, kterou je mozne nadefinovat v tools... */
    protected CheckBox initBooleanEditor(CujoProperty cujoProperty, PropertyMetadata metadata, boolean editable) {
        CheckBox checkBox = new CheckBox();
        checkBox.setBoxLabel("");
        checkBox.setValue((Boolean) cujoProperty.getValue(cujo));
        checkBox.setToolTip(metadata.getDescription());
        checkBox.setEnabled(editable);
        return checkBox;
    }

    protected ComboBox<CEnum> initCEnumEditor(CujoProperty cujoProperty, boolean editable, boolean mandatory) {
        CPropertyEnum pe = (CPropertyEnum) cujoProperty;
        ComboBox<CEnum> cb = new ComboBox<CEnum>();
        cb.setEditable(editable);
        cb.setDisplayField(cujoProperty.getName());
        cb.setTriggerAction(TriggerAction.ALL);
        cb.setStore(pe.getItemStore());
        cb.setName(cujoProperty.getName());
        cb.setDisplayField(CEnum.name.getName());
        cb.setValue((CEnum) cujoProperty.getValue(cujo));
        cb.setAllowBlank(!mandatory);
        return cb;
    }

    protected DateField initDateEditor(CujoProperty cujoProperty, boolean editable, PropertyMetadata metadata) {
        DateField dateField = new DateField();
        dateField.setName(cujoProperty.getName());
        dateField.setEnabled(editable);
        dateField.setValue((java.util.Date) cujoProperty.getValue(cujo));
        dateField.setToolTip(metadata.getDescription());
        dateField.setAllowBlank(!isPropertyMandatory(metadata, cujoProperty));
        //dateField.setPropertyEditor(new DateTimePropertyEditor(CujoModel.DEFAULT_DATE_FORMAT)); // obsolete
        dateField.getPropertyEditor().setFormat(CujoModel.DEFAULT_DATE_FORMAT);
        return dateField;
    }

    protected Label initLabel() {
        Label label = new Label();
        label.setStyleAttribute("font-size", "13");
        label.addStyleName("x-form-item-label");
        return label;
    }

    protected TextField initStringEditor(CujoProperty cujoProperty, PropertyMetadata metadata, boolean editable, boolean mandatory) {
        TextField textField;
        if (cujoProperty.isTypeOf(String.class)) {
            textField = isSuggestedTextArea(metadata) ? createTextArea() : new TextField();
        } else {
            textField = new TextField();
        }
        textField.setName(cujoProperty.getName());
        textField.setEnabled(editable);
        String emptyText = translate("", "empty-text-" + cujoProperty.getName());
        if (!emptyText.startsWith("[") && !emptyText.endsWith("]")) {
            textField.setEmptyText(emptyText);
        }
        textField.setAllowBlank(!mandatory);
        textField.setMaxLength(metadata.getMaxLength());
        Object value = cujoProperty.getValue(cujo);
        textField.setValue(value != null ? value.toString() : "");
        textField.clearInvalid(); // TODO: proč to nefunguje? Při insert bych rád viděl položky jako validní.
        return textField;
    }

    protected Field initNuberEditor(final CujoProperty cujoProperty, PropertyMetadata metadata, boolean editable, boolean mandatory) {
        TextField<String> numberField = new TextField<String>();

        numberField.setValidator(new Validator() {

            private String availableChars = "0123456789,.-";

            @Override
            public String validate(Field<?> field, String value) {
                for (char c : value.toCharArray()) {
                    if (!availableChars.contains(c + "")) {
                        return translate(getViewId(), "Non-valid-number");
                    }
                }
                return null;
            }
        });

        numberField.setName(cujoProperty.getName());
        numberField.setEnabled(editable);
        String emptyText = translate("", "empty-text-" + cujoProperty.getName());
        numberField.setEmptyText(emptyText);
        numberField.setAllowBlank(!mandatory);
        numberField.setMaxLength(metadata.getMaxLength());

        Object value = cujoProperty.getValue(cujo);
        numberField.setValue(value != null ? value.toString() : "");
        
        numberField.clearInvalid(); // TODO: proč to nefunguje? Při insert bych rád viděl položky jako validní.
        return numberField;
    }

    /**
     * Aby bylo mozno vyuzit, je potreba v editacnim dialogu prepsat metodu addComponent() nasledovne:
     * protected Field addComponent(LayoutContainer row, CUJO cujo, CujoProperty cujoProperty, EditDialogTool tool, int relations) {
     *     Field component = null;
     *     if (cujoProperty.equals(CUser.password)) {
     *         component = initPasswordEditor(cujoProperty, tool);
     *         bindField(cujoProperty, component);
     *         final int passWidth = (COMPONENT_WIDTH / 2) - 10;
     *         component.setWidth(passWidth);
     *         row.add(component, new HBoxLayoutData(0, 20, 0, 0));
     *         Field reTypeComponent = initPasswordEditor(cujoProperty, tool);
     *         bindPasswords(cujoProperty, component, reTypeComponent);
     *         reTypeComponent.setWidth(passWidth);
     *         row.add(reTypeComponent, new HBoxLayoutData(0, 0, 0, 0));
     *     } else {
     *         component = super.addComponent(row, cujo, cujoProperty, tool, relations);
     *     }
     *     return component;
     * }
     *
     *
     */
    protected Field initPasswordEditor(CujoProperty cujoProperty, EditDialogTool tool) {
        // Metadata
        PropertyMetadata metadata = getMetadata(cujoProperty);
        boolean mandatory = metadata.isMandatory();
        boolean editable = tool.isEditable();
        Field component = initStringEditor(cujoProperty, metadata, editable, mandatory);
        ((TextField<String>) component).setPassword(true);
        ((TextField<String>) component).setMinLength(5);
        return component;
    }

    protected ComboBox initListEditor(CujoProperty cujoProperty, String displayProperty, PropertyMetadata metadata) {
        ComboBox<Cujo> cb = new ComboBox<Cujo>();

        // TODO: remove me...
        cb.getListView().setStyleAttribute("overflow", "auto");

        cb.setStore(new ListStore<Cujo>());
        List values = (List) cujoProperty.getValue(cujo);
        cb.getStore().add(values != null ? values : new ArrayList<Cujo>());
        // TODO:
        cb.setDisplayField(displayProperty);
        String names = "";
        for (Cujo item : cb.getStore().getModels()) {
            if (!names.equals("")) {
                names += ", ";
            }
            names += item.get(displayProperty).toString();
        }
        cb.setEmptyText(names);
        cb.setAllowBlank(!isPropertyMandatory(metadata, cujoProperty));
        return cb;
    }

    protected CujoProperty findSortProperty(Class cType) {
        CujoProperty sortableProperty = null;

        for (CujoProperty cujoProperty : CujoManager.find(cType).getProperties()) {
            if (getMetadata(cujoProperty).isSortable()) {
                return cujoProperty;
            }
        }

        return sortableProperty;
    }

    /** Pokud chcete, aby se vam do textoveho pole vypsala vase hodnota po vlozeni Cujo objektu, prepiste danemu objektu metodu toString()... */
    protected Field initCujoEditor(CUJO cujo, CujoProperty cujoProperty, PropertyMetadata metadata, EditDialogTool tool, int relations) {

        final Runnable onChange = tool.getAfterValeuChangeCommand();
        //
        ComboBox box = CujoBox.create(
                cujoProperty.getType(),
                findSortProperty(cujoProperty.getType()),
                onChange,
                tool.getController(),
                tool.getCrit(),
                relations);

        Cujo propertyCujoValue = (Cujo) cujoProperty.getValue(cujo);
        TextField component = box == null
                ? new CujoField(propertyCujoValue, cujoProperty, onChange)
                : box;

        component.setName(cujoProperty.getName());
//        component.setEnabled(editable);
        component.setEmptyText(metadata.getDescription());
        component.setAllowBlank(!isPropertyMandatory(metadata, cujoProperty));

        component.setValue(propertyCujoValue);
        component.clearInvalid(); // TODO: proč to nefunguje? Při insert bych rád viděl položky jako validní.

        return component;
    }

    /**
     * @return posun v px. Př.: není definovaná relace, posun = 22 px, je definovaná relace, posun = 0px.
     */
    protected int addRelation(LayoutContainer row, final CujoProperty cujoProperty, Field component) {
        LiveGridPanel liveGridPanel = getTools().get(cujoProperty).getRelation();
        if (ClientClassConfig.isCujoType(cujoProperty.getType())) {
            if (liveGridPanel == null) {
                liveGridPanel = new LiveGridPanelImpl() {

                    @Override
                    public String translate(String parentViewId, String name) {
                        return EditDialog.this.translate(parentViewId, name);
                    }

                    @Override
                    public Class getCujoType() {
                        return cujoProperty.getType();
                    }

                    @Override
                    public String registerId() {
                        // TODO:
                        return "";
                    }
                };
            }
            Button relationButton = intitRelationButton(cujoProperty, liveGridPanel, component);
            row.add(relationButton);
            bindButton(cujoProperty, relationButton);
            return 0;
        }
        return BUTTON_WIDTH;
    }

    /**
     * @return posun v px. Př.: není definovaná relace, posun = 22 px, je definovaná relace, posun = 0px.
     */
    protected int addTranslation(LayoutContainer row, CujoProperty cujoProperty, Field component) {
        if (getTools() != null
                && getTools().get(cujoProperty) != null
                && getTools().get(cujoProperty).isTranslation()) {
            Button translationButton = initTranslationButton(cujoProperty, component);
            row.add(translationButton);
            bindButton(cujoProperty, translationButton);
            return 0;
        }
        return BUTTON_WIDTH;
    }

    protected void addButtonBehind(LayoutContainer row, CujoProperty cujoProperty, Field component) {
        List<Button> buttons = getTools().get(cujoProperty).getButtonBehind();
        if (buttons != null && !buttons.isEmpty()) {
            for (Button button : buttons) {
                row.add(button);
                bindButton(cujoProperty, button);
            }
        }
    }

    protected void addButtonBefore(LayoutContainer row, CujoProperty cujoProperty) {
        List<Button> buttons = getTools().get(cujoProperty).getButtonBefore();
        if (buttons != null && !buttons.isEmpty()) {
            for (Button button : buttons) {
                row.add(button);
                bindButton(cujoProperty, button);
            }
        }
    }

    private void addHelp(LayoutContainer row, CujoProperty cujoProperty, int move) {
        String help = translate(helpKey, cujoProperty.getName());
        Image helpImage = Icons.Pool.help().createImage();
        helpImage.setTitle(help);
        helpImage.setPixelSize(ICON_WIDTH, ICON_HEIGHT);
        //
        HBoxLayout layout = new HBoxLayout();
        layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
        layout.setPack(BoxLayoutPack.END);
        //
        LayoutContainer helpPanel = new LayoutContainer(layout);
        helpPanel.setPixelSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        helpPanel.add(helpImage);
        row.add(helpPanel, new HBoxLayoutData(0, 0, 0, move));
    }

    protected LayoutContainer initRow() {
        LayoutContainer row = new LayoutContainer();

        HBoxLayout layout = new HBoxLayout();
        layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.TOP);
        layout.setPadding(new Padding(ROW_PADDING_TOP, ROW_PADDING_RIGHT, ROW_PADDING_BOTTOM, ROW_PADDING_LEFT));

        row.setLayout(layout);
        return row;
    }

    /** If a text maximal length (from meta-model) is great than constant TEXT_AREA_LIMIT,
     * that a component TextArea is suggested by default. */
    protected boolean isSuggestedTextArea(PropertyMetadata metadata) {
        boolean result = metadata.getMaxLength() >= TEXT_AREA_LIMIT
                && metadata.getMaxLength() < Integer.MAX_VALUE;
        return result;
    }

    /** Create a TextArea Component */
    protected TextArea createTextArea() {
        FloatTextArea floatTextArea = new FloatTextArea() {

            @Override
            protected void refreshParent(int height) {
                super.refreshParent(height);
                int superParentWidth = ((LayoutContainer) getParent().getParent()).getWidth();

                int parentWidth = ((LayoutContainer) getParent()).getWidth();
                int realWidth = superParentWidth == parentWidth
                        ? parentWidth - XDOM.getScrollBarWidth()
                        : parentWidth;

                int totalHeight = ROW_PADDING_TOP + height + ROW_PADDING_BOTTOM;
                getParent().setPixelSize(realWidth, totalHeight);

                ((LayoutContainer) getParent()).layout();
            }
        };
        return floatTextArea;
    }

    /** Copy values from components to a protected field called <code>cujo<code>
     * @see #cujo
     */
    protected String copyValuesFromComponent() {

        for (CujoProperty p : bindingComponent.keySet()) {
            Field w = bindingComponent.get(p);
            if (!w.isValid()) {
                return p.getName() + "-is-invalid";
            }
            if (newState || w.isDirty() || p.isTypeOf(List.class)) {
                Object value = p.isTypeOf(List.class) ? ((ComboBox) w).getStore().getModels() : w.getValue();
                //pokud je null preda se do CUJA, NULL hodnoty si řeší DB
                if (value != null) {
                    if (p.isTypeOf(Long.class)) {
                        value = Long.parseLong((String) value);
                    } else if (p.isTypeOf(Integer.class)) {
                        value = Integer.parseInt((String) value);
                    } else if (p.isTypeOf(Short.class)) {
                        value = Short.parseShort((String) value);
                    } else if (p.isTypeOf(BigDecimal.class)) {
                        value = new BigDecimal((String) value);
                    }
                }
                cujo.set(p, value);
            }
            if (bindingPasswords.get(p) != null) {
                Field pass1 = bindingPasswords.get(p).get(0);
                Field pass2 = bindingPasswords.get(p).get(1);
                final String pass1Value = ((TextField<String>) pass1).getValue();
                final String pass2Value = ((TextField<String>) pass2).getValue();
                if (!pass1Value.equals(pass2Value)) {
                    return p.getName() + "-not-equals";
                }
            }
        }

        return null;
    }

    /** Bind a Label to CujoProperty  */
    protected void bindLabel(CujoProperty property, Label label) {
        bindingLabel.put(property, label);
    }

    /** Bind a Label to CujoProperty  */
    protected void bindPasswords(CujoProperty property, Field pass1, Field pass2) {
        bindingPasswords.put(property, new ArrayList<Field>());
        bindingPasswords.get(property).add(pass1);
        bindingPasswords.get(property).add(pass2);
    }

    /** Bind a Field to CujoProperty  */
    protected void bindField(CujoProperty property, Field field) {
        if (bindingComponent.size() == 0) {
            field.focus();
        }
        bindingComponent.put(property, field);
    }

    protected <T extends Field> T findWidget(CujoProperty p) {
        Field result = bindingComponent.get(p);
        if (result == null) {
            throw new RuntimeException(translate(getViewId(), "no_widget_found") + p);
        }
        return (T) result;
    }

    /** Get property Metadata. */
    protected PropertyMetadata getMetadata(CujoProperty p) {
        return this.metadataProvider.getAlways(p);
    }

    /** New instance of OK button. */
    protected Button newOkButton(boolean newState) {
        Button result = new Button(newState ? translate(getViewId(), "create") : translate(getViewId(), "update"));
        result.setIcon(Icons.Pool.ok());
        return result;
    }

    protected List<CujoProperty> getBlackList() {
        if (blackList == null) {
            blackList = new ArrayList<CujoProperty>();
            addAbstractIgnorList();
        }

        return blackList;
    }

    protected void addAbstractIgnorList() {
        blackList.add(cujo.readProperties().findProperty("id"));
    }

    protected List<CujoProperty> getDefaultWhiteList() {
        List<CujoProperty> items = new ArrayList<CujoProperty>(Arrays.asList(cujo.readProperties().getProperties()));
        return items;
    }

    protected List<CujoProperty> getWhiteList() {
        whiteList = whiteList != null ? whiteList : getDefaultWhiteList();
        if (getBlackList() != null && !getBlackList().isEmpty()) {
            for (CujoProperty blackCujoProperty : getBlackList()) {
                if (!whiteList.remove(blackCujoProperty)) {
                    List<CujoProperty> toRemove = new ArrayList<CujoProperty>();
                    for (CujoProperty whiteCujoProperty : whiteList) {
                        if (whiteCujoProperty.getName().equals(blackCujoProperty.getName())) {
                            toRemove.add(whiteCujoProperty);
                        }
                    }
                    whiteList.removeAll(toRemove);
                }
            }
            whiteList.removeAll(getBlackList());
        }
        return whiteList;
    }

    public EditDialogTool getTool(CujoProperty cujoProperty) {
        return getTools().get(cujoProperty);
    }

    public Map<CujoProperty, EditDialogTool> getTools() {
        if (tools == null) {
            tools = new HashMap<CujoProperty, EditDialogTool>();

            for (CujoProperty property : getWhiteList()) {
                tools.put(property, new EditDialogTool());
            }
        }
        return tools;
    }

    protected void initRelationCujoBoxTool(CujoProperty property, LiveGridPanel relation, Class myClass, CujoProperty myClassDisplayProperty, Runnable command, boolean mandatory) {
        initRelationCujoBoxTool(property, relation, myClass, myClassDisplayProperty, command, null, mandatory);
    }

    protected void initRelationCujoBoxTool(CujoProperty property, LiveGridPanel relation, Class myClass, CujoProperty myClassDisplayProperty, Runnable command) {
        initRelationCujoBoxTool(property, relation, myClass, myClassDisplayProperty, command, null, true);
    }

    protected void initRelationCujoBoxTool(CujoProperty property, LiveGridPanel relation, Class myClass, CujoProperty myClassDisplayProperty, CCriterion criterionNLoad, Runnable command) {
        initRelationCujoBoxTool(property, relation, myClassDisplayProperty, command, null, true, criterionNLoad);
    }

    protected void initRelationCujoBoxTool(CujoProperty property, LiveGridPanel relation, Class myClass, CujoProperty myClassDisplayProperty, CCriterion criterionNLoad, Runnable command, boolean mandatory) {
        initRelationCujoBoxTool(property, relation, myClassDisplayProperty, command, null, mandatory, criterionNLoad);
    }

    protected void initRelationCujoBoxTool(CujoProperty property, LiveGridPanel relation, Class myClass, CujoProperty myClassDisplayProperty, Runnable command, LiveGridControllerAsync controller, boolean mandatory) {
        initRelationCujoBoxTool(property, relation, myClassDisplayProperty, command, controller, mandatory, null);

    }

    protected void initRelationCujoBoxTool(CujoProperty property, LiveGridPanel relation, CujoProperty myClassDisplayProperty, Runnable command, LiveGridControllerAsync controller, boolean mandatory, CCriterion criterionNLoad) {
        EditDialogTool tool = getTools().get(property);
        if (tool != null) {
            tool.setCujoEditor(true);
            tool.setMandatory(mandatory);
            tool.setRelation(relation);
            tool.setDisplayClass(property.getType());
            tool.setDisplayProperty(myClassDisplayProperty);
            tool.setAfterValeuChangeCommand(command);
            tool.setController(controller);
            tool.setCrit(criterionNLoad);
        }
    }

    protected void initDefaultButtonTools() {
        if (getDefaultPropertiesMap() != null && !getDefaultPropertiesMap().keySet().isEmpty()) {
            for (final CujoProperty newProperty : getDefaultPropertiesMap().keySet()) {

                EditDialogTool tool = getTools().get(newProperty);
                Button defaultButton = new Button("", Icons.Pool.go_to_default(), new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        Field field = bindingComponent.get(newProperty);
                        field.setValue(getDefaultValue(getDefaultPropertiesMap().get(newProperty)));
                        cujo.set(newProperty, null);
                        getTools().get(newProperty).setSave(false);
                        checkPropertyState(field, newProperty);
                    }
                });
                tool.getButtonBehind().add(defaultButton);
                bindButton(newProperty, defaultButton);
            }
        }
    }

    protected Map<CujoProperty, List<CujoProperty>> getDefaultPropertiesMap() {
        return defaultPropertiesMap;
    }

    protected void controllDefaultValues() {
        if (getDefaultPropertiesMap() != null && !getDefaultPropertiesMap().keySet().isEmpty()) {
            for (final CujoProperty propKey : getDefaultPropertiesMap().keySet()) {
                final Field field = bindingComponent.get(propKey);
                refreshDefaultState(propKey, field);
                if (field instanceof CheckBox) {
                    ((CheckBox) field).addListener(Events.OnClick, new Listener<BaseEvent>() {

                        @Override
                        public void handleEvent(BaseEvent be) {
                            doDefaultPropertyChange(field, propKey);
                        }
                    });

                } else if (field instanceof TextField) {
                    ((TextField<String>) field).addListener(Events.Change, new Listener<FieldEvent>() {

                        @Override
                        public void handleEvent(FieldEvent be) {
                            doDefaultPropertyChange(field, propKey);
                        }
                    });
                }
            }
        }
    }

    protected void doDefaultPropertyChange(Field field, CujoProperty propKey) {
        if (!getTools().get(propKey).isSave()) {
            getTools().get(propKey).setSave(true);
        }
        checkPropertyState(field, propKey);
    }

    protected Object getDefaultValue(List<CujoProperty> defaultPropertySecvence) {
        Object defaultValue = null;
        for (CujoProperty cujoProperty : defaultPropertySecvence) {
            if (defaultValue == null) {
                defaultValue = cujo.get(cujoProperty);
            } else {
                defaultValue = ((Cujo) defaultValue).get(cujoProperty);
            }
        }
        return defaultValue;
    }

    private void checkPropertyState(Field field, CujoProperty prop) {
        if (!field.isReadOnly()) {
            if (getTools().get(prop).isSave()) {
                field.setTitle(translate("", "own-value"));
            } else {
                field.setTitle(translate("", "default-value"));
            }
        }
    }

    protected void bindButton(CujoProperty cujoProperty, Button button) {
        if (bindingButtons.get(cujoProperty) == null) {
            bindingButtons.put(cujoProperty, new ArrayList<Button>());
        }
        bindingButtons.get(cujoProperty).add(button);
    }

    public abstract String getViewId();
}
