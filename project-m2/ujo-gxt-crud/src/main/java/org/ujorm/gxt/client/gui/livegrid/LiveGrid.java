/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */
package org.ujorm.gxt.client.gui.livegrid;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LiveToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.ClientClassConfig;
import org.ujorm.gxt.client.CujoManager;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.commons.Icons;
import org.ujorm.gxt.client.controller.LiveGridControllerAsync;
import org.ujorm.gxt.client.cquery.CQuery;
import org.ujorm.gxt.client.gui.livegrid.MovesOperations;

/**
 * LiveGrid is a grid with the "live" tools to show only bit of database table.
 *
 * Tips:
 *  - To display only selected columns initializ variable availableColumns in he constructor
 *  - to display only selected columns init variable availableColumns in constructor
 *  - the order selected columns is dependent on the order items in list availableColumns
 *
 * @author Dobroslav Pelc
 */
public abstract class LiveGrid<CUJO extends AbstractCujo> extends LayoutContainer implements SortOperation<CUJO>, MovesOperations<CUJO> {

    public static final String CRUD_ID = "crud-id";
    public static final String ID_SLG_FRAME = "LiveGridFrame";
    public static final String ID_SLG_GRID_PANEL = "GridPanel";
    public static final String SLG_BUTTONS_PANEL = "ButtonsPanel";
    public static final String SLG_TREE_PANEL = "TreePanel";
    public static String VIEW_TABLE = "";
    protected String TABLE_TITLE = "Table-title";
    protected String TREE_PANEL_TITLE = "Filters";
    protected CujoProperty<CUJO, String> orderProperty = null;
    public SortDir deafultSort = SortDir.ASC;
    protected ToolBar bottomBar;
    protected Grid<CUJO> grid;
    //
    protected ContentPanel buttonsFrame;
    protected ContentPanel treeFrame;
    //
    protected Boolean reload = true;
    protected Integer relationsCount = Integer.valueOf(1);
    // view settings
    protected AbstractImagePrototype icon;
    protected List<CujoProperty> availableColumns;
    private List<String> disableColumnNames;
    private List<String> availableColumnsNamesToShow;
    protected String autoExpandColumnName = null;
    protected int autoExpandColumnIndex = 1;
    // data processing
    protected CQuery<CUJO> query;
    protected Integer defaultOffset;
    protected Component focusComponent;

    public LiveGrid() {
    }

    /** KlientskĂˇ tĹ™Ă­da, kterou tento LG reprezentuje */
    @Override
    public abstract Class<CUJO> getCujoType();

    /** UdĂˇlost pĹ™i otevĹ™enĂ­ / vytvoĹ™enĂ­ itemu. */
    public abstract void onChange(CUJO cujo);

    public abstract LayoutContainer getButtonsContainer();

    /** Objekt klientskĂ© tĹ™Ă­dy, kterou tento LG reprezentuje */
    protected CUJO getNewCujo() {
        return (CUJO) ClientClassConfig.getInstance().createCujo(getCujoType());
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        initComponents();
    }

    protected void initComponents() throws UnsupportedOperationException {
        FitLayout layout = new FitLayout();
        setLayout(layout);

        // controll values and generate default values...
        availableColumns = createDefaultAvailableColumns();
        availableColumnsNamesToShow = createDefaultAvailableColumnNamesToShow(getAvailableColumns());
        availableColumnsNamesToShow = removeDisableColumnNames(availableColumnsNamesToShow, getDisableColumnNames());
        autoExpandColumnName = createDefaultAutoExpandColumnName(autoExpandColumnName, availableColumnsNamesToShow, autoExpandColumnIndex);


        //
        List<ColumnConfig> colConfig = initColumnConfig(getAvailableColumns(), autoExpandColumnName, autoExpandColumnIndex);
        //
        ColumnModel columnModel = initColumnModel(colConfig);
        // proxy
        RpcProxy<PagingLoadResult<CUJO>> proxy = initProxy();
        // loader
        PagingLoader<PagingLoadResult<CUJO>> loader = initLoader(proxy, getAutoExpandColumnName(), deafultSort);
        // store
        ListStore<CUJO> store = new ListStore<CUJO>(loader) {
        }; // column model
        //
        grid = initGrid(store, columnModel);
        // init LG frame
        ContentPanel panel = initFrame();
        //
        LayoutContainer buttonsPanel = getButtonsContainer();
        LayoutContainer treePanel = getTreePanelContainer();
        //
        addGrid(panel, initGridPanel(grid, buttonsPanel != null, treePanel != null));
        //
        addButtons(panel, buttonsPanel);
        //
        addTreePanel(panel, treePanel);
        //
        add(panel, new FitData(0));
    }

