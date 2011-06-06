package org.ujorm.gxt.client.cbo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.CujoPropertyList;

public class CMenuItem extends AbstractCujo implements Serializable {

    protected static CujoPropertyList pl = list(CMenuItem.class);
    /** PrimaryKey */
    public static final CujoProperty<CMenuItem, Long> id = pl.newProperty("id", Long.class);
    /** Label */
    public static final CujoProperty<CMenuItem, String> label = pl.newProperty("label", String.class);
    /** Parent */
    public static final CujoProperty<CMenuItem, CMenuItem> parent = pl.newProperty("parent", CMenuItem.class);
    /** Is parent */
    public static final CujoProperty<CMenuItem, Boolean> isParent = pl.newProperty("isParent", Boolean.class);
    /** Navigation */
    public static final CujoProperty<CMenuItem, String> navigation = pl.newProperty("navigation", String.class);
    /** Index */
    public static final CujoProperty<CMenuItem, Integer> index = pl.newProperty("index", Integer.class);
    /** Active */
    public static final CujoProperty<CMenuItem, Boolean> active = pl.newProperty("active", Boolean.class);

    private CMenuItem dummy;

    @Override
    public CujoPropertyList readProperties() {
        return pl;
    }

    public static final CMenuItem create() {
        CMenuItem result = new CMenuItem();
        result.set(active, true);
        return result;
    }

    @Override
    public String toString() {
        return get(label) + " --> " + get(navigation);
    }

    public List<? extends CMenuItem> getChildren(List<? extends CMenuItem> items) {
        List<CMenuItem> children = new ArrayList<CMenuItem>();

        for (CMenuItem cMenuItem : items) {
            if (cMenuItem.get(id).equals(get(id))) {
                children.add(cMenuItem);
            }
        }
        return children;
    }
    public boolean hasChildren(List<? extends CMenuItem> items) {
        for (CMenuItem cMenuItem : items) {
            CMenuItem parentItem = cMenuItem.get(parent);
            if (parentItem != null && parentItem.get(CMenuItem.id).equals(get(id))){
                return true;
            }
        }
        return false;
    }
}
