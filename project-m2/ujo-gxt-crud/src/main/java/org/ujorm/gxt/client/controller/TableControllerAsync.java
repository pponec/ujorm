/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */
package org.ujorm.gxt.client.controller;

import com.extjs.gxt.ui.client.data.PagingLoadResult;
import org.ujorm.gxt.client.InitItems;
import org.ujorm.gxt.client.PropertyMetadata;
import java.util.List;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.ao.ValidationMessage;
import org.ujorm.gxt.client.cquery.CQuery;
import org.ujorm.gxt.client.tools.ClientSerializableEnvelope;

public interface TableControllerAsync extends MetaModelController
{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see com.jworksheet.application.client.controller.TableController
     */
    void getCujoList( CQuery query, AsyncCallback<java.util.List<Cujo>> callback);


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see com.jworksheet.application.client.controller.TableController
     */
    void getDbRows( CQuery query, com.extjs.gxt.ui.client.data.PagingLoadConfig config, AsyncCallback<com.extjs.gxt.ui.client.data.PagingLoadResult<Cujo>> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see com.jworksheet.application.client.controller.TableController
     */
    void saveOrUpdate( Cujo cujo, boolean create, AsyncCallback<ValidationMessage> callback );

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see com.jworksheet.application.client.controller.TableController
     */
    void delete( java.util.List<? extends Cujo> cujos, int deleteType, AsyncCallback<Void> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see com.jworksheet.application.client.controller.TableController
     */
    @Override
    void getEnums( AsyncCallback<InitItems> callback );

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see com.jworksheet.application.client.controller.TableController
     */
    @Override
    void typeWorkaround(ClientSerializableEnvelope o, AsyncCallback<ClientSerializableEnvelope> callback );

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see com.jworksheet.application.client.controller.TableController
     */
    @Override
    void getMetaModel( java.util.List<CQuery> properties, AsyncCallback<java.util.List<PropertyMetadata>> callback );

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see com.jworksheet.application.client.controller.TableController
     */
    void pink(AsyncCallback<Void> callback);


    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    public static class Util 
    { 
        private static TableControllerAsync instance;

        public static TableControllerAsync getInstance()
        {
            if ( instance == null )
            {
                instance = (TableControllerAsync) GWT.create( TableController.class );
                ServiceDefTarget target = (ServiceDefTarget) instance;
                target.setServiceEntryPoint( GWT.getModuleBaseURL() + "controller/TableController.rpc" );
            }
            return instance;
        }
    }
}
