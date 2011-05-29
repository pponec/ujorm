/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.gui;

import org.ujorm.gxt.client.Cujo;

/**
 *
 * @author Pelc Dobroslav
 */
public interface TablePanelOperations<CUJO extends Cujo> {

    /** Select row and give focus to row by index */
    public void selectRow();

    /** Selected Item from an Update */
    public void setUpdateValue(CUJO cujo);

    /** Selected Item from an Update */
    public void setUpdateValue(Long cujoId);

}
