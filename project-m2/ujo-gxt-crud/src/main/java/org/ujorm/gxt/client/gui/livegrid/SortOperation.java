/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2013 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */
package org.ujorm.gxt.client.gui.livegrid;

import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.controller.LiveGridControllerAsync;
import org.ujorm.gxt.client.cquery.CQuery;

/**
 *
 * @author Pelc
 */
public interface SortOperation<CUJO extends AbstractCujo> {

    public LiveGridControllerAsync getController();

    public Class<CUJO> getCujoType();

    public CQuery<CUJO> getQuery();

    public void setReload(Boolean reload);
}
