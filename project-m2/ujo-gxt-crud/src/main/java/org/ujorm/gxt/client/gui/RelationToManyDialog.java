/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2013 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */
package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.ao.ValidationMessage;
import org.ujorm.gxt.client.controller.LiveGridControllerAsync;
import org.ujorm.gxt.client.controller.TableControllerAsync;
import org.ujorm.gxt.client.cquery.CCriterion;
import org.ujorm.gxt.client.cquery.CQuery;

/**
 *
 * @author Pelc
 */
public abstract class RelationToManyDialog<CR1 extends AbstractCujo, CR2 extends AbstractCujo> extends Dialog {

    protected List<CR1> cujos;
    protected CujoProperty<CR1, String> relationProperty;
    protected CujoProperty<CR2, String> displayProperty;
    protected FieldSet fieldSet;
    protected Map<Long, CheckBox> binding = new HashMap<Long, CheckBox>();

    public abstract String translate(String viewId, String name);

    public RelationToManyDialog(List<CR1> cujos) {
        this.cujos = cujos;
        setWidth(400);
        setHeight(200);
        setHeading(getHeadingTitle());
        setScrollMode(Scroll.AUTO);
        setHideOnButtonClick(true);
    }

    protected void addFieldSet() {
        fieldSet = initFieldset(getFieldSetTitle(), true);
        add(fieldSet, new MarginData(6));
    }

    protected void addHideListener() {
        //
        addListener(Events.Hide, new Listener<WindowEvent>() {

            @Override
            public void handleEvent(WindowEvent be) {
                final Button button = be.getButtonClicked();
                if (button != null && Dialog.OK.equals(button.getItemId())) {
                    doSave();
                } else {
                    Info.display(translate("", "Info"), translate("", "Close-without-save"));
                }
            }
        });
    }

    protected void addSearchBox() {
        CujoBox box = initSearchBox();
        add(box, new MarginData(6));
    }

    // načtení předchozích sdílení...
    protected void controllPreviousData() throws NumberFormatException {
        CCriterion<CR2> sharedCrit = null;
        final String relations = cujos.get(0).get(relationProperty);
        if (relations != null) {
            for (String relationId : relations.split("; ")) {
                if (relationId.length()>0) {
                    CujoProperty idProperty = cujos.get(0).readProperties().findProperty("id");
                    CCriterion userCrit = CCriterion.where(idProperty, Long.parseLong(relationId));
                    sharedCrit = sharedCrit == null ? userCrit : sharedCrit.or(userCrit);
                }
            }
        }
        if (sharedCrit != null) {
            CQuery<CR2> q = getQuery();
            q.addCriterion(sharedCrit);
            TableControllerAsync.Util.getInstance().getCujoList(q, new AsyncCallback<List<Cujo>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Info.display(translate("", "Error"), translate("", "Relations-was-not-readed"));
                }

                @Override
                public void onSuccess(List<Cujo> result) {
                    for (Cujo cujo : result) {
                        addToFieldSet(cujo);
                    }
                    fieldSet.layout();
                    // TODO: předat předchozí nasetované relations ostatních uživatelů...
                }
            });
        }
    }

    protected String getHeadingTitle() {
        return translate("", "Relation-to-many");
    }

    protected String getFieldSetTitle() {
        return translate("", "Selected-values");
    }

    protected FieldSet initFieldset(String title, boolean expanded) {
        FieldSet set = new FieldSet();
        //
        set.setHeading(title);
        set.setCollapsible(true);
        set.setExpanded(expanded);
        return set;
    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        addSearchBox();
        addFieldSet();
        controllPreviousData();
        addHideListener();
    }

    protected CujoBox initSearchBox() {
        return new CujoBox(displayProperty, LiveGridControllerAsync.Pool.get()) {

            @Override
            public String translate(String parent, String name) {
                return RelationToManyDialog.this.translate(parent, name);
            }

            @Override
            public void onChange(Cujo selectedValue) {
                addToFieldSet(selectedValue);
                fieldSet.layout();
            }

            @Override
            public CQuery getDefaultCQuery() {
                CQuery<CR2> result = getQuery();
                return result;
            }

            @Override
            protected void onRender(Element parent, int index) {
                super.onRender(parent, index);
            }
        };
    }

    public abstract CQuery<CR2> getQuery();

    protected void addToFieldSet(Cujo selectedValue) {
        CR2 selectedCujo = (CR2) selectedValue;
        final Long relationId = selectedCujo.get("id");
        if (binding.get(relationId) == null) {
            CheckBox box = new CheckBox();
            box.setBoxLabel(selectedCujo.get(displayProperty));
            box.setValue(true);
            fieldSet.add(box, new MarginData(0, 0, 0, 3));
            binding.put(relationId, box);
        }
    }

    protected void doSave() {
        final String oldRelations = cujos.get(0).get(relationProperty);
        String relations = oldRelations == null
                ? ""
                : oldRelations;
        for (Long cujoRelation : binding.keySet()) {
            CheckBox box = binding.get(cujoRelation);
            if (box.getValue()) {
                relations = relations.length()==0
                        ? "" + cujoRelation
                        : relations + "; " + cujoRelation;
            }
        }
        for (CR1 cujo : cujos) {
            cujo.set(relationProperty, relations);
        }
        TableControllerAsync.Util.getInstance().saveOrUpdate(cujos, false, new AsyncCallback<ValidationMessage>() {

            @Override
            public void onFailure(Throwable caught) {
                Info.display(translate("", "Info"), translate("", "Error-by-saving"));
            }

            @Override
            public void onSuccess(ValidationMessage result) {
                Info.display(translate("", "Info"), translate("", "Successful-saved"));
            }
        });
    }
}
