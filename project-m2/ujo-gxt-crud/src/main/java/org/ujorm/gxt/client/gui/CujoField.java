/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.commons.Icons;

/** A client field relation.
 * @see CujoBox
 */
public class CujoField extends AdapterField {

    private static final String fieldWidth = "270px";
    private Cujo value;
    private TextField<String> textField = new TextField<String>();
    private Button button = new Button(" ");
    private String displayField = "name";

    static public class MyContainer extends HtmlContainer {

        public MyContainer() {
            super(getTableBody());
        }

        private static String getTableBody() {
            String result
                    = "<table cellspacing='0' style='width:300px;'><tr class='x-form-text'>"
                    + "<td class='cujo-field' style='width:" + fieldWidth + ";'></td>"
                    + "<td class='cujo-btn'></td>"
                    + "</tr></table>";
            return result;
        }
    }

    @SuppressWarnings({"unchecked"})
    public CujoField(TablePanel tablePanel) {
        super(new MyContainer());
        MyContainer container = (MyContainer) super.getWidget();

        container.add(textField, "td.cujo-field");
        container.add(button, "td.cujo-btn");

        textField.setEnabled(false);
        textField.setEmptyText(" ");
        textField.setWidth("100%");

        final TableListDialog selectionDialog = new TableListDialog(tablePanel, (Field) this);
        button.setWidth("100%");
        if (false) {
            button.setIcon(Icons.Pool.selectionDialog()); // toodo: how to center the icon?
        }
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                selectionDialog.show();
            }
        });
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public boolean isDirty() {
        //return textField.isDirty(); // TODO
        return true;
    }

    @Override
    public boolean isValid(boolean preventMark) {
        if (isEnabled()) {
            boolean valid = getAllowBlank()
                ? true
                : (value!=null)
                ;
            if (!valid && !preventMark) {
                String msg = getMessages().getInvalidText();
                textField.markInvalid(msg!=null ? msg : "The field is mandatory");
            }
            return valid;
        } else {
            return true;
        }
    }

    @Override
    public void setValue(Object aValue) {
        this.value = (Cujo) aValue;
        if (value != null) {
            textField.setValue((String) value.get(displayField));
        } else {
            textField.setValue(null);
        }
    }

    /** Default value is 'name' */
    public String getDisplayField() {
        return displayField;
    }

    public void setDisplayField(String displayField) {
        this.displayField = displayField;
    }

    public void setDisplayProperty(CujoProperty property) {
        this.displayField = property.getName();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        button.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return button.isEnabled();
    }

    /** Set an image icon */
    public void setIcon(AbstractImagePrototype icon) {
        button.setIcon(icon);
    }

    /**
     * Sets whether a field is valid when its value length = 0 (default to true).
     *
     * @param allowBlank true to allow blanks, false otherwise
     */
    public void setAllowBlank(boolean allowBlank) {
        textField.setAllowBlank(allowBlank);
    }

    /**
     * Gets whether a field is valid when its value length = 0 (default to true).
     * @param allowBlank true to allow blanks, false otherwise
     */
    public boolean getAllowBlank() {
        return textField.getAllowBlank();
    }

}
