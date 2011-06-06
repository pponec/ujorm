/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujorm.gxt.client.gui.editdialog;

import org.ujorm.gxt.client.gui.CujoBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import java.util.ArrayList;
import java.util.List;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.controller.LiveGridControllerAsync;
import org.ujorm.gxt.client.cquery.CCriterion;
import org.ujorm.gxt.client.gui.livegrid.LiveGridPanel;

/**
 *
 * @author Pelc Dobroslav
 */
public class EditDialogTool<CONTROLLER extends LiveGridControllerAsync> {

    private List<Button> buttonBefore = new ArrayList<Button>();
    private List<Button> buttonBehind = new ArrayList<Button>();
    private CujoProperty displayProperty;
    private CujoBox cujoBox;
    private Class<? extends Cujo> cClass;
    private CONTROLLER controller;
    private CCriterion crit;
    private boolean editable;
    private boolean mandatory;
    private LiveGridPanel relation;
    private boolean translation;
    private boolean cujoEditor;
    private Runnable afterValeuChangeCommand;
    private boolean save;

    public boolean isSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

    public EditDialogTool() {
        this(true, true);
    }

    public EditDialogTool(boolean editable, boolean mandatory) {
        this(editable, mandatory, false);
    }

    public EditDialogTool(boolean editable, boolean mandatory, boolean translation) {
        this.editable = editable;
        this.mandatory = mandatory;
        this.translation = translation;
    }

    public EditDialogTool(boolean editable, boolean mandatory, LiveGridPanel relation) {
        this(editable, mandatory, relation, false);
    }

    public EditDialogTool(boolean editable, boolean mandatory, LiveGridPanel relation, boolean cujoEditor) {
        this.editable = editable;
        this.mandatory = mandatory;
        this.relation = relation;
        this.cujoEditor = cujoEditor;
    }

    public EditDialogTool(boolean editable, boolean mandatory, LiveGridPanel relation, CujoProperty displayField) {
        this.editable = editable;
        this.mandatory = mandatory;
        this.relation = relation;
        this.displayProperty = displayField;
    }

    public LiveGridPanel getRelation() {
        return relation;
    }

    public void setRelation(LiveGridPanel relation) {
        this.relation = relation;
    }

    public boolean isTranslation() {
        return translation;
    }

    public void setTranslation(boolean translation) {
        this.translation = translation;
    }

    public List<Button> getButtonBefore() {
        return buttonBefore;
    }

    public void setButtonBefore(List<Button> buttonBefore) {
        this.buttonBefore = buttonBefore;
    }

    public List<Button> getButtonBehind() {
        return buttonBehind;
    }

    public void setButtonBehind(List<Button> buttonBehind) {
        this.buttonBehind = buttonBehind;
    }

    public String getDisplayField() {
        return displayProperty != null ? displayProperty.getName() : "name";
    }

    public CujoProperty getDisplayProperty() {
        return displayProperty;
    }

    public void setDisplayProperty(CujoProperty displayProperty) {
        this.displayProperty = displayProperty;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    boolean isCujoEditor() {
        return cujoEditor;
    }

    public Runnable getAfterValeuChangeCommand() {
        return afterValeuChangeCommand;
    }

    public void setAfterValeuChangeCommand(Runnable afterValeuChangeCommand) {
        this.afterValeuChangeCommand = afterValeuChangeCommand;
    }

    public boolean isCujoBox() {
        return cujoBox != null;
    }

    public CujoBox getCujoBox() {
        if (cujoBox == null) {
            cujoBox = CujoBox.create(cClass, displayProperty, afterValeuChangeCommand, getController(), crit, 1);
        }
        return cujoBox;
    }

    public void setCujoBox(CujoBox cujoBox) {
        this.cujoBox = cujoBox;
    }

    

    public void setDisplayClass(Class<? extends Cujo> cClass) {
        this.cClass = cClass;
    }

    public void setDisplayDataController(CONTROLLER controller) {
        this.controller = controller;
    }

    public CONTROLLER getController() {
        return controller == null ? (CONTROLLER) LiveGridControllerAsync.Pool.get() : controller;
    }

    public void setCrit(CCriterion crit) {
        this.crit = crit;
    }

    public void setcClass(Class<? extends Cujo> cClass) {
        this.cClass = cClass;
    }

    public void setController(CONTROLLER controller) {
        this.controller = controller;
    }

    public void setCujoEditor(boolean cujoEditor) {
        this.cujoEditor = cujoEditor;
    }

    public CCriterion getCrit() {
        return crit;
    }
}
