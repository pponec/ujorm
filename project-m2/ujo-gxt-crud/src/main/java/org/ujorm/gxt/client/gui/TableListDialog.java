/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Element;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.commons.Icons;

/**
 * The abstract table select dialog.
 * @author Ponec
 */
public class TableListDialog<CUJO extends Cujo> extends DataWindow<CUJO> {

    protected FormPanel panel;
    protected Field<CUJO> selectedItem;
    protected TablePanel<CUJO> tablePanel;

    @SuppressWarnings("unchecked")
    public TableListDialog(TablePanel<CUJO> tablePanel, Field<CUJO> selectedItem) {
        this.tablePanel = tablePanel;
        this.selectedItem = selectedItem;
        if (selectedItem != null) {
            tablePanel.setSelectMode(selectedItem, this);
        }
        add(tablePanel);
    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent,pos);
        //
        setIcon(Icons.Pool.selectionDialog());
        setClosable(true);
        setModal(true);
        setHeading(selectedItem!=null ? "Select: " + selectedItem.getFieldLabel() : "List");
        setWidth(600);
        setHeight(450);
        setLayout(new FillLayout());
    }

    @Override
    public void show() {
        if (selectedItem != null) {
            tablePanel.setSelectMode(selectedItem, this);
        }
        super.show();
    }

}