    protected void addButtons(ContentPanel panel, LayoutContainer buttonsPanel) {
        if (buttonsPanel != null) {
            setCrudId(buttonsPanel, SLG_BUTTONS_PANEL);
            buttonsFrame = initButtonsPanel();
            addButtonsToButtonsPanel(buttonsFrame, buttonsPanel);
            addButtonsPanelToButtonsFrame(panel, buttonsFrame);
        }
    }

    protected void addButtonsPanelToButtonsFrame(ContentPanel panel, ContentPanel buttonsFrame) {
        BorderLayoutData east = new BorderLayoutData(LayoutRegion.EAST, 150, 100, 250);
        east.setMargins(new Margins(0));
        east.setSplit(true);
        east.setCollapsible(true);
        panel.add(buttonsFrame, east);
    }

    protected void addButtonsToButtonsPanel(ContentPanel buttonsFrame, LayoutContainer buttonsPanel) {
        buttonsPanel.setBorders(false);
        buttonsPanel.setStyleAttribute("overflow", "hidden");
        buttonsFrame.add(buttonsPanel);
    }

    protected ContentPanel initButtonsPanel() {
        ContentPanel buttonsPanel = new ContentPanel();
        buttonsPanel.setLayout(new FitLayout());
        buttonsPanel.setHeaderVisible(true);
        buttonsPanel.setHeading(translate("Operations"));
        buttonsPanel.getHeader().setIntStyleAttribute("font-size", 11);
        buttonsPanel.getHeader().setStyleAttribute("border-left", "none");
        buttonsPanel.setBodyStyle("border-left-width: 0px; background-color: #DFE8F6;");
        return buttonsPanel;
    }

    protected ContentPanel initTreePanel() {
        ContentPanel treePanel = new ContentPanel();
        treePanel.setLayout(new FitLayout());
        treePanel.setHeaderVisible(true);
        treePanel.setHeading(translate(TREE_PANEL_TITLE));
        treePanel.getHeader().setIntStyleAttribute("font-size", 11);
        treePanel.getHeader().setStyleAttribute("border-right", "none");
        treePanel.setBodyStyle("border-right-width: 0px; background-color: #DFE8F6;");
        return treePanel;
    }

    protected void addTreePanel(ContentPanel panel, LayoutContainer treePanelContainer) {
        if (treePanelContainer != null) {
            setCrudId(treePanelContainer, SLG_TREE_PANEL);
            treeFrame = initTreePanel();
            addTreeToTreePanel(treeFrame, treePanelContainer);
            addTreePanelToTreeFrame(panel, treeFrame);
        }
    }

    protected void addTreePanelToTreeFrame(ContentPanel panel, ContentPanel buttonsFrame) {

        BorderLayoutData west = new BorderLayoutData(LayoutRegion.WEST, 150, 100, 250);
        west.setMargins(new Margins(0));
        west.setSplit(true);
        west.setCollapsible(true);
        panel.add(buttonsFrame, west);
    }

    protected void addTreeToTreePanel(ContentPanel buttonsFrame, LayoutContainer buttonsPanel) {
        buttonsPanel.setBorders(false);
        buttonsPanel.setStyleAttribute("overflow", "hidden");
        buttonsFrame.add(buttonsPanel);
    }

    protected ContentPanel initFrame() {
        ContentPanel panel = new ContentPanel();
        setCrudId(panel, ID_SLG_FRAME);
        panel.setLayout(new BorderLayout());
        panel.setFrame(false);
        panel.setBodyBorder(false);
        panel.setHeaderVisible(false);
        panel.getHeader().setIntStyleAttribute("height", 6);
        panel.getHeader().setIntStyleAttribute("padding", 0);
        panel.getHeader().setBorders(false);
        return panel;
    }

