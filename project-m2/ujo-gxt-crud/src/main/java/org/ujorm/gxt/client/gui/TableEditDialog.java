/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import java.util.LinkedHashMap;
import org.ujorm.gxt.client.CEnum;
import org.ujorm.gxt.client.PropertyMetadata;
import org.ujorm.gxt.client.PropertyMetadataProvider;
import org.ujorm.gxt.client.ClientClassConfig;
import org.ujorm.gxt.client.CPropertyEnum;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoModel;
import org.ujorm.gxt.client.CujoProperty;
import java.util.List;
import java.util.Map;
import org.ujorm.gxt.client.CLoginRedirectable;
import org.ujorm.gxt.client.ClientCallback;
import org.ujorm.gxt.client.commons.Icons;
import org.ujorm.gxt.client.controller.TableControllerAsync;
import org.ujorm.gxt.client.cquery.CQuery;

/**
 * The abstract edit dialog.
 * @author Ponec
 */
abstract public class TableEditDialog<CUJO extends Cujo> extends DataWindow<CUJO> implements CLoginRedirectable {

    /** A text maximal length (from meta-model) to creating a TextArea
     * instead of TextField component.  */
    protected static final int TEXT_AREA_LIMIT = 180;
    /** The form panel */
    protected FormPanel panel;
    /** Insert Action */
    protected boolean newState;
    /** Is dialog data loaded? */
    private boolean dataLoaded;
    protected Map<CujoProperty, Field> binding = new LinkedHashMap<CujoProperty, Field>();
    /** The client business object to edit. */
    protected CUJO cujo;
    /** A result of a BO saving. */
    protected PropertyMetadataProvider metadataProvider;
    protected TablePanelOperations<CUJO> operations;
    protected CQuery<CUJO> editQuery;

    /** Public Constructor */
    public TableEditDialog(CUJO cujo, boolean newState) {
        init(cujo, newState, null);
    }

    /** Public Constructor with an query to edit the latest data from database. 
     * Call the init(...) method before use the dialog.
     */
    public TableEditDialog() {
    }

    /** Create new Item. */
    abstract public CUJO createItem();

    /** Public Constructor with an query to edit the latest data from database. */
    final public void init(CUJO cujo, boolean newState, CQuery<CUJO> editQuery) {
        this.cujo = cujo;
        this.newState = newState;
        this.editQuery = editQuery;
        this.metadataProvider = ClientClassConfig.getInstance().getPropertyMedatata();
    }

    /** Public Constructor with an query to edit the latest data from database. */
    final public <T extends TableEditDialog> T init$(CUJO cujo, boolean newState, CQuery<CUJO> editQuery) {
        init(cujo, newState, editQuery);
        return (T) this;
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (operations != null) {
            operations.setUpdateValue(getSaveCujoId());
            operations.selectRow();
        }
    }

    /** Override the method by the onCreateWidgets()
     * @see #onCreateWidgets(com.google.gwt.user.client.Element, int)
     */
    @Override
    final protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        //
        setIcon(newState ? Icons.Pool.add() : Icons.Pool.edit());
        setHeading(newState ? "New" : "Edit");
        setClosable(true);
        setModal(true);
        //setWidth(500);
        //setHeight(350);
        setLayout(new FillLayout());

        // --------------

        onCreateWidgets(parent, pos);

