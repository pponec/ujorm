package org.ujorm.gxt.client.cbo;

import java.io.Serializable;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.CujoPropertyList;

public class CTableInfo extends AbstractCujo implements Serializable {

    protected static CujoPropertyList pl = list(CTableInfo.class);
    /** PrimaryKey */
    public static final CujoProperty<CTableInfo, Long> id = pl.newProperty("id", Long.class);
    /** Columns
     *  - Jednotlivé názvy sloupců řaďte za sebe a oddělujte čárkou
     */
    public static final CujoProperty<CTableInfo, String> columns = pl.newProperty("columns", String.class);
    /** Sort
     *  - název sloupce, podle kterého uživatel nasposledy řadil
     */
    public static final CujoProperty<CTableInfo, String> sort = pl.newProperty("sort", String.class);

    @Override
    public CujoPropertyList readProperties() {
        return pl;
    }

    public static final CTableInfo create() {
        CTableInfo result = new CTableInfo();
        return result;
    }

    @Override
    public String toString() {
        return get(sort) + " --> " + get(columns);
    }
}
