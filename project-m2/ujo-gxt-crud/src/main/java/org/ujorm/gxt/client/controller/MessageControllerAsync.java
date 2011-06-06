package org.ujorm.gxt.client.controller;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import java.util.List;

public interface MessageControllerAsync {

    void loadMessageAfterLogIn(java.lang.String locale, AsyncCallback<java.util.Map<java.lang.String, java.lang.String>> callback);

    void loadMessageBeforeLogIn(java.lang.String locale, AsyncCallback<java.util.Map<java.lang.String, java.lang.String>> callback);

    void loadMessageByKey(java.lang.String key, AsyncCallback<java.util.Map<java.lang.String, java.lang.String>> callback);

    void listLocales(AsyncCallback<java.util.List<java.lang.String>> callback);

    void reloadLocales(AsyncCallback<Void> callback);

    void listStore(AsyncCallback<java.util.List<com.extjs.gxt.ui.client.data.ModelData>> callback);

    void save(com.extjs.gxt.ui.client.data.ModelData modelData, AsyncCallback<Void> callback);

    void update(com.extjs.gxt.ui.client.data.ModelData modelData, AsyncCallback<Void> callback);

    void delete(java.util.List<com.extjs.gxt.ui.client.data.ModelData> modelData, AsyncCallback<Void> callback);

    /** Pomocná třída poskytující metodou get() instanci controlleru pro asynchronní volání */
    public static class Pool {

        private static MessageControllerAsync instance = null;

        public static MessageControllerAsync get() {
            if (instance == null) {
                instance = (MessageControllerAsync) GWT.create(MessageController.class);
                ServiceDefTarget target = (ServiceDefTarget) instance;
                target.setServiceEntryPoint( GWT.getModuleBaseURL() + "controller/MessageController.rpc" );
            }
            return instance;
        }
    }
}
