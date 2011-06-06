/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujorm.gxt.client.gui;

/**
 *
 * @author Pelc Dobroslav
 */
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.store.StoreEvent;
import java.util.List;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.commons.Icons;
import org.ujorm.gxt.client.controller.TableControllerAsync;
import org.ujorm.gxt.client.cquery.CQuery;

public abstract class CujoTreePanel<CUJO extends Cujo> extends ContentPanel {

    protected TreeStore<CUJO> store;
    protected TreeLoader<CUJO> loader;
    protected TreePanel<CUJO> tree;
    protected CQuery<CUJO> query;
    private String displayProperty = "name";
    private CUJO parent;

    public CUJO getParentItem() {
        return parent;
    }

    public void setParentItem(CUJO parent) {
        this.parent = parent;
    }

    public CujoTreePanel() {
    }

    public CujoTreePanel(CQuery<CUJO> query) {
        this(query, null);
    }

    public CujoTreePanel(CQuery<CUJO> query, CujoProperty displayProperty) {
        this.query = query;
        if (displayProperty != null) {
            this.displayProperty = displayProperty.getName();
        }
    }

    public abstract CQuery<CUJO> getDefaultQuery();

    public abstract String getPanelTitle();

    public abstract void onChange(CUJO selectedItem);

    public abstract void afterStoreInit(TreeStore<CUJO> loader);

    protected AbstractImagePrototype availableItemIcon(CUJO model) {
        return Icons.Pool.bullet_blue();
    }

    public abstract boolean hasItemChildren(CUJO parent);

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        setLayout(new FlowLayout(10));

        // data proxy
        RpcProxy<List<Cujo>> proxy = new RpcProxy<List<Cujo>>() {

            @Override
            protected void load(Object loadConfig, AsyncCallback<List<Cujo>> callback) {
                TableControllerAsync.Util.getInstance().getCujoList(getDefaultQuery(), callback);
            }
        };

        // tree loader
        loader = new BaseTreeLoader<CUJO>(proxy) {

            @Override
            public boolean hasChildren(CUJO parent) {

                boolean isParent = hasItemChildren(parent);
                if (isParent) {
                    setParentItem(parent);
                }
                return isParent;
            }
        };

        // trees store
        store = new TreeStore<CUJO>(loader);
        store.addStoreListener(new StoreListener<CUJO>() {

            @Override
            public void storeUpdate(StoreEvent<CUJO> se) {
                super.storeUpdate(se);
                afterStoreInit(store);
            }
        });
//        tree.setModelProcessor(null)
        tree = new TreePanel<CUJO>(store);
        tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<CUJO>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<CUJO> se) {
                onChange(se.getSelectedItem());
            }
        });
        tree.setStateful(true);
        tree.setDisplayProperty(displayProperty);
        // statefull components need a defined id
        tree.setId("statefullasynctreepanel");
        tree.setIconProvider(new ModelIconProvider<CUJO>() {

            @Override
            public AbstractImagePrototype getIcon(CUJO model) {
                return availableItemIcon(model);
            }
        });

        setHeading(getPanelTitle());
        setLayout(new FitLayout());
        add(tree);
//        cp.setSize(315, 400);

        ToolTipConfig config = new ToolTipConfig();
        config.setTitle("Example Information");
        config.setShowDelay(1);
        config.setText("In this example state has been enabled for the tree. When enabled, the expand state of the tree is "
                + "saved and restored using the StateManager. Try refreshing the browser after expanding some nodes in the "
                + "tree. Notice that this works with asynchronous loading of nodes.");

        ToolButton btn = new ToolButton("x-tool-help");
        btn.setToolTip(config);

        getHeader().addTool(btn);
    }

    public void setDisplayProperty(String displayProperty) {
        this.displayProperty = displayProperty;
    }

    public TreePanel<CUJO> getTree() {
        return tree;
    }
}
