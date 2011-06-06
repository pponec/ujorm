/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;
import org.ujorm.gxt.client.CLoginRedirectable;
import org.ujorm.gxt.client.CMessageException;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.controller.TableControllerAsync;
import org.ujorm.gxt.client.cquery.CCriterion;
import org.ujorm.gxt.client.cquery.CQuery;
import org.ujorm.gxt.client.tools.MessageDialog;

/**
 * Component type of drop down for a CUJO object.
 * @author Pelc, Ponec
 * @see CujoField
 */
public abstract class OldCujoBox<CUJO extends Cujo> extends ComboBox<CUJO> {

    /** The property to display in the drop-down list */
    protected CujoProperty displayProperty;
    protected CCriterion<CUJO> aditionalCriterion;
    /** Depth to Load relations. Value 0 means no relations, value 1 means load the first level of relations. */
    protected int loadRelations = 0;
    protected CLoginRedirectable loginRedirectable;

    public OldCujoBox(CujoProperty<? super CUJO,?> displayProperty) {
        this(displayProperty, null);
    }

    public OldCujoBox(CujoProperty<? super CUJO,?> displayProperty, CLoginRedirectable loginRedirectable) {
        this.loginRedirectable = loginRedirectable;
        setDisplayProperty(displayProperty);
        setEditable(false);
    }

    @Override
    protected void onRender(Element parent, int index) {
        // Combo Box needs the Store:
        final PagingLoader<PagingLoadResult<CUJO>> loader = createLoader();
        loader.setSortDir(SortDir.ASC);
        loader.setRemoteSort(true);
        store = new ListStore<CUJO>(loader);
        super.onRender(parent, index);

        addSelectionChangedListener(new SelectionChangedListener<CUJO>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<CUJO> se) {
                onChange(se.getSelectedItem());
            }
        });

        setEmptyText("Select value...");
    }

        /** Create a Loader for a Store. */
    protected PagingLoader<PagingLoadResult<CUJO>> createLoader() {
        RpcProxy<PagingLoadResult<Cujo>> proxy = new RpcProxy<PagingLoadResult<Cujo>>() {
            @Override
            public void load(final Object loadConfig, final AsyncCallback<PagingLoadResult<Cujo>> callback) {
                final AsyncCallback<PagingLoadResult<Cujo>> callback2 = new AsyncCallback<PagingLoadResult<Cujo>>() {
                    @Override
                    public void onFailure(final Throwable e) {
                        if (loginRedirectable!=null && CMessageException.isSessionTimeout(e)) {
                            loginRedirectable.redirectToLogin();
                            return;
                        }
                        callback.onFailure(e);
                        String msg = "Data loading error";
                        new MessageDialog(msg).show();
                        OldCujoBox.this.unmask();
                    }
                    @Override
                    public void onSuccess(final PagingLoadResult<Cujo> result) {
                        onDataLoadSuccess(((PagingLoadResult<CUJO>) result).getData());
                        callback.onSuccess(result);
                    }
                };
                TableControllerAsync.Util.getInstance().getDbRows(getCQuery(), new BasePagingLoadConfig(), callback2);
            }
        };

        final PagingLoader<PagingLoadResult<CUJO>> loader = new BasePagingLoader<PagingLoadResult<CUJO>>(proxy);
        loader.setSortDir(SortDir.ASC);
        loader.setRemoteSort(true);
        return loader;
    }

    /** Overwrite this method. */
    protected void onDataLoadSuccess(List<CUJO> data) {
    }

    /** The underlying data field name to bind to this ComboBox (defaults to 'text' */
    public final void setDisplayProperty(CujoProperty<? super CUJO,?> displayProperty) {
        this.displayProperty = displayProperty;
        setDisplayField( displayProperty!=null
                       ? displayProperty.getName()
                       : "name"
                       );
    }

    public CCriterion<CUJO> getAditionalCriterion() {
        return aditionalCriterion;
    }

    /** Add newe Criterion to the default Query */
    public void addCriterion(CCriterion<CUJO> aditionalCriterion) {
        this.aditionalCriterion = aditionalCriterion;
    }

    /** Add newe Criterion to the default Query and reload the Store. */
    public void addCriterionNLoad(CCriterion<CUJO> aditionalCriterion) {
        this.aditionalCriterion = aditionalCriterion;
        this.reloadStore();
    }

    /** Reload the Store */
    public void reloadStore() {
        this.getStore().getLoader().load();
    }

    /** Build new query */
    final protected CQuery getCQuery() {
        CQuery result = getDefaultCQuery();
        CCriterion crn = getAditionalCriterion();
        if (crn!=null) {
            result.addCriterion(crn);
        }
        result.setRelations(loadRelations);
        return result;
    }


    /** Action On Change for a dependency implementation. */
    public abstract void onChange(CUJO selectedValue);

    /** Create a default Query */
    public abstract CQuery<CUJO> getDefaultCQuery();

    /** Request to load object relations. Default value is false. */
    public int getLoadRelations() {
        return loadRelations;
    }

    /** Request to load object relations. Default value is 0 : load no relations. */
    public void setLoadRelations(int depth) {
        this.loadRelations = depth;
    }
}
