/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client;

import org.ujorm.gxt.client.controller.MetaModelController;
import com.google.gwt.core.client.GWT;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ujorm.gxt.client.cquery.CCriterion;
import org.ujorm.gxt.client.cquery.COperator;
import org.ujorm.gxt.client.cquery.CQuery;

/**
 * Inicialization the CUJO objects + mapping to BO
 * @author Pavel Ponec
 */
public class ClientClassConfig {

    private static ClientClassConfig instance;

    private HashMap<String, Cujo> bosMap = new HashMap<String, Cujo>();
    private EnumItems enums;
    private PropertyMetadataProvider medatata;
    private MetaModelController service;
    private ClientCallback callback;

    /** Initializa all BOs */
    private ClientClassConfig() {
    }

    public void startUp(MetaModelController metaModelService, ClientCallback callback) {
        this.service = metaModelService;
        this.callback = callback;
        initEnums();
    }

    private void initEnums() {

        if (!GWT.isClient()) {
            return;
        }

        if (enums == null) {
            service.getEnums(new ClientCallback<InitItems>() {

                @Override
                public void onSuccess(InitItems initItems) {
                    enums = initItems.getEnumItems();
                    initMetadata(initItems.getCujos());
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void initMetadata(List<Cujo> cujos) {

        if (!GWT.isClient() || bosMap == null) {
            return;
        }

        for (Cujo cujo : cujos) {
            initCujo(cujo);
        }

        final ArrayList<CQuery> qList = new ArrayList<CQuery>();
        final ArrayList<CujoProperty> pList = new ArrayList<CujoProperty>();
        Iterator<Cujo> it = bosMap.values().iterator();

        while (it.hasNext()) {
            Cujo cujo = it.next();
            CCriterion cc = null;

            for (CujoProperty p : cujo.readProperties()) {
                pList.add(p);
                CCriterion c2 = CCriterion.where(p, COperator.EQ, (Object) null);
                if (cc == null) {
                    cc = c2;
                } else {
                    cc = cc.or(c2);
                }
            }
            CQuery cQuery = new CQuery(cujo.getClass());
            cQuery.setCriterion(cc);
            qList.add(cQuery);
        }

        service.getMetaModel(qList, new ClientCallback<List<PropertyMetadata>>() {

            @Override
            public void onSuccess(List<PropertyMetadata> result) {
                HashMap<CujoProperty, PropertyMetadata> map = new HashMap<CujoProperty, PropertyMetadata>();

                for (int i = 0; i < pList.size(); i++) {
                    if (result.get(i) != null) {
                        map.put(pList.get(i), result.get(i));
                    }
                }
                medatata = new PropertyMetadataProvider(map);
                //_bosType = null; // TODO: later maybe
                callback.onSuccess(null);
            }
        });
    }

    public void initCujo(Cujo cujo) {
        cujo.readProperties().getType(); // initialization test
        bosMap.put(cujo.getClass().getName(), cujo);
    }

    /** Initialization */
    public static ClientClassConfig getInstance() {
        if (instance == null) {
            instance = new ClientClassConfig();
        }
        return instance;
    }

    /** Is the type a cujo object? */
    public static boolean isCujo(Object object) {
        return isCujoType(object.getClass());
    }

    /** Is the type a cujo object? */
    public static boolean isCujoType(Class type) {
        return getInstance().bosMap.containsKey(type.getName());
    }

    /** Get all items for enumerator name */
    public static List<CEnum> getEnumItems(String enumName) {
        ClientClassConfig t = getInstance();

        /*
        if (t.enums==null) {
        t.enums = EnumItems.newInstance();
        }
        */
        return t.enums.getItems(enumName);
    }

    /** Create CUJO object by a class. If the class was not registered then the method returns NULL. */
    public Cujo createCujo(Class cujoType) {
        Cujo result = bosMap.get(cujoType.getName());
        if (result!=null) {
            result = result.createInstance();
        }
        return result;
    }

    /** Get all items for enumerator name */
    public static boolean isValidEnum(String enumType, String enumName) {
        ClientClassConfig t = getInstance();
        return t.enums.isValid(enumType, enumName);
    }

    /** Returns an instance of the Client Enumerator */
    public static CEnum getEnumItem(String enumType, String enumName) throws RuntimeException {
        ClientClassConfig t = getInstance();
        return t.enums.getItem(enumType, enumName);
    }

    /** Returns property metadata */
    public PropertyMetadataProvider getPropertyMedatata() {
        return medatata;
    }

    /** Returns an instance of the Client Manager */
    public CujoManager getCujoManager() {
        return new CujoManager();
    }
}