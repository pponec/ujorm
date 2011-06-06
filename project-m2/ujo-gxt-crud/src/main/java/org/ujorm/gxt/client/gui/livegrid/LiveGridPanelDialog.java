package org.ujorm.gxt.client.gui.livegrid;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Element;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.commons.Icons;
import org.ujorm.gxt.client.controller.TableControllerAsync;
import org.ujorm.gxt.client.gui.editdialog.EditWindow;

public class LiveGridPanelDialog<CUJO extends Cujo> extends EditWindow<CUJO> {

    protected FormPanel panel;
    protected Field<CUJO> selectedItem;
    protected TableControllerAsync service;

    public LiveGridPanelDialog(LiveGridPanel liveGridPanel) {
        this(liveGridPanel, null);
    }

    public LiveGridPanelDialog(LiveGridPanel liveGridPanel, Field<CUJO> selectedItem) {
        this.selectedItem = selectedItem;
        if (selectedItem != null) {
            liveGridPanel.setSelectMode(selectedItem, this);
        }
        add(liveGridPanel);
    }

    public LiveGridPanelDialog(LiveGridPanel liveGridPanel, CUJO value, Field<CUJO> component) {
        this.selectedItem = component;
        if (component != null) {
            liveGridPanel.setSelectMode(component, (AbstractCujo) value, this);
        }
        add(liveGridPanel);
    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        //
        setIcon(Icons.Pool.selectionDialog());
        setClosable(true);
        setModal(true);
        setHeading(selectedItem != null ? "Select" : "List");
        setWidth(600);
        setHeight(450);
        setLayout(new FillLayout());
    }

    @Override
    public void onShow() {
        super.onShow();
    }

    /** Returns a generic service. */
    protected TableControllerAsync getService() {
        if (service == null) {
            service = TableControllerAsync.Util.getInstance();
        }
        return service;
    }
}
