package org.ujorm.gxt.client.controller;

import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.cbo.CTableInfo;
import org.ujorm.gxt.client.cquery.CQuery;

public interface LiveGridController<CUJO extends AbstractCujo>
        extends RemoteService {

    public Boolean saveTableInfo(String clientClassNamem, String columns, String sort);

    public CTableInfo loadTableInfo(String clientClassNamem);

    public PagingLoadResult<CUJO> getData(CQuery<CUJO> query, Boolean reloadCount, Integer relations);

    public Integer getSearchedRow(String clientClassNamem, Boolean descending, String orderBy, Long searched, CQuery query);

    public Integer getSearchedRow(String clientClassNamem, Boolean descending, String orderBy, Integer searched, CQuery query);

    public Integer getSearchedRow(String clientClassNamem, Boolean descending, String orderBy, Integer searched, Long id, CQuery query);

    public Integer getSearchedRow(String clientClassNamem, Boolean descending, String orderBy, String searched, CQuery query);

    public Integer getSearchedRow(String clientClassNamem, Boolean descending, String orderBy, Cujo searched, Long id, CQuery query);

    public Integer getSearchedRow(String clientClassNamem, Boolean descending, String orderBy, String searched, Long id, CQuery query);

    public Boolean isQuery(String clientClassNamem, Long searched, String orderBy, Boolean descending, CQuery query);

    public Boolean isQuery(String clientClassNamem, Integer searched, String orderBy, Boolean descending, CQuery query);

    public Boolean isQuery(String clientClassNamem, String searched, String orderBy, Boolean descending, CQuery query);

    public String delete(CUJO bo);
}
