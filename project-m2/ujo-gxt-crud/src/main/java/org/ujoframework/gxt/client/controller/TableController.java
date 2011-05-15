/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujoframework.gxt.client.controller;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import java.util.List;
import org.ujoframework.gxt.client.CMessageException;
import org.ujoframework.gxt.client.Cujo;
import org.ujoframework.gxt.client.InitItems;
import org.ujoframework.gxt.client.PropertyMetadata;
import org.ujoframework.gxt.client.ao.ValidationMessage;
import org.ujoframework.gxt.client.cquery.CQuery;
import org.ujoframework.gxt.client.tools.ClientSerializableEnvelope;

//@RemoteServiceRelativePath(TableController.CONTROLLER)
//maven Generate this

public interface TableController extends RemoteService {

    public static final int DELETE_AUTO = 1;
    public static final int DELETE_LOGICAL = 2;
    public static final int DELETE_PHYSICAL = 3;

    /** Returns rows of the table by the parameters. */
    public List<Cujo> getCujoList(CQuery query) throws CMessageException;

    /** Returns rows of the table by the parameters. */
    public PagingLoadResult<Cujo> getDbRows(CQuery query, PagingLoadConfig config) throws CMessageException;

    /** Save or Update selected CUJO */
    public ValidationMessage saveOrUpdate(Cujo cujo, boolean create) throws CMessageException;

    /** Delete the row by an action type.
     * If action type quals to DELETE_AUTO thean and there is exists an attribute 'active' typove of Boolean,
     * than the method delete ther row logically else physically.
     * @param actionType See the constants DELETE_*.
     * @see #DELETE_AUTO
     * @see #DELETE_LOGICAL
     * @see #DELETE_PHYSICAL
     */
    public void delete(List<? extends Cujo> cujos,  int deleteType) throws CMessageException;

    /** Returns all enum items */
    public InitItems getEnums() throws CMessageException;

    /** Returns a property MetaModel */
    public List<PropertyMetadata> getMetaModel(List<CQuery> properties) throws CMessageException;

    /* An workaround for the GXT serialization */
    public ClientSerializableEnvelope typeWorkaround(ClientSerializableEnvelope o);

    /** Pink the server */
    public void pink();

}