    protected ContentPanel initGridPanel(Grid<CUJO> grid, boolean isTree, boolean isButtons) {
        // panel to dock LiveGrid and GridButtons
        ContentPanel gridPanel = new ContentPanel(new FitLayout());

        setCrudId(gridPanel, ID_SLG_GRID_PANEL);

        gridPanel.setHeaderVisible(false);
        gridPanel.setBorders(false);
        gridPanel.add(grid);
        //
        ToolBar topToolBar = initTopToolBar();
        boolean topComponentShow = topToolBar != null;
        if (topComponentShow) {
            gridPanel.setTopComponent(topToolBar);
        }
        //
        ToolBar bottomToolBar = initBottomToolBar(grid);
        boolean bottomComponentShow = bottomToolBar != null;
        if (bottomComponentShow) {
            gridPanel.setBottomComponent(bottomToolBar);
        }
        //
        gridPanel.getHeader().setStyleAttribute("height", "15px");
        gridPanel.getHeader().setIntStyleAttribute("font-size", 11);

        if (isTree) {
            if (topComponentShow) {
                gridPanel.getTopComponent().setStyleAttribute("border-right-style", "dotted");
            }
            gridPanel.setBodyStyle(gridPanel.getBodyStyle() + " border-right-style: dotted;");
            if (bottomComponentShow) {
                gridPanel.getBottomComponent().setStyleAttribute("border-right-style", "dotted");
            }
        }
        // if treePnal
        if (isButtons) {
            if (topComponentShow) {
                gridPanel.getTopComponent().setStyleAttribute("border-left-style", "dotted");
            }
            gridPanel.setBodyStyle(gridPanel.getBodyStyle() + " border-left-style: dotted;");
            if (bottomComponentShow) {
                gridPanel.getBottomComponent().setStyleAttribute("border-left-style", "dotted");
            }
        }

        return gridPanel;
    }

    protected void setCrudId(Component gridPanel, String componentId) {
        gridPanel.getAriaSupport().setState(CRUD_ID, VIEW_TABLE + "_" + componentId);
    }

    public ToolBar initTopToolBar() {

        ToolBar topBar = new ToolBar();
        topBar.addStyleName("x-panel-header");
        topBar.setStyleAttribute("padding-left", "4px");

        AbstractImagePrototype tableIcon = icon == null ? Icons.Pool.table() : icon;
        Image image = tableIcon.createImage();
        image.setPixelSize(16, 16);
        LayoutContainer iconPanel = new LayoutContainer(new CenterLayout());
        iconPanel.setPixelSize(20, 20);
        iconPanel.add(image);
        topBar.add(iconPanel);

        Label tableLabel = new Label(getTableTitle());
        tableLabel.setStyleAttribute("color", "#15428B");
        tableLabel.setStyleAttribute("font-family", "tahoma,arial,verdana,sans-serif");
        tableLabel.setStyleAttribute("font-size", "11px");
        tableLabel.setStyleAttribute("font-weight", "bold");
        tableLabel.setStyleAttribute("padding-left", "5px");
        topBar.add(tableLabel);

        return topBar;
    }

    public String getTableTitle() {
        return translate(TABLE_TITLE);
    }

