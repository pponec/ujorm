/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujorm.gxt.client.gui.editdialog;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Element;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.CujoProperty;

/**
 *
 * @author Dobroslav Pelc
 */
public abstract class GroupingEditDialog<CUJO extends AbstractCujo> extends EditDialog<CUJO> {

    public static final String PROPERTY_ABSTRACT = "property_abstract";
    protected String viewId = "";
    public static final String FIELD_SET_GROUP_ID = "FIELD_SET_GROUP_ID";
    protected Map<String, List<CujoProperty>> groupsMap;

    public GroupingEditDialog(CUJO cujo, boolean newState) {
        this(cujo, newState, "edit-dialog");
    }

    public GroupingEditDialog(CUJO cujo, boolean newState, String viewId) {
        this(cujo, newState, "edit-dialog", null);
    }

    public GroupingEditDialog(CUJO cujo, boolean newState, String viewId, Runnable afterInitCommand) {
        super(cujo, newState);
        this.viewId = viewId == null ? "" : viewId;
        this.afterSubmitCommand = afterInitCommand;
        this.COMPONENT_WIDTH = 300;
    }

    @Override
    protected void onRender(Element parent, int pos) {
        groupsMap = initGroupsMap(getWhiteList());
        super.onRender(parent, pos);
    }

    @Override
    protected LayoutContainer initCenterPanel(CUJO cujo, int relations) {
        LayoutContainer centerPanel = new LayoutContainer(new RowLayout());
        createFieldSets(centerPanel, cujo, relations);
        return centerPanel;
    }

    /** Before call this method, you have to set cujo (data value), tools (buttons, help) and groups to map... */


    protected void createFieldSets(LayoutContainer panel, CUJO cujo, int relations) {
        for (String groupId : groupsMap.keySet()) {
            FieldSet fieldSet = initFieldset(translate(viewId, groupId), true);
            fillSet(cujo, fieldSet, groupsMap.get(groupId), relations);
            panel.add(fieldSet, new MarginData(5));
        }
    }

    protected void fillSet(CUJO cujo, FieldSet set, List<CujoProperty> props, int relations) {
        for (CujoProperty cujoProperty : props) {
            // druha cast podminky pro pripad, ze neni definovane zobrazeni
            if (getTool(cujoProperty) != null) {
                set.add(createPanelRow(cujo, cujoProperty, relations));
            }
        }
    }

    protected FieldSet initFieldset(String title, boolean expanded) {
//        FormLayout layout = new FormLayout();
//        layout.setLabelWidth(170);
//        layout.setDefaultWidth(350);
//        layout.setLabelAlign(LabelAlign.RIGHT);
        //
        FieldSet set = new FieldSet();
        set.setLayout(new RowLayout());
        //
        set.setHeading(title);
        set.setCollapsible(true);
        set.setExpanded(expanded);
        return set;
    }

    public Map<String, List<CujoProperty>> initGroupsMap(List<CujoProperty> keys) {
        HashMap<String, List<CujoProperty>> resultMap = new HashMap<String, List<CujoProperty>>();

        for (CujoProperty cujoProperty : keys) {
            if (resultMap.get(PROPERTY_ABSTRACT) == null) {
                resultMap.put(PROPERTY_ABSTRACT, new ArrayList<CujoProperty>());
            }
            resultMap.get(PROPERTY_ABSTRACT).add(cujoProperty);
        }

        return resultMap;
    }
}
