/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.gui.livegrid;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.commons.Icons;

/**
 * @author Pelc
 *
 * LiveGrid is a grid with the "live" searchable tools to show only bit of database table.
 *
 * Tips:
 *  - To display only selected columns initializ variable availableColumns in he constructor
 *  - to display only selected columns init variable availableColumns in constructor
 *  - the order selected columns is dependent on the order items in list availableColumns
 *
 */
public abstract class LiveGridSearch<CUJO extends AbstractCujo> extends LiveGrid<CUJO> {

    public static final String LIVE_GRID_SEARCH_FIELD = "LiveGridSearchField";
    // const
    public static final int SEARCH_DELAY = 600;
    public static final int SEARCH_FIELD_WIDTH = 150;
    public static final EventType liveGridSearch = new EventType();
    //
    protected TextField<String> searchField;
    protected DelayedTask searchDelay;
    protected String searchedFirst;

    /** DvoufĂˇzovĂ© vyhledĂˇvĂˇnĂ­:
     *  1. provÄ›Ĺ™Ă­me, Ĺľe na dohledĂˇvanĂ˝ vĂ˝raz existujĂ­ zĂˇznamy v DB
     *  2. doÄŤteme zĂˇznamy dlepoĹľadovanĂ˝ch kritĂ©riĂ­
     */
    public void doSearch() {
        reload = true;
        focusComponent = searchField;
        // abychom mohli vyhledĂˇvat, musĂ­ bĂ˝t oznaÄŤenĂ˝ nÄ›jakĂ˝ Ĺ™Ăˇdek
        CUJO selectedCujo = grid.getSelectionModel().getSelectedItem();
        if (selectedCujo == null) {
            Info.display(translate("Info"), translate("Select-one-row"));
            return;
        }
        PagingLoadConfig config = (PagingLoadConfig) grid.getStore().getLoadConfig();
        final String orderBy = config.getSortField();
        final boolean descending = config.getSortDir().equals(SortDir.DESC);
        final String searched = searchField.getValue();
        // TODO: nahradit zjiĹˇĹĄovĂˇnĂ­ datovĂ©ho typu z CujoProperty, podle kterĂ© se Ĺ™adĂ­
        final Object searchedColumnValue = selectedCujo.get(orderBy);
        //
        if (searched == null || searched.length()==0) {
            stornoTextField();
        }
        //
        AsyncCallback controllExistCallback = new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
                Info.display(translate("Error"), translate("Controll-query-exist-faild"));
                // TODO: overovani existence zaznamu selhalo...
            }

