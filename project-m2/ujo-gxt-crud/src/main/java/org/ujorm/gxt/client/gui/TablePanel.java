/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import java.util.EnumSet;
import org.ujorm.gxt.client.ClientCallback;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.cquery.CCriterion;
import org.ujorm.gxt.client.cquery.CQuery;
import java.util.List;
import java.util.Map;
import org.ujorm.gxt.client.CLoginRedirectable;
import org.ujorm.gxt.client.CMessageException;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.ao.Permissions;
import org.ujorm.gxt.client.commons.Icons;
import org.ujorm.gxt.client.controller.TableController;
import org.ujorm.gxt.client.controller.TableControllerAsync;
import org.ujorm.gxt.client.tools.MessageDialog;
import org.ujorm.gxt.client.tools.Tools;
import static org.ujorm.gxt.client.commons.KeyCodes.*;

/** Generic CRUD panel for the table formet */
abstract public class TablePanel<CUJO extends Cujo> extends LayoutContainer implements TablePanelOperations<CUJO>, CLoginRedirectable {

    static TableControllerAsync s;
    protected ContentPanel cpPanel;
    protected ContentPanel cpTable;
    private ContentPanel cpButtons;
    private int deleteActionType = TableController.DELETE_AUTO;
    protected Grid<CUJO> grid;
    protected PagingToolBar gridToolBar;
    protected CUJO selectedItem = null;
    protected Long selectedId = null;
    protected List<CUJO> selectedItems = null;
    protected Field<CUJO> selectedComponent = null;
    protected Window selectedDialog;
    protected String buttonSelect = "Select";
    protected String buttonCreate = "New";
    protected String buttonUpdate = "Update";
    protected String buttonDelete = "Delete";
    protected String buttonCopy = "Copy";
    protected String buttonQuit = "Quit";
    protected int pageSize = 30;
    private CQuery<CUJO> query;
    private CCriterion<CUJO> advancedCrn = null;
    private boolean selectFirstOnNextLoad = true;
    private int selectedItemIndex = 0;
    /** Roles for permission to Display the display panel. */
    private Permissions displayPermission;
    /** Roles for permission to Modify data from the panel. */
    private Permissions modifyPermission;

    /** Constuctor to use a default query implemented by the method. */
    public TablePanel() {
        this(null);
    }

    /** Constuctor to use a selected model of the table. */
    public TablePanel(CQuery<CUJO> query) {
        this.setLayout(new FitLayout());
        this.query = query;
    }

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

    /** Create Loader */
    protected PagingLoader<PagingLoadResult<ModelData>> createLodader() {
        RpcProxy<PagingLoadResult<Cujo>> proxy = new RpcProxy<PagingLoadResult<Cujo>>() {

            @Override
            public void load(final Object loadConfig, final AsyncCallback<PagingLoadResult<Cujo>> callback) {
                final AsyncCallback<PagingLoadResult<Cujo>> callback2 = new AsyncCallback<PagingLoadResult<Cujo>>() {

                    @Override
                    public void onFailure(final Throwable caught) {
                        if (CMessageException.isSessionTimeout(caught)) {
                            redirectToLogin();
                        }
                        callback.onFailure(caught);
                        GWT.log("Error TablePanel loading ", caught);
                        grid.unmask();
                        MessageDialog.getInstance("Unsupported operation").show();
                    }

                    @Override
                    public void onSuccess(final PagingLoadResult<Cujo> result) {
                        callback.onSuccess(result);
                        onTableLoad((PagingLoadResult<CUJO>) result);
                    }
                };
                getService().getDbRows(getQuery(), (PagingLoadConfig) loadConfig, callback2);
            }
        };

        // loader
        final PagingLoader<PagingLoadResult<ModelData>> loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
        loader.setRemoteSort(true);
        return loader;
    }

    /** Is it a select mode? */
    protected boolean isSelectMode() {
        return selectedComponent != null;
    }

