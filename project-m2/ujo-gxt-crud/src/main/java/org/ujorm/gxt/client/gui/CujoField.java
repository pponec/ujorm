/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.widget.form.TextField;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoProperty;

/**
 *
 * @author Pelc Dobroslav
 */
public class CujoField<CUJO extends Cujo> extends TextField<CUJO> {

    protected CUJO cujo;
    protected CujoProperty displayField;
    private Runnable afterValeuChangeCommand;

    public CujoField(CUJO cujo, CujoProperty displayField, Runnable afterValeuChangeCommand) {
        super();
        this.cujo = cujo;
        this.displayField = displayField;
    }

    @Override
    public void setValue(CUJO cujo) {
        this.cujo = cujo;
        super.setValue(cujo);
        if (getAfterValeuChangeCommand() != null) {
            getAfterValeuChangeCommand().run();
        }
    }

    @Override
    public CUJO getValue() {
        return cujo;
    }

    public Runnable getAfterValeuChangeCommand() {
        return afterValeuChangeCommand;
    }

    public void setAfterValeuChangeCommand(Runnable afterValeuChangeCommand) {
        this.afterValeuChangeCommand = afterValeuChangeCommand;
    }
}
