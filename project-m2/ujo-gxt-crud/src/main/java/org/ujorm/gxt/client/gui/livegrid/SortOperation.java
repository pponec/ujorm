/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
