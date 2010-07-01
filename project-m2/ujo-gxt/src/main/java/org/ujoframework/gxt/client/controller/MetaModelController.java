/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujoframework.gxt.client.controller;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;
import org.ujoframework.gxt.client.InitItems;
import org.ujoframework.gxt.client.PropertyMetadata;
import org.ujoframework.gxt.client.cquery.CQuery;

/**
 *
 * @author gola
 */
public interface MetaModelController {

    public void getMetaModel(List<CQuery> properties, AsyncCallback<List<PropertyMetadata>> callback);

    public void getEnums(AsyncCallback<InitItems> callback);
    
}
