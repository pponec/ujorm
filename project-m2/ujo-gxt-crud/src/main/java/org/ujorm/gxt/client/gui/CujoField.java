/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
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