            @Override
            public void onSuccess(Boolean result) {
                if (result != null && result) {
                    // TODO: vĂ˝raz nalezen - dohledĂˇvanĂ­ polĂ­ÄŤko by mÄ›lo zezelenat...
                    if (searched.length()>0) {
                        searchedToGreen();
                    }
                    AsyncCallback getRowNumCallback = new AsyncCallback<Integer>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Info.display(translate("Error"), translate("Searching-first-row-faild"));
                            // TODO: hledani pozice prvniho vhodneho zaznamu selhalo...
                        }

                        @Override
                        public void onSuccess(Integer result) {
                            if (result == null) {
                                // TODO: neznama chyba - hledeany rowNum se vratil jako NULL
                                return;
                            }
                            ((LiveGridView) grid.getView()).moveTo(result.intValue(), searchField);
                        }
                    };
                    if (searchedColumnValue instanceof Long) {
                        getController().getSearchedRow(
                                getCujoType().getName(),
                                descending,
                                orderBy,
                                Long.valueOf(searched),
                                query,
                                getRowNumCallback);
                    } else if (searchedColumnValue instanceof Integer) {
                        getController().getSearchedRow(
                                getCujoType().getName(),
                                descending,
                                orderBy,
                                Integer.valueOf(searched),
                                query,
                                getRowNumCallback);
                    } else {
                        getController().getSearchedRow(
                                getCujoType().getName(),
                                descending,
                                orderBy,
                                searched,
                                query,
                                getRowNumCallback);
                    }
                } else {
                    Info.display(translate("Info"), translate("Searched-key-not-found"));
                    searchedToRed();
                }
            }
        };
        try {
            if (searchedColumnValue instanceof Long) {
                getController().isQuery(
                        getCujoType().getName(),
                        Long.valueOf(searched),
                        orderBy,
                        descending,
                        query,
                        controllExistCallback);
            } else if (searchedColumnValue instanceof Integer) {
                getController().isQuery(
                        getCujoType().getName(),
                        Integer.valueOf(searched),
                        orderBy,
                        descending,
                        query,
                        controllExistCallback);
            } else {
                getController().isQuery(
                        getCujoType().getName(),
                        searched,
                        orderBy,
                        descending,
                        query,
                        controllExistCallback);
            }
        } catch (Exception ex) {
            System.out.println(translate(VIEW_TABLE, "err_cant_transform_text_to_class") + ex);
        }
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        searchDelay = new DelayedTask(new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                doSearch();
            }
        });

    }

    /** VytvoĹ™Ă­ LiveGridView. DolnÄ›nĂ­m metody afterRender zajistĂ­me horizontĂˇlnĂ­ scrollovĂˇnĂ­. */
    @Override
    protected LiveGridView initView() {
        LiveGridView<CUJO> view = super.initView();
        view.addListener(LiveGridView.afterHeaderClick, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                // DEBUG:
                // Info.display("OnHeder", "afterClick");

                // String value = searchField.getValue();
                // if (value != null && !value.isEmpty()) {
                //      doSearch();
                // }

                searchField.setValue("");
                stornoTextField();
            }
        });
        return view;
    }

    public void stornoTextField() {
        searchField.removeStyleName("textfield-red");
        searchField.removeStyleName("textfield-green");
        ((ToolBar) searchField.getParent()).layout();
    }

    protected void searchedToRed() {
        searchField.removeStyleName("textfield-green");
        searchField.addStyleName("textfield-red");
        searchField.setStyleAttribute("background", width);
    }

    protected void searchedToGreen() {
        searchField.removeStyleName("textfield-red");
        searchField.addStyleName("textfield-green");
        ((ToolBar) searchField.getParent()).layout();
    }

    // TODO: pĹ™esunout do LGP
    public void onMouseDoubleClick() {
        onChange(getSelectedItem());
    }

    // TODO: pĹ™esunout do LGP
    public void onEnterPress() {
        onChange(getSelectedItem());
    }

    /** NovÄ› pĹ™idĂˇme listener, kterĂ˝ ve finĂˇlnĂ­ podobÄ› bude delegovat udĂˇlost z gridu na searchField. NynĂ­ pouze naslouchĂˇ a na CTRL + F pĹ™edĂˇ focus na searchField. */
    @Override
    protected Grid<CUJO> initGrid(ListStore<CUJO> store, ColumnModel cm) {
        Grid<CUJO> liveGrid = super.initGrid(store, cm);
        liveGrid.addListener(Events.OnKeyDown, new Listener<GridEvent<CUJO>>() {

            @Override
            public void handleEvent(GridEvent<CUJO> be) {
                // ctrl + f
                if (be.isControlKey() && be.getKeyCode() == 70) {
                    searchField.focus();
                    be.setCancelled(true);
                    be.stopEvent();
                    return;
                }
            }
        });
        return liveGrid;
    }

    /** PĹ™ipravĂ­me dohledĂˇvacĂ­ polĂ­ÄŤko se sadou listenerĹŻ, kterĂˇ se postarĂˇ o nĂˇslodnou delegaci eventĹŻ do LiveGridSearch... */
    protected TextField<String> initSearchField() {
        final TextField<String> field = new TextField<String>();
        setCrudId(field, LIVE_GRID_SEARCH_FIELD);
        field.setEmptyText(translate("Search..."));
        field.setWidth(SEARCH_FIELD_WIDTH);
        field.addKeyListener(new KeyListener() {

            @Override
            public void componentKeyDown(ComponentEvent event) {
                super.componentKeyDown(event);
                final int keyCode = event.getKeyCode();
                // DEBUG
                // Info.display("searchField", "KeyDown");

                // tyto klavesovĂ© zkratky nesmĂ­ vyvolat dohledĂˇvĂˇnĂ­:
                if (keyCode == KeyCodes.KEY_LEFT
                        || keyCode == KeyCodes.KEY_RIGHT
                        || (event.isControlKey() && event.getKeyCode() == 70))// ctrl + f
                {
                    stop(event);
                    return;
                }
                // tyto klavesovĂ© zkratk jsou povolenĂ© pro ovlĂˇdĂˇnĂ­ LG:
                LiveGridSelectionModel<CUJO> selection = (LiveGridSelectionModel<CUJO>) grid.getSelectionModel();
                if (keyCode == KeyCodes.KEY_DOWN) {
                    selection.setFocusComponent(searchField);
                    selection.doKeyDown(null);
                    stop(event);
                    return;
                }
                if (keyCode == KeyCodes.KEY_UP) {
                    selection.setFocusComponent(searchField);
                    selection.doKeyUp(null);
                    stop(event);
                    return;
                }
                final LiveGridView<CUJO> view = (LiveGridView<CUJO>) grid.getView();
                if (keyCode == KeyCodes.KEY_PAGEDOWN) {
                    view.movePageDown();
                    searchField.focus();
                    stop(event);
                    return;
                }
                if (keyCode == KeyCodes.KEY_PAGEUP) {
                    view.movePageUp();
                    searchField.focus();
                    stop(event);
                    return;
                }
                if (keyCode == KeyCodes.KEY_ENTER) {
                    final CUJO selectedItem = getSelectedItem();
                    if (selectedItem != null) {
                        onChange(selectedItem);
                    } else {
                        Info.display(translate("Info"), translate("Select-one-row"));
                    }
                    stop(event);
                    return;
                }

                // pokud to dojde aĹľ sem, byl napsĂˇn znak, kterĂ˝ mĂˇ bĂ˝t dohledatelnĂ˝
                field.fireEvent(liveGridSearch, new FieldEvent(field));
            }

            private void stop(ComponentEvent event) {
                event.setCancelled(true);
                event.stopEvent();
            }
        });
        field.addListener(liveGridSearch, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                // DEBUG
                // Info.display("LiveGridSearch", "value: " + be.getField().getRawValue());
                if (searchField.getValue() != null) {
                    searchDelay.delay(SEARCH_DELAY);
                }
            }
        });
        if (searchedFirst != null) {
            field.setValue(searchedFirst);
            if (defaultOffset != null) {
                searchedToGreen();
            } else {
                searchedToRed();
            }
        }
        return field;
    }

    @Override
    public ToolBar initTopToolBar() {
        ToolBar bar = super.initTopToolBar();
        bar.add(new FillToolItem());
        addSearchTool(bar);
        return bar;
    }

    /** Binding search tool. */
    protected void addSearchTool(ToolBar toolBar) {
        searchField = initSearchField();
        LayoutContainer imgPanel = initSearchIcon();

        toolBar.add(imgPanel);
        toolBar.add(searchField);
    }

    protected LayoutContainer initSearchIcon() {
        AbstractImagePrototype searchIcon = Icons.Pool.liveSearch();
        Image img = searchIcon.createImage();
        img.setPixelSize(16, 16);
        LayoutContainer imgPanel = new LayoutContainer(new CenterLayout());
        imgPanel.setSize(20, 20);
        imgPanel.add(img);
        return imgPanel;
    }
}