    protected void addGrid(ContentPanel panel, ContentPanel gridPanel) {
        BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER);
        center.setMargins(new Margins(0));
        center.setSplit(true);
        panel.add(gridPanel, center);
    }

    public LayoutContainer getTreePanelContainer() {
        return null;
    }

    /** StringovĂ˝ nĂˇzev aktuĂˇlnĂ­ho sloupce (podle nÄ›j se Ĺ™adĂ­ a dohledĂˇvĂˇ). */
    public String getOrderPropertyName() {

        return orderProperty != null
                ? orderProperty.getName()
                : "id";
    }

    /** VytvoĹ™Ă­me RpcProxy, pomocĂ­ nĂ­Ĺľ si LG doÄŤĂ­tĂˇ data v pĹ™Ă­padÄ›, Ĺľe store nemĂˇ vyhovujĂ­cĂ­ Ĺ™Ăˇdky k zobrazenĂ­... */
    public RpcProxy<PagingLoadResult<CUJO>> initProxy() {
        RpcProxy<PagingLoadResult<CUJO>> proxy = new RpcProxy<PagingLoadResult<CUJO>>() {

            @Override
            protected void load(Object loadConfig, AsyncCallback<PagingLoadResult<CUJO>> callback) {
                PagingLoadConfig config = (PagingLoadConfig) loadConfig;
                CQuery<CUJO> q = createQuery(config);
                getController().getData(q, reload, relationsCount, callback);
                stopReloadRowsCount();
            }
        };

        return proxy;
    }

    /** Getter */
    @Override
    public LiveGridControllerAsync getController() {
        return LiveGridControllerAsync.Pool.get();
    }

    /** SestavĂ­me klientskou query, kterĂˇ definuje dotaz pro naÄŤtenĂ­ dat. V Ăşvahu se bere vstupnĂ­ config a takĂ© vĂ˝stup metody getQuery */
    protected CQuery<CUJO> createQuery(PagingLoadConfig config) {
        CQuery<CUJO> cquery = new CQuery(getCujoType());
        if (config != null) {
            cquery.setOffset(config.getOffset());
            cquery.setLimit(config.getLimit());

            // orderBy
            final String configSort = config.getSortField();
            final String sortField = configSort != null && configSort.length()>0
                    ? configSort
                    : getAutoExpandColumnName();
            if (sortField != null) {
                CujoProperty valueProp = CujoManager.findIndirectProperty(getCujoType(), sortField);
                CujoProperty idProp = CujoManager.findIndirectProperty(getCujoType(), "id");
                if (config.getSortDir().compareTo(SortDir.DESC) == 0) {
                    valueProp = valueProp.descending();
                    idProp = idProp.descending();
                }
                cquery.addOrderBy(valueProp);
                cquery.addOrderBy(idProp);

            }
            // contactQuery
            if (getQuery() != null) {
                if (getQuery().getCriterion() != null) {
                    cquery.addCriterion(getQuery().getCriterion());
                }
                if (getQuery().getOrderBy() != null) {
                    for (CujoProperty<CUJO, ?> contactedOrder : getQuery().getOrderBy()) {
                        cquery.addOrderBy(contactedOrder);
                    }
                }
            }
        }
        return cquery;
    }

    /** Getter */
    @Override
    public CQuery<CUJO> getQuery() {
        return query;
    }

    /** na serveru je jiĹľ v cache, takĹľe se nepotĹ™ebuje znovu ptĂˇt DB na poÄŤet zĂˇznamĹŻ... */
    public void stopReloadRowsCount() {
        if (reload) {
            reload = false;
        }
    }

    /** VytvoĹ™Ă­me Loader, nadefinujeme mu RpcProxy pro asynchronnĂ­ volĂˇnĂ­ callbackĹŻ, pĹ™ednastavĂ­me sloupec pro Ĺ™azenĂ­ a smÄ›r Ĺ™azenĂ­... */
    protected PagingLoader<PagingLoadResult<CUJO>> initLoader(RpcProxy<PagingLoadResult<CUJO>> proxy, String autoExpandColumnName, SortDir sortDir) {
        PagingLoader<PagingLoadResult<CUJO>> loader = new BasePagingLoader<PagingLoadResult<CUJO>>(proxy);
        loader.addListener(Loader.BeforeLoad, new Listener<LoadEvent>() {

            @Override
            public void handleEvent(LoadEvent be) {
                if (!reload) {
                    be.setCancelled(true);
                    grid.unmask();
                    LiveGrid.this.unmask();
                    return;
                }

                // Budeme doÄŤĂ­tat:
                // 1. kontrola maskovĂˇnĂ­
                if (!grid.isMasked() && !LiveGrid.this.isMasked()) {
                    grid.mask(translate("Loading..."));
                }
                // 2. konfigurace loaderu
                BasePagingLoadConfig m = be.<BasePagingLoadConfig>getConfig();
                m.set("start", m.get("offset"));
                m.set("ext", "js");
                m.set("lightWeight", true);
                m.set("sort", (m.get("sortField") == null) ? "" : m.get("sortField"));
                m.set("dir", (m.get("sortDir") == null || (m.get("sortDir") != null && m.<SortDir>get("sortDir").equals(SortDir.NONE))) ? "" : m.get("sortDir"));
            }
        });
        loader.setSortDir(sortDir);
        loader.setSortField(autoExpandColumnName);
        loader.setRemoteSort(true);

        return loader;
    }

    /** VytvoĹ™Ă­me Grid, pĹ™edĂˇme mu store, a column model. DĂˇle nastavĂ­me LiveGridStore (doplnÄ›nou o horizontĂˇlnĂ­ scrollovĂˇnĂ­)... */
    protected Grid<CUJO> initGrid(ListStore<CUJO> store, ColumnModel cm) {
        // grid
        final Grid<CUJO> grid = new Grid<CUJO>(store, cm);
        setCrudId(grid, "grid");
        if (autoExpandColumnName != null && autoExpandColumnName.length()>0) {
            grid.setAutoExpandColumn(getAutoExpandColumnName());
            grid.setAutoExpandMax(10000);
            grid.setAutoExpandMin(150);
        }
        grid.setStripeRows(true);
        grid.setColumnLines(true);

        // view
        LiveGridView liveView = initView();
        grid.setView(liveView);

        // selectionModel
        grid.setSelectionModel(initSelectionModel());

        // scroll listener (liveGridScroller)
        grid.addListener(Events.OnScroll, new Listener<GridEvent<CUJO>>() {

            @Override
            public void handleEvent(GridEvent<CUJO> be) {
                if (LiveGrid.this.isMasked() || grid.isMasked() || grid.getView().getScroller().isMasked()) {
                    be.setCancelled(true);
                    be.stopEvent();
                    return;
                }
                setReload(true);
            }
        });

        // mouse listener
        grid.addListener(Events.OnDoubleClick, new Listener<GridEvent<CUJO>>() {

            @Override
            public void handleEvent(GridEvent<CUJO> be) {
                onChange(grid.getSelectionModel().getSelectedItem());
            }
        });

        // key listener
        grid.addListener(Events.OnKeyDown, new Listener<GridEvent<CUJO>>() {

            @Override
            public void handleEvent(GridEvent<CUJO> be) {

                // eventy jsou pĹ™Ă­pustnĂ© pouze, kdyĹľ jsou k dispozici data...
                if (LiveGrid.this.isMasked() || grid.isMasked() || ((LiveGridView) grid.getView()).getScroller().isMasked()) {
                    stop(be);
                    return;
                }

                // eventy, kterĂ© je tĹ™eba blokovat:
                if (KeyCodes.KEY_BACKSPACE == be.getKeyCode()) {
                    stop(be);
                    return;
                }

                // enter
                if (KeyCodes.KEY_ENTER == be.getKeyCode()) {
                    List<CUJO> selectedItems = grid.getSelectionModel().getSelectedItems();
                    if (selectedItems.isEmpty()) {
                        Info.display(translate("Info"), translate("Select-one-row"));
                        stop(be);
                        return;
                    }
                    if (selectedItems.size() > 1) {
                        Info.display(translate("Info"), translate("Select-only-one-row"));
                        stop(be);
                        return;
                    }
                    onChange(selectedItems.get(0));
                }

                LiveGridView view = (LiveGridView) grid.getView();
                LiveGridSelectionModel<CUJO> selection = (LiveGridSelectionModel<CUJO>) grid.getSelectionModel();
                // pageUp
                if (KeyCodes.KEY_PAGEUP == be.getKeyCode()) {
                    if (selection.hasPrevSelect()) {
                        // oznaÄŤĂ­me prnĂ­ Ĺ™Ăˇdek (nenĂ­ oznaÄŤen ĹľĂˇdnĂ˝, nebo nestojĂ­me na prvnĂ­ pozici)
                        selection.select(
                                0,
                                be.isShiftKey() ? true : false,
                                getFocusOnLiveGrid());
                    } else {
                        view.movePageUp();
                    }
                }
                // pageDown
                if (KeyCodes.KEY_PAGEDOWN == be.getKeyCode()) {
                    if (selection.hasNextSelect()) {
                        int selectIndex = Math.max(0, Math.min(grid.getStore().getCount() - 1, view.getVisibleRowCount() - 1));
                        selection.select(
                                selectIndex,
                                be.isShiftKey() ? true : false,
                                getFocusOnLiveGrid());
                    } else {
                        view.movePageDown();
                    }
                }
                // home
                if (KeyCodes.KEY_HOME == be.getKeyCode()) {
                    view.moveHome();
                }
                // home
                if (KeyCodes.KEY_END == be.getKeyCode()) {
                    view.moveEnd();
                }

            }

            protected void stop(GridEvent<CUJO> be) {
                be.setCancelled(true);
                be.stopEvent();
            }
        });

        // zaĹ™Ă­dĂ­ oznaÄŤenĂ­ Ĺ™Ăˇdku v gridu po doÄŤtenĂ­
        grid.getView().addListener(Events.LiveGridViewUpdate, new Listener<GridEvent<CUJO>>() {

            @Override
            public void handleEvent(GridEvent<CUJO> be) {
                // pokud je pĹ™ednastavenĂ˝ offset pro store, musĂ­me posunout takĂ© view...
                if (defaultOffset != null) {
                    ((LiveGridView) grid.getView()).moveTo(defaultOffset);
                    defaultOffset = null;
                }

                //VyznaÄŤĂ­me sloupec, podle kterĂ©ho se aktuĂˇlnÄ› Ĺ™adĂ­
                String sortField = grid.getStore().getLoadConfig().getSortField();
                for (CujoProperty cujoProperty : availableColumns) {
                    if (cujoProperty.getName().equals(sortField)) {
                        // TODO: po zmÄ›nÄ› bunÄ›k je potĹ™eba dĂˇle oĹˇetĹ™it
                        ((LiveGridView) grid.getView()).selectColumn(availableColumns.indexOf(cujoProperty));
                    }
                }

                // oznaÄŤenĂ­ Ĺ™Ăˇdku (podle poslednĂ­ho oznaÄŤenĂ©ho itemu)
                int lastSelected = ((LiveGridView) grid.getView()).getLastSelected();
                ((LiveGridSelectionModel) grid.getSelectionModel()).select(lastSelected, false, getFocusOnLiveGrid());
//                if (focusComponent != null) {
//                    focusComponent.focus();
//                    focusComponent = null;
//                }
                grid.unmask();
                LiveGrid.this.unmask();
            }
        });

        return grid;
    }

    public Component getFocusOnLiveGrid() {
        return focusComponent == null || focusComponent.equals(grid)
                ? null
                : focusComponent;
    }

    protected GridViewConfig getGridViewConfig() {
        return null;
    }

    /** VytvoĹ™Ă­ LiveGridView. DolnÄ›nĂ­m metody afterRender zajistĂ­me horizontĂˇlnĂ­ scrollovĂˇnĂ­. */
    protected LiveGridSelectionModel<CUJO> initSelectionModel() {
        LiveGridSelectionModel<CUJO> selectionModel = new LiveGridSelectionModel();
        return selectionModel;
    }

    /** VytvoĹ™Ă­ LiveGridView. DolnÄ›nĂ­m metody afterRender zajistĂ­me horizontĂˇlnĂ­ scrollovĂˇnĂ­. */
    protected LiveGridView<CUJO> initView() {
        LiveGridView<CUJO> liveView = new LiveGridView<CUJO>(this) {

            @Override
            public String translate(String name) {
                return LiveGrid.this.translate(name);
            }
        };

        // gridViewConfig
        GridViewConfig gridViewConfig = getGridViewConfig();
        if (gridViewConfig != null) {
            liveView.setViewConfig(gridViewConfig);
        }

        liveView.setEmptyText(translate("No-rows-available-on-the-server."));
        return liveView;
    }

    @Override
    protected void afterRender() {
        super.afterRender();
        grid.focus();
    }

    /** VytvoĹ™Ă­ ContentPanel, do kterĂ©ho je umĂ­stÄ›nĂ˝ LG... */
    protected ContentPanel initGridPanel() {
        ContentPanel panel = new ContentPanel();
        panel.setFrame(true);
        panel.setCollapsible(true);
        panel.setAnimCollapse(false);
        panel.setIcon(Icons.Pool.table());
        panel.setHeading("LiveGrid");
        panel.setLayout(new FitLayout());
        return panel;
    }

    /** VytvoĹ™Ă­ ToolBar, kterĂ˝ je nĂˇslednÄ› umĂ­stÄ›n jako BottomComponent v panelu pro tabulku. Je primĂˇrnÄ› urÄŤen napĹ™. pro LiveGridTool a ostatnĂ­ tools... */
    protected ToolBar initBottomToolBar(Grid<CUJO> grid) {
        ToolBar toolBar = new ToolBar();
        toolBar.add(new FillToolItem());
        addLiveTool(grid, toolBar);
        return toolBar;
    }

    /** Nabindujeme LiveGridTool. */
    protected void addLiveTool(Grid<CUJO> grid, ToolBar toolBar) {
        LiveToolItem item = new LiveToolItem();
        item.bindGrid(grid);
        toolBar.add(item);
    }

    /** Metoda, kterĂˇ supluje pĹ™eklady - po pĹ™ipojenĂ­ UJO-CRUD staÄŤĂ­ na jednom mĂ­stÄ› definovat tuto abstraktnĂ­ metodu a mĂˇte moĹľnost napsat si vlastnĂ­ pĹ™eklady k celĂ˝m LG... */
    public abstract String translate(String parent, String name);

    /** PĹ™edvyplnÄ›nĂˇ metoda po pĹ™eklady - vyuĹľĂ­vĂˇ automaticky rodiÄŤe z property v LiveGrid.VIEW_TABLE. */
    public String translate(String name) {
        return translate(VIEW_TABLE, name);
    }

    public abstract String registerId();

    /** Getter */
    protected List<CujoProperty> getAvailableColumns() {
        return availableColumns;
    }

    /** Pokud jsou availableColumns null, nebo je list prĂˇzdnĂ˝, pĹ™idĂˇ se pro kaĹľdou cujoProperty jeden sloupec... */
    private List<CujoProperty> createDefaultAvailableColumns() {
        if (availableColumns == null || availableColumns.isEmpty()) {
            List<CujoProperty> availableCols = new ArrayList<CujoProperty>();
            List list = Arrays.asList(getNewCujo().readProperties().getProperties());
            availableCols.addAll(list);
            return availableCols;
        }
        return availableColumns;
    }

    /** VracĂ­ nĂˇzev sloupcĹŻ, kterĂ© mohou bĂ˝t zobrazeny - slpĹ�ujĂ­ 2 kritĂ©ria:
     *   1. jsou definovĂˇny v availableColumns (popĹ™. property availableColumns je null
     *   2. nejsou obsaĹľeny v property disableColumnsNames
     */
    private List<String> createDefaultAvailableColumnNamesToShow(List<CujoProperty> availableColumns) {
        List<String> availableColsNamesToShow = new ArrayList<String>();
        for (CujoProperty cujoProperty : availableColumns) {
            availableColsNamesToShow.add(cujoProperty.getName());
        }
        return availableColsNamesToShow;
    }

    /** NĂˇzev sloupce, kterĂ˝ se svou*/
    protected String getAutoExpandColumnName() {
        if (autoExpandColumnName == null) {
            autoExpandColumnName = availableColumnsNamesToShow.get(autoExpandColumnIndex);
        }
        return autoExpandColumnName;
    }

    /** Vygeneruje nĂˇzev sloupce, kterĂ˝ se bude roztahovat. */
    protected String createDefaultAutoExpandColumnName(String name, List<String> names, int index) throws UnsupportedOperationException {
        if (name == null) {
            if (index < names.size()) {
                return names.get(index);
            } else if (!names.isEmpty()) {
                return names.get(0);
            } else {
                // TODO: nedefinovany stav - neni zadny pozadovany sloupec do tabulky...
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
        return name;
    }

    /** OdebĂ­rĂˇnĂ­ sloupcĹŻ se provĂˇnĂ­ na zĂˇkladÄ› stringovĂ˝ch nĂˇzvĹŻ sloupcĹŻ. */
    protected List<String> removeDisableColumnNames(List<String> toShow, List<String> disable) {
        for (String disableColumnName : disable) {
            toShow.remove(disableColumnName);
            for (CujoProperty column : getAvailableColumns()) {
                if (column.getName().equals(disableColumnName)) {
                    getAvailableColumns().remove(column);
                    break;
                }
            }
        }
        return toShow;
    }

    /** Tato metoda vracĂ­ seznam sloupcĹŻ, kterĂ© nemajĂ­ bĂ˝t vĹŻbec zobrazeny (uvaĹľuje se i v pĹ™Ă­padÄ›, Ĺľe nadefinujete sloupce pro zobrazenĂ­ - takĹľe pokud bude stejnĂ˝ sloupec v available column i v disableColumnNames, nezobrazĂ­ se...). */
    protected List<String> getDisableColumnNames() {
        return disableColumnNames == null
                ? new ArrayList<String>()
                : disableColumnNames;
    }

    /** Na zĂˇkladÄ› columnConfigu sestavĂ­me column model. */
    protected ColumnModel initColumnModel(List<ColumnConfig> columnConfigs) {
        return new ColumnModel(columnConfigs);
    }

    /** VytvoĹ™Ă­me Column Config.
     *  - pokud chcete nadefinovat sloupce, kerĂ© se majĂ­ zobrazit, inicializujte v konstruktoru promÄ›nnou availableColumn
     *  - pokud chcete nadefinovat poĹ™adĂ­ sloupcĹŻ, nastavte sprĂˇvnÄ› poĹ™adĂ­ CujoProperty v listu availableColumn
     */
    protected List<ColumnConfig> initColumnConfig(List<CujoProperty> availableColumns, String autoExpandColumnName, int autoExpandColumnIndex) {
        List<ColumnConfig> config = new ArrayList<ColumnConfig>();
        int i = 0;
        for (final CujoProperty cujoProperty : availableColumns) {
            final String name = cujoProperty.getName();

            final boolean isAutoExpand = (autoExpandColumnName != null && autoExpandColumnName.equals(name)) || i == autoExpandColumnIndex;

            ColumnConfig col = createDefaultColumnConfig(name, isAutoExpand, cujoProperty);
            if (isAutoExpand) {

                col.setRenderer(new GridCellRenderer<CUJO>() {

                    @Override
                    public Object render(CUJO model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<CUJO> store, Grid<CUJO> grid) {
                        Object value = createColumRendererValue(cujoProperty, model);
                        if (value instanceof String) {
                            return "<B>" + value + "</B>";
                        }

                        return value;
                    }
                });
            } else {

                col.setRenderer(new GridCellRenderer<CUJO>() {

                    @Override
                    public Object render(CUJO model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<CUJO> store, Grid<CUJO> grid) {
                        return createColumRendererValue(cujoProperty, model);
                    }
                });
            }

            config.add(col);
            ++i;
        }
        return config;
    }

    protected Object createColumRendererValue(CujoProperty cujoProperty, CUJO model) {
        return cujoProperty.getType() == Date.class
                ? parseDate((Date) model.get(cujoProperty))
                : model.get(cujoProperty) != null ? model.get(cujoProperty).toString() : "";
    }

    protected ColumnConfig createDefaultColumnConfig(final String name, final boolean isAutoExpand, final CujoProperty cujoProperty) {
        ColumnConfig columnConfig = new ColumnConfig(
                name,
                translate(name),
                isAutoExpand ? 200 : getColumnWidth(cujoProperty.getType()));

        return columnConfig;
    }

    /** Vraci string ve formatu dd.MM.yyyy, popr prazdny retezec pokud je Date null. */
    public String parseDate(Date date) {
        if (date == null) {
            return "";
        }
        return DateTimeFormat.getFormat("dd.MM.yyyy").format(date);
    }
    // column width const
    private final int TEXT_COLUMN = 150;
    private final int DATE_COLUMN = 65;
    private final int DIGIT_COLUMN = 65;
    private final int INDEX_COLUMN = 30;
    private final int DEFAULT_COLUMN = 100;
    private final int MAIN_COLUMN = 200;

    /** VracĂ­ ĹˇĂ­Ĺ™ku sloupce v zĂˇvislosti na datovĂ©m typu, kterĂ˝ sloupec obsahuje. */
    protected int getColumnWidth(Class type) {
        if (type == String.class) {
            return TEXT_COLUMN;
        } else if (type == Date.class) {
            return DATE_COLUMN;
        } else if (type == Integer.class || type == Long.class || type == Double.class) {
            return DIGIT_COLUMN;
        } else {
            return DEFAULT_COLUMN;
        }
    }

    /** VrĂˇtĂ­ nĂˇsledujĂ­cĂ­ zĂˇznam od prvnĂ­ho oznaÄŤenĂ©ho itemu v LG a pĹ™esune na nÄ›j fokus.
     *
     * TODO: prozatĂ­m se umĂ­ posouvat pouze po view, nikoliv po store!
     */
    @Override
    public CUJO getNextCujo() {
        final CUJO selectedCujo = (CUJO) grid.getSelectionModel().getSelectedItem();
        grid.getSelectionModel().selectNext(false);
        final CUJO nextCujo = (CUJO) grid.getSelectionModel().getSelectedItem();
        if (selectedCujo.equals(nextCujo)) {
            // jsme na poslednim zaznamu...
            return null;
        }
        return nextCujo;
    }

    /** VrĂˇtĂ­ pĹ™edchozĂ­ zĂˇznam od prvnĂ­ho oznaÄŤenĂ©ho itemu v LG a pĹ™esune na nÄ›j fokus.
     *
     * TODO: prozatĂ­m se umĂ­ posouvat pouze po view, nikoliv po store!
     */
    @Override
    public CUJO getPrevCujo() {
        final CUJO selectedCujo = (CUJO) grid.getSelectionModel().getSelectedItem();
        grid.getSelectionModel().selectPrevious(false);
        final CUJO prevCujo = (CUJO) grid.getSelectionModel().getSelectedItem();
        if (selectedCujo.equals(prevCujo)) {
            // jsme na prvvim zaznamu...
            return null;
        }
        return prevCujo;
    }

    /** VrĂˇtĂ­ prvnĂ­ oznaÄŤenĂ˝ item v LG, nebo null. */
    public CUJO getSelectedItem() {
        if (getSelectedItems() != null && getSelectedItems().size() > 0) {
            return getSelectedItems().get(0);
        }
        return null;
    }

    /** VrĂˇtĂ­ vĹˇechny oznaÄŤenĂ© itemy v LG */
    public List<CUJO> getSelectedItems() {
        return grid.getSelectionModel().getSelectedItems();
    }

    @Override
    public void setReload(Boolean reload) {
        this.reload = reload;
    }
}
