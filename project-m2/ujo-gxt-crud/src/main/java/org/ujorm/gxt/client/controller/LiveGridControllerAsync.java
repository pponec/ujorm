package org.ujorm.gxt.client.controller;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.cbo.CTableInfo;
import org.ujorm.gxt.client.cquery.CQuery;

public interface LiveGridControllerAsync<CUJO extends AbstractCujo> {

    void saveTableInfo(String cujoClass, String columns, String sort, AsyncCallback<Boolean> callback);

    void loadTableInfo(String cujoClass, AsyncCallback<CTableInfo> callback);

    void getData(CQuery<CUJO> query, Boolean reloadCount, Integer relations, AsyncCallback<com.extjs.gxt.ui.client.data.PagingLoadResult<CUJO>> callback);

    void getSearchedRow(String clientClassNamem, Boolean descending, String orderBy, Cujo searched, Long id, CQuery query, AsyncCallback<Integer> callback);

    void getSearchedRow(String clientClassNamem, Boolean descending, String orderBy, String searched, CQuery query, AsyncCallback<Integer> callback);

    void getSearchedRow(String clientClassNamem, Boolean descending, String orderBy, Integer searched, CQuery query, AsyncCallback<Integer> callback);

    void getSearchedRow(String clientClassNamem, Boolean descending, String orderBy, Long id, CQuery query, AsyncCallback<Integer> callback);

    void getSearchedRow(String clientClassNamem, Boolean descending, String orderBy, String searched, Long id, CQuery query, AsyncCallback<Integer> callback);

    void getSearchedRow(String clientClassNamem, Boolean descending, String orderBy, Integer searched, Long id, CQuery query, AsyncCallback<Integer> callback);

    void isQuery(String clientClassNamem, String searched, String orderBy, Boolean descending, CQuery query, AsyncCallback<Boolean> callback);

    void isQuery(String clientClassNamem, Long searched, String orderBy, Boolean descending, CQuery query, AsyncCallback<Boolean> callback);

    void isQuery(String clientClassNamem, Integer searched, String orderBy, Boolean descending, CQuery query, AsyncCallback<Boolean> callback);

    void delete(CUJO bo, AsyncCallback<String> callback);

    /** Pomocná třída poskytující metodou get() instanci controlleru pro asynchronní volání */
    public static class Pool {

        private static LiveGridControllerAsync instance = null;

        public static LiveGridControllerAsync get() {
            if (instance == null) {
                instance = (LiveGridControllerAsync) GWT.create(LiveGridController.class);
                ServiceDefTarget target = (ServiceDefTarget) instance;
                target.setServiceEntryPoint(GWT.getModuleBaseURL() + "controller/LiveGridController.rpc");
            }
            return instance;
        }
    }
}
