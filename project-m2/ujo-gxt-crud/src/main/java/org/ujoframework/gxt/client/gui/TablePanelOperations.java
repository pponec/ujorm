/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujoframework.gxt.client.gui;

import org.ujoframework.gxt.client.Cujo;

/**
 *
 * @author Pelc Dobroslav
 */
public interface TablePanelOperations<CUJO extends Cujo> {

    /** Select row and give focus to row by index */
    public void selectRow();

    public void setUpdateValue(CUJO cujo);

    public void setUpdateValue(Long cujoId);

}
