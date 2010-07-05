/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujoframework.gxt.client;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extended Table Column Model
 * @author Pavel Ponec
 */
public class CujoModel extends ColumnModel {

    public static final DateTimeFormat DEFAULT_DATE_FORMAT = GWT.isClient()
        ? DateTimeFormat.getFormat("yyyy-MM-dd HH:mm")
        : null
        ;

    public static final DateTimeFormat DEFAULT_DAY_FORMAT = GWT.isClient()
        ? DateTimeFormat.getFormat("yyyy-MM-dd")
        : null
        ;


    private final Map<CujoProperty, ColumnConfig> map = new HashMap<CujoProperty, ColumnConfig>();
    private final CujoPropertyList propertyList;

    public CujoModel(CujoPropertyList propertyList) {
        this(propertyList, propertyList.getProperties());
    }

    public CujoModel(CujoPropertyList propertyList, CujoProperty... properties) {
        super(createPropertyList(propertyList));
        this.propertyList = propertyList;

        for (ColumnConfig config : super.getColumns()) {
            CujoProperty property = propertyList.findProperty(config.getId());
            map.put(property, config);
        }
    }

    private static List<ColumnConfig> createPropertyList(CujoPropertyList propertyList) {
        List<ColumnConfig> result = new ArrayList<ColumnConfig>();
        PropertyMetadataProvider metadataProvider = ClientClassConfig.getInstance().getPropertyMedatata();

        for (CujoProperty p : propertyList.getProperties()) {
            PropertyMetadata metadata = metadataProvider != null
                ? metadataProvider.getAlways(p)
                : new PropertyMetadata(p);

            ColumnConfig config = new ColumnConfig();
            config.setId(p.getName());
            config.setHeader(metadata.getColumnLabel());
            config.setToolTip(metadata.getDescription());
            config.setWidth(100); // [px]
            config.setAlignment(p.isTypeOf(Number.class)
                ? HorizontalAlignment.RIGHT
                : HorizontalAlignment.LEFT);
            config.setSortable(true);

            if (p.isTypeOf(java.util.Date.class)) {
                config.setDateTimeFormat(DEFAULT_DATE_FORMAT);
            }
            if (p.isTypeOf(java.sql.Date.class)) {
                config.setDateTimeFormat(DEFAULT_DAY_FORMAT);
            }
            if (p.isTypeOf(Boolean.class)) {
                config.setAlignment(HorizontalAlignment.CENTER);
                config.setRenderer(new GridCellRenderer() {
                    @Override
                    public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore store, Grid grid) {
                        Boolean b = (Boolean) model.get(property);
                        return b ? "ok" : '-';
                    }
                });
            }
            if ("id".equals(p.getName())) {
                config.setHidden(true);
            }
            if ("active".equals(p.getName())) {
                config.setHidden(true);
            }


            result.add(config);
        }
        return result;
    }

    /** Returns config by property  */
    public ColumnConfig getConfig(CujoProperty property) {
        return map.get(property);
    }

    /** Returns the UjoPropertylist */
    public CujoPropertyList getPropertyList() {
        return propertyList;
    }
}
