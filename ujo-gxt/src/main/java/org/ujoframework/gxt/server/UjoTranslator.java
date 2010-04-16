/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujoframework.gxt.server;

import org.ujoframework.gxt.client.CEnum;
import org.ujoframework.gxt.client.Cujo;
import org.ujoframework.gxt.client.CujoProperty;
import org.ujoframework.gxt.client.CujoPropertyList;
import org.ujoframework.gxt.client.cquery.CQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.UjoPropertyList;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.core.UjoManager;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.Session;
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.orm.metaModel.MetaRelation2Many;

/**
 * Translate an iterator of Ujo to the ListStore&lt;CUJO&gt;.
 * @author Pavel Ponec
 */
public class UjoTranslator<CUJO extends Cujo> {

    private IServerClassConfig serverClassConfig;
    private List<PropContainer> properties;
    private CujoPropertyList cujoPropertyList;
    private UjoTranslatorCallback<CUJO>[] callBacks;
    private Map<UjoProperty, UjoTranslator> relationMap;
    private List<Ujo> ujos = null;
    private Session dummySession;

    public UjoTranslator(CUJO clientObject, boolean relations, IServerClassConfig serverClassConfig) {
        this(clientObject.readProperties(),
                UjoManager.getInstance().readProperties(serverClassConfig.getServerClass(clientObject.getClass().getName())),
                relations,
                serverClassConfig);
    }

    public UjoTranslator(Class<CUJO> clientType, Class<Ujo> serverType, boolean relations, IServerClassConfig serverClassConfig) throws Exception {
        this(clientType.newInstance().readProperties(),
                UjoManager.getInstance().readProperties(serverType),
                relations,
                serverClassConfig);
    }

    @SuppressWarnings("unchecked")
    public UjoTranslator(CujoPropertyList cujoPropertyList,
            UjoPropertyList ujoPropertyList,
            IServerClassConfig serverClassConfig
            ) {
        this(cujoPropertyList, ujoPropertyList, true, serverClassConfig);
    }