        if (editQuery!=null) {
            TableControllerAsync.Util.getInstance().getCujoList(editQuery, new ClientCallback(TableEditDialog.this) {
                @Override
                public void onSuccess(Object result) {
                    List<CUJO> cujos = (List<CUJO>) result;

                    if (cujos.size() == 1) {
                        cujo = cujos.get(0);
                    } else {
                        GWT.log("WARNING: The EditQuery returns: " + cujos.size() + " items!", null);
                    }
                    copyValuesToComponent();
                }
            });
        } else {
            // WorkAround for the Component.select();
            TableControllerAsync.Util.getInstance().pink(new ClientCallback(TableEditDialog.this) {
                @Override
                public void onSuccess(Object result) {
                    copyValuesToComponent();
                }
            });
        }
    }

    /** Create Widgets - overwrite the method */
    abstract protected void onCreateWidgets(Element parent, int pos);

    /** Bind a Field to CujoProperty  */
    protected void bind(CujoProperty property, Field field) {
        if (binding.isEmpty()) {
            field.focus();
        }
        binding.put(property, field);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Field> T findWidget(CujoProperty p) {
        Field result = binding.get(p);
        if (result == null) {
            throw new RuntimeException("No widget was found for property: " + p);
        }
        return (T) result;
    }

    /** Get property Metadata. */
    protected PropertyMetadata getMetadata(CujoProperty p) {
        return this.metadataProvider.getAlways(p);
    }

    /** Create an empty ComboBox, no store */
    @SuppressWarnings({"unchecked"})
    protected ComboBox createCombo(CujoProperty p) {
        PropertyMetadata metadata = getMetadata(p);
        String label = metadata.getSideLabel();

        ComboBox cb = new ComboBox();
        cb.setFieldLabel(label);
        cb.setDisplayField(p.getName());
        cb.setTriggerAction(TriggerAction.ALL);
        cb.setName(p.getName());
        cb.setDisplayField("name");
        cb.setAllowBlank(!metadata.isMandatory());

        panel.add(cb);
        bind(p, cb);
        return cb;
    }

    /** Create widget by property */
    @SuppressWarnings("unchecked")
    protected Field createWidget(CujoProperty<? super CUJO, ?> p) {
        return createWidget(p, (Field) null, (String) null);
    }

    /** Create a box relation by property */
    @SuppressWarnings("unchecked")
    protected Field createBoxRelation(CujoProperty<? super CUJO, ?> p, CujoBox box) {
        return createWidget(p, box);
    }

    /** Create a widget relation by property */
    @SuppressWarnings("unchecked")
    protected Field createWidgetRelation(CujoProperty<? super CUJO, ?> p, TablePanel tablePanel) {
        return createWidget(p, new CujoField(tablePanel));
    }

    /** Create a widget relation by property */
    @SuppressWarnings("unchecked")
    protected Field createWidgetRelation(CujoProperty<? super CUJO, ?> p, TablePanel tablePanel, CujoProperty<?, ?> display) {
        Field field = createWidgetRelation(p, tablePanel);

        if (display != null) {
            ((CujoField) field).setDisplayField(display.getName());
        }

        return field;
    }

    /** Create widget by property */
    @SuppressWarnings("unchecked")
    protected Field createWidget(CujoProperty<? super CUJO, ?> p, Field widget) {
        return createWidget(p, widget, (String) null);
    }

    /** Create widget by property */
    @SuppressWarnings("unchecked")
    protected Field createWidget(CujoProperty<? super CUJO, ?> p, Field widget, String aLabel) {
        PropertyMetadata metadata = getMetadata(p);
        String label = aLabel != null ? aLabel : metadata.getSideLabel();

        if (widget != null) {
            widget.setName(p.getName());
            widget.setFieldLabel(label);
            if (widget.isEnabled()) {
                widget.setEmptyText(metadata.getDescription());
            }
            if (widget.getClass().equals(TextArea.class)) {
                ((TextArea) widget).setAllowBlank(!metadata.isMandatory());
                ((TextArea) widget).setMaxLength(metadata.getMaxLengthExt());
            }
            if (widget.getClass().equals(CujoField.class)) {
                ((CujoField) widget).setAllowBlank(!metadata.isMandatory());
            }
            widget.clearInvalid();

            panel.add(widget);
            bind(p, widget);
            return widget;
        }

        if (p.isTypeOf(Boolean.class)) {
            CheckBox checkBox = new CheckBox();

            checkBox.setName(p.getName());
            checkBox.setFieldLabel(label);
            checkBox.setBoxLabel(metadata.getColumnLabel());
            checkBox.setToolTip(metadata.getDescription());

            panel.add(checkBox);
            bind(p, checkBox);
            return checkBox;
        }

        if (p.isTypeOf(java.util.Date.class)) {
            DateField dateField = new DateField();

            dateField.setName(p.getName());
            dateField.setFieldLabel(label);
            dateField.setToolTip(metadata.getDescription());

            DateTimeFormat dtFormat = p.isTypeOf(java.sql.Date.class)
                    ? CujoModel.DEFAULT_DAY_FORMAT
                    : CujoModel.DEFAULT_DATE_FORMAT;
            dateField.getPropertyEditor().setFormat(dtFormat);
            dateField.setAllowBlank(!metadata.isMandatory());

            panel.add(dateField);
            bind(p, dateField);
            return dateField;
        }


        if (p.isTypeOf(CEnum.class)) {
            CPropertyEnum pe = (CPropertyEnum) p;

            ComboBox<CEnum> cb = new ComboBox<CEnum>();
            cb.setFieldLabel(label);
            cb.setDisplayField(p.getName());
            cb.setTriggerAction(TriggerAction.ALL);
            cb.setStore(pe.getItemStore());
            cb.setName(p.getName());
            cb.setDisplayField(CEnum.name.getName());
            cb.setAllowBlank(!metadata.isMandatory());

            panel.add(cb);
            bind(p, cb);
            return cb;
            //
        } else {
            TextField textField;
            if (p.isTypeOf(String.class)) {
                textField = isSuggestedTextArea(metadata)
                        ? createTextArea()
                        : new TextField();
            } else {
                textField = new TextField();
            }

            textField.setName(p.getName());
            textField.setFieldLabel(label);
            textField.setAllowBlank(!metadata.isMandatory());
            if (textField.isEnabled()) {
                textField.setEmptyText(metadata.getDescription());
            }
            if (metadata.getMaxLength() > 0) {
                textField.setMaxLength(metadata.getMaxLength());
            }

            panel.add(textField);
            bind(p, textField);
            return textField;
        }
    }

    /** Copy values from components to a protected field called <code>cujo<code>
     * @see #cujo
     */
    @SuppressWarnings("unchecked")
    protected boolean copyValuesFromComponent() {
        boolean result = true;
        for (CujoProperty p : binding.keySet()) {
            Field w = binding.get(p);
            if (!w.isValid()) {
                result = false;
                continue;
            }
            if (newState || w.isDirty()) {
                try {
                    copyValueFromComponent(p, w.getValue());
                } catch (Throwable e) {
                    result = false;
                    setFieldErrorMessage(p, w, e);
                }
            }
        }
        if (result) {
            GWT.log("Cujo from component: " + cujo + " is assigned", null);
        }
        return result;
    }

    /** Copy the one value per one component */
    protected void copyValueFromComponent(CujoProperty p, Object value) throws Exception {

        if (p.isTypeOf(Long.class)) {
            value = Long.parseLong((String) value);
        } else if (p.isTypeOf(Integer.class)) {
            value = Integer.parseInt((String) value);
        } else if (p.isTypeOf(Short.class)) {
            value = Short.parseShort((String) value);
        } else if (p.isTypeOf(java.sql.Date.class)) {
            if (value != null && "java.util.Date".equals(value.getClass().getName())) {
                value = new java.sql.Date(((java.util.Date) value).getTime());
            }
        }
        cujo.set(p, value);
    }

    /** Set field error message: */
    protected void setFieldErrorMessage(CujoProperty p, Field w, Throwable e) {
        if (e!=null && e.getMessage()!=null) {
            w.markInvalid("Invalid value: " + e.getMessage());
        } else {
            w.markInvalid("Invalid value");
        }
    }

    /** Copy values from components to a protected field called <code>cujo<code>
     * @see #cujo
     */
    @SuppressWarnings("unchecked")
    protected boolean copyValuesToComponent() {
        dataLoaded = false;

        for (CujoProperty p : binding.keySet()) {
            Field w = binding.get(p);
            if (w != null) {
                copyValueToComponent(w, p, p.getValue(cujo));
            }
        }

        dataLoaded = true;
        GWT.log("Cujo to Component: " + cujo, null);
        return true;
    }

    /** Copy the one value per one component. */
    protected void copyValueToComponent(Field w, CujoProperty p, Object value) throws NumberFormatException {
        if (w instanceof CujoField) {
            ((CujoField)w).setRawValue((Cujo)value);
        } else if(p.isTypeOf(String.class)) {
            w.setRawValue(value != null ? value.toString() : null);
        } else {
            w.setValue(value);
            w.clearInvalid();
        }
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
        return createTextArea(100, true);
    }

    /** Create a TextArea Component */
    protected TextArea createTextArea(int height, boolean enabled) {
        TextArea result = new TextArea();
        result.setHeight(height);
        result.setEnabled(enabled);
        return result;
    }

    /** New instance of OK button. */
    protected Button newOkButton(boolean newState) {
        Button result = new Button(newState ? "Create" : "Update");
        result.setIcon(Icons.Pool.ok());
        return result;
    }

    /** New instance of OK button. */
    protected Button newQuitButton(boolean newState) {
        Button result = new Button("Quit");
        result.setIcon(Icons.Pool.goBack());
        return result;
    }

    protected Long getSaveCujoId() {
        if (cujo == null || cujo.get("id") == null) {
            cujo.set("id", -1l);
        }
        return cujo.get("id");
    }

    public void setTablePanelOperations(TablePanelOperations<CUJO> operations) {
        this.operations = operations;
    }

    /** Is a panel data are loaded */
    public boolean isDataLoadedToComponent() {
        return dataLoaded;
    }

    /** Find a field by the property. */
    public <T extends Field> T findField(CujoProperty p) {
        return (T) binding.get(p);
    }

    /** Have got the dialog a NEW STATE? */
    public boolean isNewState() {
        return newState;
    }

    @Override
    public void redirectToLogin() {
        GWT.log("Session time out", null);
    }

}