    /** Is allowed to select a many table rows? */
    protected boolean isMultiSelectMode() {
        boolean result = selectedComponent != null
                && MultiField.class.equals(selectedComponent.getClass());
        return result;
    }

    /** Returns the selectedDialog title. */
    protected String getTableTitle() {
        String result = Tools.getSimpleName(getQuery().getType());
        return result;
    }

    @Override
    protected void onRender(com.google.gwt.user.client.Element parent, int index) {
        super.onRender(parent, index);
        setScrollMode(Scroll.AUTO);

        cpPanel = new ContentPanel();
        cpPanel.setHeading(getTableTitle());
        cpPanel.setHeaderVisible(getTableTitle() != null);
        cpPanel.setSize("98%", "450px");
        cpPanel.setLayout(new BorderLayout());
        cpPanel.setIcon(Icons.Pool.table());
        cpPanel.setCollapsible(false);
        cpPanel.setAnimCollapse(false);

        // Table:
        gridToolBar = new PagingToolBar(pageSize);
        gridInitialization();

        // Buttons
        buttonInitialization();
    }

    /** Grid initialization */
    protected void gridInitialization() {
        final PagingLoader<PagingLoadResult<ModelData>> loader = createLodader();

        ListStore<CUJO> store = new ListStore<CUJO>(loader);
        store.addStoreListener(new StoreListener<CUJO>() {

            @Override
            public void storeDataChanged(StoreEvent<CUJO> se) {
                super.storeDataChanged(se);
                if (selectFirstOnNextLoad) {
//                        selectFirstOnNextLoad = false;
                    DeferredCommand.addCommand(new Command() {

                        @Override
                        public void execute() {
                            if (grid.getSelectionModel().getSelectedItem() == null) {
                                selectRow();
                            }
                        }
                    });
                }
            }
        });

        gridToolBar.bind(loader);

        ColumnModel cm = getQuery().getColumnModel();

        grid = new Grid<CUJO>(store, cm);
        grid.setStateId(getClass().getName());
        grid.setStateful(true);

        if (isSelectMode() && !isMultiSelectMode()) {
            grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        grid.addListener(Events.Attach, new Listener<GridEvent<Cujo>>() {

            @Override
            public void handleEvent(GridEvent<Cujo> be) {
                PagingLoadConfig config = new BasePagingLoadConfig();
                config.setOffset(0);
                config.setLimit(pageSize);

                Map<String, Object> state = grid.getState();
                if (state.containsKey("offset")) {
                    int offset = (Integer) state.get("offset");
                    int limit = (Integer) state.get("limit");
                    config.setOffset(offset);
                    config.setLimit(limit);
                }
                if (state.containsKey("sortField")) {
                    config.setSortField((String) state.get("sortField"));
                    config.setSortDir(SortDir.valueOf((String) state.get("sortDir")));
                }
                loader.load(config);
            }
        });
        grid.addListener(Events.OnDoubleClick, new Listener<GridEvent>() {

            @Override
            public void handleEvent(GridEvent be) {
                if (isSelectMode()) {
                    selectItem(getAllSelectedItems());
                } else {
                    editItem(getFirstSelectedItem(), gridToolBar);
                }
            }
        });
        grid.addListener(Events.OnKeyDown, new Listener<GridEvent>() {

            @Override
            public void handleEvent(GridEvent ge) {
                CUJO item = getFirstSelectedItem();
                int keyCode = ge.getEvent().getKeyCode();
                if (keyCode == F7) {
                    createItem(item, gridToolBar);
                    stop(ge);
                } else if (keyCode == F5) {
                    copyItem(gridToolBar);
                    stop(ge);
                } else if (keyCode == F2) {
                    editItem(item, gridToolBar);
                    stop(ge);
                } else if (keyCode == ENTER) {
                    if (isSelectMode()) {
                       selectItem(getAllSelectedItems());
                    } else {
                       editItem(item, gridToolBar);
                    }
                    stop(ge);
                } else if (keyCode == DELETE || keyCode == F8) {
                    deleteItem(grid.getSelectionModel().getSelectedItems(), gridToolBar);
                    stop(ge);
                } else if (buttonQuit != null && keyCode == BACKSPACE) {
                    doGoBack();
                    stop(ge);
                }
            }

            protected void stop(GridEvent be) {
                be.stopEvent();
                be.setCancelled(true);
            }
        });
        grid.setLoadMask(true);
        grid.setBorders(true);
        //grid.setAutoExpandColumn("forum");


        // ------------------------------------------


        cpTable = new ContentPanel();
        cpTable.setHeaderVisible(false);
        VBoxLayout westLayout = new VBoxLayout();
        westLayout.setPadding(new Padding(5));
        westLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        cpTable.setLayout(westLayout);
        //BorderLayoutData west = new BorderLayoutData(LayoutRegion.WEST, 150, 100, 250);
        BorderLayoutData west = new BorderLayoutData(LayoutRegion.CENTER);

        west.setMargins(new Margins(5));
        west.setSplit(true);

        VBoxLayoutData tableData = new VBoxLayoutData(0, 0, 0, 0);
        tableData.setFlex(10);

        cpTable.add(grid, tableData);
        cpPanel.add(cpTable, west);
        cpPanel.setBottomComponent(gridToolBar);
    }

    /** Button initialization */
    protected void buttonInitialization() {
        cpButtons = new ContentPanel();
        cpButtons.setHeaderVisible(false);
        cpButtons.setLayout(new FitLayout());
        //cpButtons.add(new Html("<p style=\"padding:10px;color:#556677;font-size:11px;\">Select a configuration on the left</p>"));
        BorderLayoutData center = new BorderLayoutData(LayoutRegion.EAST, 150, 100, 250);
        center.setMargins(new Margins(5));
        cpPanel.add(cpButtons, center);
        add(cpPanel, new FlowData(0));

        LayoutContainer c = new LayoutContainer();
        VBoxLayout layout = new VBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        c.setLayout(layout);
        createButtons(c, gridToolBar);
        cpButtons.add(c);
    }

    @Override
    public void selectRow() {
        findModelById();
        if (selectedItem != null) {
            int index = grid.getStore().indexOf(selectedItem);
            selectedItemIndex = index == -1 ? 0 : index;
        }
        if (grid.getStore().getCount()>0) {
            grid.getSelectionModel().select(selectedItemIndex, false);
            grid.getView().focusRow(selectedItemIndex);
        }
    }

    private void findModelById() {
        if (selectedItem != null) {
            Long selectedId = selectedItem.get("id");
            for (CUJO cujo : grid.getStore().getModels()) {
                Long id = cujo.get("id");
                if (id.equals(selectedId)) {
                    setUpdateValue(cujo);
                    return;
                }
            }
        }
    }

    /** Selected Item from an Update */
    @Override
    public void setUpdateValue(CUJO cujo) {
        this.selectedItem = cujo;
    }

    /** Selected Item from an Update */
    @Override
    public void setUpdateValue(Long cujoId) {
        this.selectedId = cujoId;
    }

    /** Action after table loading */
    protected void onTableLoad(PagingLoadResult<CUJO> result) {
    }

    protected Button addButton(String label, AbstractImagePrototype icon, LayoutContainer c) {

        if (label == null) {
            return null;
        }

        Button result = null;
        boolean selectlabel = label.equals(buttonSelect);

        if (buttonQuit == label
                ? selectedDialog != null
                : (isSelectMode() == selectlabel || buttonCreate == label)) {
            result = new Button(label);
            result.setIcon(icon);

            VBoxLayoutData ld = new VBoxLayoutData(new Margins(0, 0, 5, 0));
            c.add(result, ld);
        }

        return result;
    }

    protected int getDeleteActionType() {
        return deleteActionType;
    }

    protected void setDeleteActionType(int deleteActionType) {

        switch (deleteActionType) {
            case TableController.DELETE_AUTO:
            case TableController.DELETE_LOGICAL:
            case TableController.DELETE_PHYSICAL:
                this.deleteActionType = deleteActionType;
                break;
            default:
                throw new RuntimeException("Unsupported parameter: " + deleteActionType);
        }
    }

    /** Create buttons */
    protected void createButtons(LayoutContainer buttonContainer, final PagingToolBar toolBar) {

        Button buttonS = addButton(buttonSelect, Icons.Pool.select(), buttonContainer);
        Button buttonC = addButton(buttonCreate, Icons.Pool.add(), buttonContainer);
        Button buttonY = addButton(buttonCopy, Icons.Pool.copy(), buttonContainer);
        Button buttonU = addButton(buttonUpdate, Icons.Pool.edit(), buttonContainer);
        //tton buttonE = addButton(buttonDetail, Icons.Pool.detail(), buttonContainer);
        Button buttonD = addButton(buttonDelete, Icons.Pool.delete(), buttonContainer);
        //c.add(new Button(button4Text), new VBoxLayoutData(new Margins(0)));
        Button buttonG = addButton(buttonQuit, Icons.Pool.goBack(), buttonContainer);

        // ------------------------
        // SELECT action:
        if (buttonS != null) {
            buttonS.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    selectItem(getAllSelectedItems());
                }
            });
        }
        // CREATE button action:
        if (buttonC != null) {
            buttonC.setToolTip("[F7]");
            buttonC.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    createItem(getFirstSelectedItem(), toolBar);
                }
            });
        }
        // COPY button action:
        if (buttonY != null) {
            buttonY.setToolTip("[F5]");
            buttonY.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    copyItem(toolBar);
                }
            });
        }
        // UPDATE button action:
        if (buttonU != null) {
            buttonU.setToolTip("[F2]");
            buttonU.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    editItem(getFirstSelectedItem(), toolBar);
                }
            });
        }
        // DELETE button action:
        if (buttonD != null) {
            buttonD.setToolTip("[F8], [DEL]");
            buttonD.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    deleteItem(grid.getSelectionModel().getSelectedItems(), toolBar);
                }
            });
        }

        // Go back
        if (buttonG != null) {
            buttonG.setToolTip("[BackSpace]");
            buttonG.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    if (selectedDialog != null) {
                        doGoBack();
                    }
                }
            });
        }
    }

    protected void doGoBack() {
        selectedDialog.hide();
        selectedDialog.clearState();
    }

    protected void copyItem(final PagingToolBar toolBar) {
        if (!isActionPanelEnabled()) { 
            return; 
        }
        selectedItem = getFirstSelectedItem();
        if (selectedItem == null) {
            MessageDialog.getInstance("No row to copy was selected.").show();
            return;
        }

        final TableEditDialog editDialog = createTableEditDialog(getFirstSelectedItem(), true, true);
        if (editDialog!=null) {
            editDialog.setTablePanelOperations(this);
            editDialog.addListener(Events.BeforeHide, new Listener<WindowEvent>() {
                @Override
                public void handleEvent(WindowEvent be) {
                    boolean ok = editDialog.isChangedData();
                    if (ok) {
                        toolBar.refresh();
                    }
                }
            });
            editDialog.show();
        }
    }

    protected boolean selectItem(List<CUJO> selectedItems) {
        if (selectedItems.size() > 0 && selectedComponent != null) {
            if (isMultiSelectMode()) {
                ((MultiField) selectedComponent).setValues(selectedItems);
            } else {
                selectedItem = selectedItems.get(0);
                selectedComponent.setValue(selectedItem);
            }
            doGoBack();
        } else {
            MessageDialog.getInstance("No row was selected.").show();
            return true;
        }
        return false;
    }

    protected void createItem(CUJO firstSelectedItem, final PagingToolBar toolBar) {
        if (!isActionPanelEnabled()) { 
            return; 
        }
        final TableEditDialog editDialog = createTableEditDialog(firstSelectedItem, true, false);
        if (editDialog!=null) {
            editDialog.setTablePanelOperations(this);
            editDialog.addListener(Events.BeforeHide, new Listener<WindowEvent>() {

                @Override
                public void handleEvent(WindowEvent be) {
                    boolean ok = editDialog.isChangedData();
                    if (ok) {
                        toolBar.refresh();
                    }
                }
            });
            editDialog.show();
        }
    }

    protected void deleteItem(final List<CUJO> selectedItems, final PagingToolBar toolBar) {
        if (!isActionPanelEnabled()) { 
            return; 
        }
        if (selectedItems.isEmpty()) {
            MessageDialog.getInstance("No row to delete was selected.").show();
            return;
        }
        if (selectedItems.size() > 0) {
            String message = "The " + selectedItems.size() + " row(s) will be deleted.";
            final MessageDialog d = new MessageDialog(message);
            d.setButtons(Dialog.OKCANCEL);
            d.addListener(Events.BeforeHide, new Listener<WindowEvent>() {

                @Override
                @SuppressWarnings("unchecked")
                public void handleEvent(WindowEvent be) {
                    boolean ok = d.isClickedOk(be);
                    if (ok) {

                        getService().delete(selectedItems, getDeleteActionType(), new ClientCallback(TablePanel.this) {

                            @Override
                            public void onSuccess(Object result) {
                                toolBar.refresh();
                            }
                        });
                    }
                }
            });
            d.show();
        }
    }

    protected void editItem(CUJO firstSelectedItem, final PagingToolBar toolBar) {
        if (!isActionPanelEnabled()) { 
            return; 
        }
        this.selectedItem = firstSelectedItem;
        if (selectedItem == null) {
            MessageDialog.getInstance("No row to update was selected.").show();
            return;
        }
        final TableEditDialog editDialog = createTableEditDialog(selectedItem, false, false);
        if (editDialog!=null) {
            editDialog.setTablePanelOperations(this);
            editDialog.addListener(Events.BeforeHide, new Listener<WindowEvent>() {

                @Override
                public void handleEvent(WindowEvent be) {
                    boolean ok = editDialog.isChangedData();
                    if (ok) {
                        toolBar.refresh();
                    }
                }
            });
            editDialog.show();
        }
    }

    /** Returns the first selected item or null if no rows is selected */
    protected CUJO getFirstSelectedItem() {
        final List<CUJO> result = getAllSelectedItems();
        return result.size() > 0 ? result.get(0) : null;
    }

    /** Returns all selected item or null if no rows is selected */
    protected List<CUJO> getAllSelectedItems() {
        final List<CUJO> result = selectedItems = grid.getSelectionModel().getSelectedItems();
        return result;
    }

    /** Define a list of the Table columns. If the result will be null or empty than table show all rows. */
    protected CujoProperty[] createTableColumns() {
        return null;
    }

    /** The method can return a default database query. <br/>
     * Sample of an implementation:
     * <pre>
     *    return CQuery.newInstance(ClientDomain.class, createTableColumns());
     * </pre>
     */
    protected CQuery<? super CUJO> createDefaultQuery() {
        return null;
    }

    /** The method to reload table row before modification.
     * If ID value is null than the method returns the NULL value.
     */
    protected CQuery<CUJO> getEditQuery(boolean newState, CUJO editable) {
        return newState
            ? null
            : getEditQuery(editable)
            ;
    }

    /** The method to reload table row before modification. 
     * If ID value is null than the method returns the NULL value.
     */
    protected CQuery<CUJO> getEditQuery(CUJO editable) {
        CQuery<? super CUJO> result = null;

        if (editable != null) {
            CujoProperty property = editable.readProperties().findProperty("id", false);
            if (property == null) {
                property = editable.readProperties().getProperties()[0];
            }

            // Add ID identifier:
            final Object value = property.getValue(editable);
            if (value!=null) {
                CCriterion crn = CCriterion.where(property, value);
                result = new CQuery<CUJO>(getQuery());
                result.addCriterion(crn);
            }
        }        
        return (CQuery<CUJO>) result;
    }

    /** The method can change a query. */
    @SuppressWarnings("unchecked")
    protected void setQuery(CQuery<? super CUJO> query) {
        this.query = (CQuery<CUJO>) query;
    }

    /** Returns a parameter query from constructor or the default query. */
    @SuppressWarnings("unchecked")
    protected CQuery getQuery() {
        if (query == null) {
            query = (CQuery<CUJO>) createDefaultQuery();
            if (advancedCrn != null) {
                CCriterion crn = query.getCriterion();
                crn = crn != null ? crn.and(advancedCrn) : advancedCrn;
                query.setCriterion(crn);
            }
        }
        return query;
    }

    /** Reload table */
    protected void reloadTable() {
        setQuery(null); // force new build of the CQuery
        gridToolBar.refresh();
    }

    /** Get advanced criterion. */
    public CCriterion<CUJO> getAdvancedCrn() {
        return advancedCrn;
    }

    /** Add a criterion to the default query. */
    @SuppressWarnings("unchecked")
    public void addCriterion(CCriterion<? super CUJO> advancedCrn) {
        this.advancedCrn = (CCriterion<CUJO>) advancedCrn;
    }

    protected TableControllerAsync getService() {
        if (s == null) {
            s = TableControllerAsync.Util.getInstance();
        }
        return s;
    }


    /** Create a new edit dialog and make initialization */
    @SuppressWarnings("unchecked")
    protected TableEditDialog<CUJO> createTableEditDialog(final CUJO selectedItem, boolean newState, boolean clone) {
        final TableEditDialog<CUJO> editDialog = createDialogInstance();
        if (editDialog != null) {
            CUJO cujo = newState ? editDialog.createItem() : selectedItem;
            if (clone) {
                copy(selectedItem, cujo);
            }
            editDialog.init(cujo, newState, getEditQuery(newState, selectedItem));
        }
        return editDialog;
    }

    /** Create a new instance of the Edit dialog, no initializaton. */
    abstract protected <T extends TableEditDialog<CUJO>> T createDialogInstance();

    /** Copy all properties of the same type exclude the names 'id', 'createdBy', 'modifiedBy' */
    protected void copy(Cujo from, Cujo to) {
        if (from != null) {
            for (CujoProperty p : from.readProperties()) {
                String name = p.getName();
                if (!"id".equals(name)
                        && !"createdBy".equals(name)
                        && !"modifiedBy".equals(name)) {
                    p.copy(from, to);
                }
            }
        }
    }

    /** Test to display permission */
    public boolean istDisplayAllowed(EnumSet roles) {
        return displayPermission==null ? true : displayPermission.isAllowed(roles);
    }

    public Permissions getDisplayPermission() {
        return displayPermission;
    }

    public <T extends TablePanel> T setRoles(Enum ... roles) {
        this.displayPermission = new Permissions(roles);
        return (T) this;
    }

    /** Test to modify permission */
    public boolean isModifyAllowed(EnumSet roles) {
        return modifyPermission==null ? true : modifyPermission.isAllowed(roles);
    }

    public Permissions getModifyPermission() {
        return modifyPermission;
    }

    public <T extends TablePanel> T setModifyRoles(Enum ... roles) {
        this.modifyPermission = new Permissions(roles);
        return (T) this;
    }

    /** Returns a default button panel */
    protected ContentPanel getActionPanel() {
        return cpButtons;
    }

    protected boolean isActionPanelEnabled() {
        return true;
    }

    @Override
    public void redirectToLogin() {
        GWT.log("Session time out", null);
    }


}
