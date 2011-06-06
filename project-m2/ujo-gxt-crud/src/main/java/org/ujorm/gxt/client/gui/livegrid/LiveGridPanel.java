package org.ujorm.gxt.client.gui.livegrid;

import org.ujorm.gxt.client.gui.MultiField;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.commons.Icons;
import org.ujorm.gxt.client.controller.TableController;
import org.ujorm.gxt.client.gui.CujoBox;
import org.ujorm.gxt.client.tools.MessageDialog;

/**
 * LiveGridPanel is CRUD (create, read, update, delete) implemented on LiveGrid. As new are here buttons and keyListener for CRUD action.
 *
 * @author Dobroslav Pelc
 */
public abstract class LiveGridPanel<CUJO extends AbstractCujo> extends LiveGridSearch<CUJO> implements LiveGridButton<CUJO> {

    // button labels
    public static final String BUTTON_SELECT_LABEL = "select";
    public static final String BUTTON_CREATE_LABEL = "create";
    public final String BUTTON_UPDATE_LABEL = "update";
    public final String BUTTON_DELETE_LABEL = "delete";
    public final String BUTTON_RELOAD_LABEL = "reload";
    public final String BUTTON_DEFAULT_LABEL = "default_view";
    public final String BUTTON_SAVE_LABEL = "save_view";
    public final String BUTTON_BACK_LABEL = "go_back";
    // buttons
    private Button buttonSelect;
    private Button buttonCreate;
    private Button buttonUpdate;
    private Button buttonDelete;
    private Button buttonReload;
    private LayoutContainer buttonsContainer = new LayoutContainer();
    private LayoutContainer buttonPanelTop;
    protected boolean selectEnable = false;

    public LiveGridPanel() {
        VIEW_TABLE = "liveGridPanel";
        initButtons();
    }

    public void reloadBrowser() {
        mask(translate("loading"));
        setReload(true);
        reload = true;
        grid.getStore().getLoader().load();
    }

    @Override
    public void initButtons() {
        // prepare buttons
        buttonSelect = new Button(translate(VIEW_TABLE, BUTTON_SELECT_LABEL), Icons.Pool.select(), buttonActionSelect());
        setCrudId(buttonSelect, "ButtonSelect");

        buttonUpdate = new Button(translate(VIEW_TABLE, BUTTON_UPDATE_LABEL), Icons.Pool.edit(), buttonActionUpdate());
        setCrudId(buttonUpdate, "ButtonUpdate");

        buttonDelete = new Button(translate(VIEW_TABLE, BUTTON_DELETE_LABEL), Icons.Pool.delete(), buttonActionDelete());
        setCrudId(buttonDelete, "ButtonDelete");

        buttonReload = new Button(translate(VIEW_TABLE, BUTTON_RELOAD_LABEL), Icons.Pool.repeat(), buttonActionReload());
        setCrudId(buttonReload, "ButtonReload");

        //prepare button containers
        buttonsContainer.setLayout(new BorderLayout());
        buttonPanelTop = new LayoutContainer();
        VBoxLayoutData ld = new VBoxLayoutData(new Margins(0, 0, 5, 0));
        // Button on top
        buttonPanelTop.setStyleAttribute("background-color", "white");
        VBoxLayout topLayout = new VBoxLayout();
        topLayout.setPadding(new Padding(5));
        topLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        buttonPanelTop.setLayout(topLayout);
        if (selectEnable) {
            buttonPanelTop.add(buttonSelect, ld);
        }
        addCreateButton(ld);
        buttonPanelTop.add(buttonUpdate, ld);
        buttonPanelTop.add(buttonDelete, ld);
        buttonPanelTop.add(buttonReload, ld);
        // Button on bottom
        LayoutContainer buttonPanelBottom = new LayoutContainer();
        buttonPanelBottom.setStyleAttribute("background-color", "white");
        VBoxLayout bottomlayout = new VBoxLayout();
        bottomlayout.setPadding(new Padding(5));
        bottomlayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        buttonPanelBottom.setLayout(bottomlayout);
        buttonsContainer.add(buttonPanelTop, new BorderLayoutData(LayoutRegion.CENTER));
        buttonsContainer.add(buttonPanelBottom, new BorderLayoutData(LayoutRegion.SOUTH, 60));
    }