    @SuppressWarnings("unchecked")
    public UjoTranslator(CujoPropertyList cujoPropertyList,
            UjoPropertyList ujoPropertyList,
            boolean relations,
            IServerClassConfig serverClassConfig)
            throws NoSuchElementException {

        this.cujoPropertyList = cujoPropertyList;
        this.properties = new ArrayList<PropContainer>(ujoPropertyList.size());
        this.serverClassConfig = serverClassConfig;
        this.dummySession = Session.newClosedSession(serverClassConfig.getHandler());
        if (relations) {
            relationMap = new HashMap<UjoProperty, UjoTranslator>();
        }

        for (UjoProperty p1 : ujoPropertyList) {

            for (CujoProperty p2 : cujoPropertyList) {
                if (p1.getName().equals(p2.getName())) {
                    boolean pk = isPrimaryKey(p1);

                    if (p2.getType().isAssignableFrom(p1.getType())) {
                        properties.add(new PropContainer(p1, p2, pk));
                        break;
                    }
                    if (isRelations() && p1.isTypeOf(Ujo.class) && Cujo.class.isAssignableFrom(p2.getType())) {
                        try {
                            properties.add(new PropContainer(p1, p2, pk));
                            UjoTranslator ut = new UjoTranslator(p2.getType(), p1.getType(), false, serverClassConfig);
                            relationMap.put(p1, ut);
                            break;
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }
                    if (Enum.class.isAssignableFrom(p1.getType())) {
                        if (p2.isTypeOf(String.class)) {
                            properties.add(new PropContainer(p1, p2, pk));
                            break;
                        }
                        if (p2.isTypeOf(CEnum.class)) {
                            properties.add(new PropContainer(p1, p2, pk));
                            break;
                        }
                    }
                }
            }
        }

        if (properties.size() == 0) {
            throw new NoSuchElementException("No matching properties of the " + ujoPropertyList.getType());
        }
    }

    /** Get List of CUJOs */
    @SuppressWarnings({"unchecked"})
    public ListExt<CUJO> translate(UjoIterator<? extends Ujo> bos) {
        return translate(bos, 0, Integer.MAX_VALUE);
    }

    /** Get List of CUJOs */
    @SuppressWarnings({"unchecked"})
    public ListExt<CUJO> translate(UjoIterator<? extends Ujo> bos, int offset, int limit) {

        ListExt<CUJO> result = new ListExt<CUJO>();


        if (bos == null) {
            return result;
        }

        result.setTotalCount((int) bos.count());
        bos.skip(offset);

        for (Ujo ujo : bos) {
            if (limit-- <= 0) {
                bos.close();
                break;
            }

            CUJO cujo = translateToClient(ujo);
            result.add(cujo);

        }
        return result;
    }

    /** Translate a server object to a client object. */
    @SuppressWarnings("unchecked")
    public CUJO translateToClient(Ujo ujo) {
        CUJO cujo = (CUJO) newCujo(cujoPropertyList);

        for (PropContainer pc : properties) {
            if (!isRelations() && pc.p1.isTypeOf(Ujo.class)) {
                continue;
            }
            Object value = pc.p1.getValue(ujo);

            if (value != null && pc.p1.isTypeOf(Enum.class)) {
                Enum enumValue = (Enum) value;
                if (pc.p2.isTypeOf(CEnum.class)) {
                    value = new CEnum(enumValue.ordinal(), enumValue.name());
                } else {
                    value = enumValue.name();
                }
            }
            if (value != null && isRelations() && pc.p1.isTypeOf(Ujo.class)) {
                if (ujos == null) {
                    ujos = new ArrayList<Ujo>(1);
                } else {
                    ujos.clear();
                }
                ujos.add((Ujo) value);
                UjoTranslator ut = relationMap.get(pc.p1);
                value = ut.translate(UjoIterator.getInstance(ujos)).list().get(0);
            }
            cujo.set(pc.p1.getName(), value);

        }

        copy(ujo, cujo);
        return cujo;
    }

    /** Returns an instance of the related server class. Ujo properties are ignored. */
    @SuppressWarnings({"unchecked"})
    public <T extends OrmUjo> T translateToServer(CUJO cujo) {

        if (cujo == null) {
            return null;
        }

        OrmUjo result = serverClassConfig.newServerObject(cujo.getClass().getName());

        for (PropContainer pc : properties) {

            final boolean hasSession = result.readSession()!=null;
            if (pc.pk == hasSession) {
                result.writeSession(pc.pk ? null : dummySession);
            }

            Object value = cujo.get(pc.p2);

            if (value != null && pc.p1.isTypeOf(Enum.class)) {
                // Copy ENUM:
                if (value instanceof CEnum) {
                    value = Enum.valueOf(pc.p1.getType(), ((CEnum) value).getName());
                } else {
                    value = Enum.valueOf(pc.p1.getType(), (String) value);
                }
            }
            if (value != null && pc.p1.isTypeOf(OrmUjo.class)) {
                // Copy a foreign key:
                final String pkPropertyName = "id";
                try {
                    Object idValue = ((Cujo) value).get(pkPropertyName);
                    OrmUjo ormValue = (OrmUjo) pc.p1.getType().newInstance();
                    UjoProperty p = ormValue.readProperties().find(pkPropertyName, true);
                    p.setValue(ormValue, idValue);
                    value = ormValue;
                } catch (Exception e) {
                    throw new IllegalStateException("Can't create instance for " + pc.p1.getType(), e);
                }
            }
            result.writeValue(pc.p1, value);
        }
        
        result.writeSession(null);
        return (T) result;
    }

    /** Copy Ujo to CUJO. Overwrite the method by your special idea. */
    protected void copy(Ujo ujo, CUJO cujo) {
        if (callBacks != null) {
            for (UjoTranslatorCallback<CUJO> cb : callBacks) {
                cb.copy(ujo, cujo);
            }
        }
    }

    public <CUJO extends Cujo> CUJO newCujo(CujoPropertyList propertyList) {
        try {
            @SuppressWarnings("unchecked")
            CUJO result = (CUJO) propertyList.getType().newInstance();
            return result;

        } catch (Exception e) {
            throw new IllegalStateException("Can't create instance for " + propertyList.getType(), e);
        }
    }

    public UjoTranslatorCallback<CUJO>[] getCallBacks() {
        return callBacks;
    }

    public void setCallBack(UjoTranslatorCallback<CUJO>... callBack) {
        this.callBacks = callBack;
    }

    protected boolean isRelations() {
        return relationMap != null;
    }

    // ------------------ STATIC METHODS --------------------------
    @SuppressWarnings("unchecked")
    public static <CUJO extends Cujo> UjoTranslator<CUJO> newInstance(
            CQuery clientQuery,
            Class serverClass,
            boolean relations,
            IServerClassConfig serverClassConfig
            ) {
        try {
            Class clientType = Class.forName(clientQuery.getTypeName());
            return new UjoTranslator<CUJO>(clientType, (Class<Ujo>) serverClass, relations, serverClassConfig);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    @SuppressWarnings("unchecked")
    public static <CUJO extends Cujo> UjoTranslator<CUJO> newInstance(
            Class clientClass,
            Class serverClass,
            boolean relations,
            IServerClassConfig serverClassConfig) {
        try {
            return new UjoTranslator<CUJO>(clientClass, (Class<Ujo>) serverClass, relations, serverClassConfig);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** Is it a primary key? */
    private boolean isPrimaryKey(UjoProperty p1) {
        boolean result = false;
        MetaRelation2Many column = serverClassConfig.getHandler().findColumnModel(p1);
        if (column instanceof MetaColumn && ((MetaColumn) column).isPrimaryKey()) {
            result = true;
        }
        return result;
    }

    private class PropContainer {

        final UjoProperty p1;
        final CujoProperty p2;
        final boolean pk;

        public PropContainer(UjoProperty uProperty, CujoProperty cProperty, boolean pk) {
            this.p1 = uProperty;
            this.p2 = cProperty;
            this.pk = pk;
        }
    }
}