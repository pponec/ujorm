/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujorm.gxt.client.gui.livegrid;

import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.gui.editdialog.EditDialog;
import org.ujorm.gxt.client.gui.livegrid.LiveGridPanel.LiveGridReloadCommand;

/**
 *
 * @author Dobroslav Pelc
 */
public abstract class LiveGridPanelImpl<CUJO extends AbstractCujo> extends LiveGridPanel<CUJO> {

    protected String EDIT_DIALOG = "edit_dialog";
    protected EditDialog<CUJO> editDialog;

    public LiveGridPanelImpl() {
    }

    @Override
    public void showEditDialog(CUJO selectedItem, boolean newState) {
        editDialog = initEditDialog(selectedItem, newState);
        editDialog.show();
    }

    /** VytvoĹ™Ă­ defaultnĂ­ editaÄŤnĂ­ formulĂˇĹ™ */
    protected EditDialog<CUJO> initEditDialog(CUJO selectedItem, boolean newState) {
        return new EditDialog<CUJO>(
                selectedItem,
                newState) {

            @Override
            public String translate(String parent, String name) {
                return LiveGridPanelImpl.this.translate(parent, name);
            }

            @Override
            public String getViewId() {
                return EDIT_DIALOG;
            }

            @Override
            protected void afterSubmit(CUJO cujo) {
                LiveGridReloadCommand command = getLiveGridReloadCommand(newState);
                command.run();
            }
        };
    }

    protected LiveGridReloadCommand getLiveGridReloadCommand(boolean newState) {
        return new LiveGridReloadCommand(newState ? LiveGridReloadCommand.SUCCESSFUL_SAVED : LiveGridReloadCommand.SUCCESSFUL_UPDATED);
    }

    @Override
    protected void beforeCreate() {
    }

    @Override
    protected void afterCreate() {
    }

    @Override
    protected void beforeUpdate() {
    }

    @Override
    protected void afterUpdate() {
    }

    @Override
    protected void beforeDelete() {
    }

    @Override
    protected void afterDelete() {
    }
}