    protected void addCreateButton(VBoxLayoutData ld) {
        buttonCreate = new Button(translate(VIEW_TABLE, BUTTON_CREATE_LABEL), Icons.Pool.add(), buttonActionCreate());
        setCrudId(buttonCreate, "ButtonCreate");
        buttonPanelTop.add(buttonCreate, ld);
    }

    public void addButton(Button button) {
        insertButton(button, buttonPanelTop.getItemCount());
    }

    public void insertButton(Button button, int index) {

        buttonPanelTop.insert(button, index, new VBoxLayoutData(new Margins(0, 0, 5, 0)));

    }

    @Override
    public SelectionListener<ButtonEvent> buttonActionBack() {
        // TODO
        return null;
    }
    private int deleteActionType = TableController.DELETE_AUTO;

    protected int getDeleteActionType() {
        return deleteActionType;
    }

    @Override
    public SelectionListener<ButtonEvent> buttonActionDelete() {
        // TODO
        return new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final List<CUJO> selectedItems = getSelectedItems();
                if (selectedItems.size() == 0) {
                    MessageDialog.getInstance(translate(VIEW_TABLE, "no_row_to_delete_selected")).show();
                    return;
                }
                beforeDelete();

                if (selectedItems.size() > 0) {
                    mask(translate(VIEW_TABLE, "uploading"));
                    String message = selectedItems.size() + " " + translate(VIEW_TABLE, "x_rows_will_be_deleted");
                    final MessageDialog d = new MessageDialog(message);
                    d.addListener(Events.BeforeHide, new Listener<WindowEvent>() {

                        @Override
                        public void handleEvent(WindowEvent be) {
                            boolean ok = d.isClickedOk(be);
                            if (ok) {
                                // TODO: nemaze, protoze neni definovana servisa...
                                for (final AbstractCujo selectedItem : selectedItems) {
                                    getController().delete(selectedItem, new AsyncCallback<String>() {

                                        @Override
                                        public void onSuccess(String result) {
                                            if (result == null) {
                                                new LiveGridReloadCommand(LiveGridReloadCommand.SUCCESSFUL_DELETED).run();
                                            } else {
                                                Info.display(translate("Error"), translate(result));
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            // TODO:
                                            throw new UnsupportedOperationException("Not supported yet.");
                                        }
                                    });
                                }
                            }
                        }
                    });
                    d.addListener(Events.BeforeHide, new Listener<WindowEvent>() {

                        @Override
                        public void handleEvent(WindowEvent be) {
                            // do nothing
                        }
                    });
                    d.show();
                    //odmaskovani komponenty po zavreni message dialogu
                    unmask();
                }
            }
        };
    }

    @Override
    public SelectionListener<ButtonEvent> buttonActionReload() {
        return new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                reloadBrowser();
            }
        };
    }

    @Override
    public SelectionListener<ButtonEvent> buttonActionUpdate() {
        return new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                beforeUpdate();
                CUJO selectedItem = LiveGridPanel.super.getSelectedItem();
                if (selectedItem == null) {
                    MessageDialog.getInstance(translate(VIEW_TABLE, "no_row_to_update_selected")).show();
                    return;
                }
                showEditDialog(selectedItem, false);
            }
        };
    }

    @Override
    public SelectionListener<ButtonEvent> buttonActionCreate() {
        return new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final CUJO newCujo;
                beforeCreate();
                newCujo = (CUJO) getNewCujo();
                // promazĂˇnĂ­ vĹˇech pĹ™edchozĂ­ch hodnot
                for (CujoProperty cujoProperty : newCujo.readProperties().getProperties()) {
                    newCujo.set(cujoProperty, null);
                }
                if (newCujo == null) {
                    return;
                }
                showEditDialog(newCujo, true);
            }
        };
    }

    @Override
    public SelectionListener<ButtonEvent> buttonActionSelect() {
        return new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                doSelectItem();
            }
        };
    }

    public void doSelectItem() {
        selectedItems = getSelectedItems();
        if (selectedItems.size() > 0 && selectedComponent != null) {
            if (isMultiSelectMode()) {
                ((MultiField<CUJO>) selectedComponent).setValues(selectedItems);
            } else {
                selectedItem = selectedItems.get(0);
                if (selectedComponent instanceof CujoBox) {
                    selectedComponent.setValue(selectedItem);
                } else if (selectedComponent instanceof ComboBox) {
                    ComboBox<CUJO> box = (ComboBox<CUJO>) selectedComponent;
                    box.getStore().add(selectedItem);
                } else {
                    selectedComponent.setValue(selectedItem);
                }
            }
            selectedDialog.hide();
            selectedDialog.clearState();
        } else {
            MessageDialog.getInstance(translate(VIEW_TABLE, "no_row_selected")).show();
        }
    }

    @Override
    public void onChange(CUJO cujo) {
        if (selectEnable) {
            doSelectItem();
        } else {
            if (cujo != null) {
                showEditDialog(cujo, false);
            }
        }
    }

    @Override
    public LayoutContainer getButtonsContainer() {
        if (buttonsContainer == null) {
            initButtons();
        }
        return buttonsContainer;
    }

    public class LiveGridReloadCommand implements Runnable {

        public static final String SUCCESSFUL_DELETED = "successful_deleted";
        public static final String SUCCESSFUL_RELOADED = "successful_reloaded";
        public static final String SUCCESSFUL_SAVED = "successful_saved";
        public static final String SUCCESSFUL_UPDATED = "successful_updated";
        private String message;

        public LiveGridReloadCommand(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            mask(translate(VIEW_TABLE, "uploading"));
            Info.display(translate(VIEW_TABLE, "message") + ":", "<ul><li>" + translate(VIEW_TABLE, message) + "</li></ul>");
            if (message.equals(SUCCESSFUL_SAVED)) {
                afterCreate();
            } else if (message.equals(SUCCESSFUL_UPDATED)) {
                afterUpdate();
            } else if (message.equals(SUCCESSFUL_DELETED)) {
                afterDelete();
            }
            LiveGridPanel.this.reloadBrowser();
        }
    }

    public abstract void showEditDialog(CUJO cujo, boolean newState);

    /** Defatult action before create */
    abstract protected void beforeCreate();

    /** Defatult action after create */
    abstract protected void afterCreate();

    /** Defatult action before update */
    abstract protected void beforeUpdate();

    /** Defatult action after update */
    abstract protected void afterUpdate();

    /** Defatult action before delete */
    abstract protected void beforeDelete();

    /** Defatult action after delete */
    abstract protected void afterDelete();

    /** Is it a select mode? */
    protected boolean isSelectMode() {
        return selectedComponent != null;
    }

    /** Is allowed to select a many table rows? */
    protected boolean isMultiSelectMode() {
        return selectedComponent != null && MultiField.class.equals(selectedComponent.getClass());
    }
    protected Field<CUJO> selectedComponent = null;
    protected List<CUJO> selectedItems = null;
    protected CUJO selectedItem = null;
    protected Window selectedDialog;

    /**
     * Table in the Select mode or the Multi-select
     * @param selectedComponent
     * @param selectedDialog
     */
    public void setSelectMode(Field<CUJO> selectedComponent, Window selectedDialog) {
        this.selectedComponent = selectedComponent;
        this.selectedItem = selectedComponent != null ? selectedComponent.getValue() : null;
        this.selectedDialog = selectedDialog;
    }

    /**
     * Table in the Select mode or the Multi-select
     * @param selectedValue
     * @param selectedDialog
     */
    public void setSelectMode(Field selectedComponent, CUJO selectedValue, Window selectedDialog) {
        this.selectedComponent = selectedComponent;
        this.selectedItem = selectedValue;
        this.selectedDialog = selectedDialog;
    }

    /** Returns the first selected item or null if no rows is selected */
    protected CUJO getFirstSelectedItem() {
        List<CUJO> result = getSelectedItems();
        return result.size() > 0 ? result.get(0) : null;
    }
}
