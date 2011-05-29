/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.controller;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;
import org.ujorm.gxt.client.CMessageException;
import org.ujorm.gxt.client.InitItems;
import org.ujorm.gxt.client.PropertyMetadata;
import org.ujorm.gxt.client.cquery.CQuery;
import org.ujorm.gxt.client.tools.ClientSerializableEnvelope;

/**
 *
 * @author gola
 */
public interface MetaModelController {

    public void getMetaModel(List<CQuery> properties, AsyncCallback<List<PropertyMetadata>> callback) throws CMessageException;

    public void getEnums(AsyncCallback<InitItems> callback) throws CMessageException;

    public void typeWorkaround(ClientSerializableEnvelope o, AsyncCallback<ClientSerializableEnvelope> envelop);
    
}
