/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2013 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */
package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.controller.LiveGridControllerAsync;
import org.ujorm.gxt.client.cquery.CCriterion;
import org.ujorm.gxt.client.cquery.CQuery;

/**
 * Component type of drop down for a CUJO object.
 * @author Pelc, Ponec
 * @see CujoField
 */
public abstract class CujoBox<CUJO extends Cujo, CONTROLLER extends LiveGridControllerAsync> extends ComboBox<CUJO> {

    /** The property to display in the drop-down list */
    protected CujoProperty displayProperty;
    protected CCriterion<CUJO> aditionalCriterion;
    /** Load Object relations */
    protected int loadRelations = 1;

    public abstract String translate(String parent, String name);
    private CCriterion crit;

    public CujoBox(CujoProperty<? super CUJO, ?> displayProperty, final CONTROLLER controller) {
        setDisplayProperty(displayProperty);

        RpcProxy<PagingLoadResult<CUJO>> proxy = new RpcProxy<PagingLoadResult<CUJO>>() {

            @Override
            public void load(final Object loadConfig, final AsyncCallback<PagingLoadResult<CUJO>> callback) {

                controller.getData(getCQuery(), true, 1, new AsyncCallback<PagingLoadResult<CUJO>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(PagingLoadResult<CUJO> result) {
                        processSuccessCallback(result, callback);
                    }
                });
            }
        };

        final PagingLoader<PagingLoadResult<CUJO>> loader = new BasePagingLoader<PagingLoadResult<CUJO>>(proxy);
        loader.setSortDir(SortDir.ASC);
        loader.setRemoteSort(true);
        store = new ListStore<CUJO>(loader);
    }

    protected void processSuccessCallback(PagingLoadResult<CUJO> data, AsyncCallback<PagingLoadResult<CUJO>> callback) {
        callback.onSuccess(data);
    }

    public static CujoBox create(final Class<? extends Cujo> cClass, final CujoProperty displayProp, final Runnable onChange, LiveGridControllerAsync controller, CCriterion crit, int relations) {
        if (cClass == null || displayProp == null) {
            return null;
        }

        CujoBox cujoBox = new CujoBox(displayProp, controller) {

            @Override
            public void onChange(Cujo selectedValue) {
                if (onChange != null) {
                    onChange.run();
                }
            }

            @Override
            public CQuery getDefaultCQuery() {
                CQuery q = CQuery.newInstance(cClass);
                // order by
                if (displayProp != null) {
                    q.addOrderBy(displayProp);
                }
                // where
                if (getCrit() != null) {
                    q.setCriterion(getCrit());
                }
                return q;
            }

            @Override
            public String translate(String parent, String name) {
                // TODO: distribuovat ven...
                return name;
            }
        };
        cujoBox.setCrit(crit);
        return cujoBox;
    }

    public CCriterion getCrit() {
        return crit;
    }

    public void setCrit(CCriterion crit) {
        this.crit = crit;
    }

    public final void setDisplayProperty(CujoProperty<? super CUJO, ?> displayProperty) {
        setDisplayField(displayProperty != null
                ? displayProperty.getName()
                : "name");
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        addSelectionChangedListener(new SelectionChangedListener<CUJO>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<CUJO> se) {
                onChange(se.getSelectedItem());
            }
        });
        setEmptyText(translate("", "selectValue"));

        // TODO: remove me...
        getListView().setStyleAttribute("overflow", "auto");

        setTypeAhead(true);
        setMinChars(1);
        addKeyListener(new KeyListener() {

            @Override
            public void componentKeyDown(ComponentEvent event) {
                if (event.getKeyCode() == KeyCodes.KEY_BACKSPACE) {
                    setValue(null);
                }
                super.componentKeyDown(event);
            }
        });
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
        this.getStore().getLoader().load();
    }

    /** Build new query */
    final protected CQuery getCQuery() {
        CQuery result = getDefaultCQuery();
        CCriterion crn = getAditionalCriterion();
        if (crn != null) {
            result.addCriterion(crn);
        }
        result.setRelations(loadRelations);
        if (displayProperty != null) {
            result.orderBy(displayProperty);
        }
        return result;
    }

    /** Action On Change for a dependency implementation. */
    public abstract void onChange(CUJO selectedValue);

    /** Create a default Query */
    public abstract CQuery<CUJO> getDefaultCQuery();

    /** Request to load object relations. Default value is false. */
    public int isLoadRelations() {
        return loadRelations;
    }

    /** Request to load object relations. Default value is false. */
    public void setLoadRelations(int loadRelations) {
        this.loadRelations = loadRelations;
    }
}
