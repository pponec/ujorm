/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.data.ModelProcessor;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import java.util.HashMap;
import java.util.Map;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.cbo.CMenuItem;
import org.ujorm.gxt.client.cquery.CQuery;
import org.ujorm.gxt.client.cquery.CCriterion;

/**
 *
 * @author Pelc Dobroslav
 */
public abstract class CujoTreePanelImpl<CUJO extends CMenuItem> extends CujoTreePanel<CUJO> {

    private Map<String, AbstractImagePrototype> icons;

    public CujoTreePanelImpl() {
        this(null);
    }

    public abstract String translate(String parent, String key);

    public CujoTreePanelImpl(CQuery<CUJO> query) {
        super(query, CMenuItem.label);
        icons = initIcons();
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        final String MENU_ITEM_SHOW_KEY = "translate_key";
        tree.setDisplayProperty(MENU_ITEM_SHOW_KEY);
        tree.setModelProcessor(new ModelProcessor<CUJO>() {

            @Override
            public CUJO prepareData(CUJO model) {
                model.set(MENU_ITEM_SHOW_KEY, translate("", model.get(CMenuItem.label)));
                return model;
            }
        });
        tree.setAutoExpand(true);
    }

    @Override
    public CQuery<CUJO> getDefaultQuery() {
        CQuery<CUJO> cQuery = new CQuery<CUJO>((Class<? extends CUJO>) CMenuItem.class);

        CUJO parent = getParentItem();

        CCriterion<CUJO> parentCriterion = (CCriterion<CUJO>) CCriterion.where(CMenuItem.parent, parent);

        cQuery.setCriterion(parentCriterion);
        if (query != null) {
            if (query.getCriterion() != null) {
                cQuery.setCriterion(cQuery.getCriterion().and(query.getCriterion()));
            }
            if (query.getOrderBy() != null) {
                for (CujoProperty<CUJO, ?> cujoProperty : query.getOrderBy()) {
                cQuery.addOrderBy(cujoProperty);
                }
            }
        }
        cQuery.addOrderBy((CujoProperty<CUJO, ?>) CMenuItem.index);
        return cQuery;
    }

    @Override
    public boolean hasItemChildren(CUJO parent) {

        Boolean isParent = parent.get(CMenuItem.isParent);
        return isParent == null ? false : isParent;
    }

    @Override
    public String getPanelTitle() {
        return translate("", "main-menu");
    }

    @Override
    public void afterStoreInit(TreeStore<CUJO> loader) {
        // TODO:
    }

    @Override
    protected AbstractImagePrototype availableItemIcon(CUJO model) {
        String navKey = model.get(CMenuItem.navigation);
        AbstractImagePrototype icon = icons.get(navKey);
        return icon != null ? icon : super.availableItemIcon(model);
    }

    protected Map<String, AbstractImagePrototype> initIcons() {
        Map<String, AbstractImagePrototype> map = new HashMap<String, AbstractImagePrototype>();
        return map;
    }
}
