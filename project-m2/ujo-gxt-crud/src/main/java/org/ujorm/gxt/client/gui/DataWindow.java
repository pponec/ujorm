/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.user.client.Element;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.controller.TableControllerAsync;

/**
 * The abstract edit dialog.
 * @author Ponec
 */
public class DataWindow<CUJO extends Cujo> extends Window {

    /** The TRUE value means that a parent panel must reload data. */
    protected boolean changedData = false;
    //
    private static TableControllerAsync controller;

    /** Reset the 'changedData' */
    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        changedData = false;
    }

    /** Is the window closed by a succesful submit action?
     * The TRUE value means that a parent panel must reload data.
     */
    public boolean isChangedData() {
        return changedData;
    }

    @SuppressWarnings("unchecked")
    public TableControllerAsync getController() {
        if (controller == null) {
            controller = TableControllerAsync.Util.getInstance();
        }
        return controller;
    }
}
