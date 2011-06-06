package org.ujorm.gxt.client.controller;

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.user.client.rpc.RemoteService;
import com.gwtincubator.security.exception.ApplicationSecurityException;
import java.util.List;
import java.util.Map;

public interface MessageController extends RemoteService {

    public Map<String, String> loadMessageAfterLogIn(String locale) throws ApplicationSecurityException;

    public Map<String, String> loadMessageBeforeLogIn(String locale);

    public Map<String, String> loadMessageByKey(String key) throws ApplicationSecurityException;

    public List<String> listLocales() throws ApplicationSecurityException;

    public void reloadLocales() throws ApplicationSecurityException;

    public List<ModelData> listStore() throws ApplicationSecurityException;

    public void save(ModelData modelData) throws ApplicationSecurityException;

    public void update(ModelData modelData) throws ApplicationSecurityException;

    public void delete(List<ModelData> modelData) throws ApplicationSecurityException;
}
