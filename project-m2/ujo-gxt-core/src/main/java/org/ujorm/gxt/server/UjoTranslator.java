/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */
package org.ujorm.gxt.server;

import java.awt.Color;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.ujorm.Ujo;
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
import org.ujorm.core.UjoCoder;
import org.ujorm.core.UjoIterator;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.ValueExportable;
import org.ujorm.gxt.client.CEnum;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.CujoPropertyList;
import org.ujorm.gxt.client.cquery.CQuery;
import org.ujorm.gxt.client.tools.ColorGxt;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaRelation2Many;

/**
 * Translate an iterator of Ujo to the ListStore&lt;CUJO&gt;.
 * @author Pavel Ponec
 */
public final class UjoTranslator<CUJO extends Cujo> {

    private IServerClassConfig serverClassConfig;
    private List<PropContainer> properties;
    private CujoPropertyList cujoPropertyList;
    private UjoTranslatorCallback<CUJO>[] callBacks;
    private Map<UjoProperty, UjoTranslator> relationMap;
    private Session dummySession;
    private UjoCoder ujoCoder;

    public UjoTranslator(CUJO clientObject, int relations, IServerClassConfig serverClassConfig) {
        this(clientObject.readProperties(),
                UjoManager.getInstance().readProperties(serverClassConfig.getServerClass(clientObject.getClass().getName())),
                relations,
                null,
                serverClassConfig);
    }

    /**
     * Create new Translator
     * @throws RuntimeException Any erorr due create new instance.
     */
    public UjoTranslator(Class<CUJO> clientType,
            Class<Ujo> serverType,
            int relations,
            IServerClassConfig serverClassConfig)
            throws RuntimeException {
        this( createInstance(clientType).readProperties()
            , UjoManager.getInstance().readProperties(serverType)
            , relations
            , null
            , serverClassConfig
            );
    }

    /** Create new instance. */
    private static <T extends Cujo> T createInstance(Class clientType) throws IllegalStateException {
        try {
            return (T) clientType.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Can't create instance for " + clientType, e);
        }
    }

    /***
     * Create instance with a first level of relations request.
     */
    @SuppressWarnings("unchecked")
    public UjoTranslator(CujoPropertyList cujoPropertyList
         , UjoPropertyList ujoPropertyList
         , IServerClassConfig serverClassConfig) {
        this(cujoPropertyList, ujoPropertyList, 1, null, serverClassConfig);
    }

    /**
     * Constructor
     * @param relations The depth o relations, the value 0 means no relation is converted.
     */
    @SuppressWarnings("unchecked")
    UjoTranslator(CujoPropertyList cujoPropertyList
          , UjoPropertyList ujoPropertyList
          , int relations
          , Set<UjoProperty> myPropertySet
          , IServerClassConfig serverClassConfig)
          throws RuntimeException {

        this.cujoPropertyList = cujoPropertyList;
        this.properties = new ArrayList<PropContainer>(ujoPropertyList.size());
        this.serverClassConfig = serverClassConfig;
        this.dummySession = Session.newClosedSession(serverClassConfig.getHandler());
        if (relations > 0) {
            relationMap = new HashMap<UjoProperty, UjoTranslator>();
        }

        for (UjoProperty p1 : ujoPropertyList) {
            if (myPropertySet == null || myPropertySet.contains(p1)) {
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
                                UjoTranslator ut = new UjoTranslator(p2.getType(), p1.getType(), relations-1, serverClassConfig);
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
                        if (Color.class.isAssignableFrom(p1.getType())) {
                            if (p2.isTypeOf(String.class)) {
                                properties.add(new PropContainer(p1, p2, pk));
                                break;
                            }
                            if (p2.isTypeOf(ColorGxt.class)) {
                                properties.add(new PropContainer(p1, p2, pk));
                                break;
                            }
                        }
                        if (ValueExportable.class.isAssignableFrom(p1.getType())) {
                            if (p2.isTypeOf(String.class)) {
                                properties.add(new PropContainer(p1, p2, pk));
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (properties.isEmpty()) {
            throw new NoSuchElementException("No matching properties of the " + ujoPropertyList.getType());
        }
    }

    /** Get List of CUJOs */
    @SuppressWarnings({"unchecked"})
    public ListExt<CUJO> translate(UjoIterator<? extends Ujo> iterator) {
        return translate((Iterator)iterator);
    }

    /** Get List of CUJOs */
    @SuppressWarnings({"unchecked"})
    public ListExt<CUJO> translate(Iterable<? extends Ujo> bos) {
        return translate(bos.iterator());
    }

    /** Get List of CUJOs */
    @SuppressWarnings({"unchecked"})
    public ListExt<CUJO> translate(Iterator<? extends Ujo> bos) {
        return translate(bos, 0, Integer.MAX_VALUE, null);
    }

    /** Get List of CUJOs
     * @param bos Business object
     * @param offset Offset is used for optimization only, it does not restrict the BOS count.
     * @param limit Limit is used for optimization only, it does not restrict the BOS count.
     * @param query If query is NULL than the Total Line Count is calculatd from bos.size() .
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public ListExt<CUJO> translate(Iterator<? extends Ujo> bos, int offset, int limit, Query query) {

        ListExt<CUJO> result = new ListExt<CUJO>();

        if (bos == null) {
            return result;
        }

        int count = 0;
         while (bos.hasNext()) {
            ++count;
            Ujo ujo = bos.next();
            final CUJO cujo = translateToClient(ujo);
            result.add(cujo);
        }

        if (offset<=0 && count<limit || query==null) {
            result.setTotalCount(count);
        } else {
            // Call a new database request:
            result.setTotalCount((int) query.getCount());
        }

        return result;
    }

    /** Translate a one server object to a client object. */
    @SuppressWarnings("unchecked")
    public CUJO translateToClient(Ujo ujo) {
        CUJO cujo = (CUJO) newCujo(cujoPropertyList);

        for (PropContainer pc : properties) {
            if (!isRelations() && pc.p1.isTypeOf(Ujo.class)) {
                continue;
            }
            Object value = pc.p1.getValue(ujo);

            if (value == null) {
                // No conversion
            } else if (pc.p1.isTypeOf(Enum.class)) {
                Enum enumValue = (Enum) value;
                if (pc.p2.isTypeOf(CEnum.class)) {
                    value = new CEnum(enumValue.ordinal(), enumValue.name());
                } else {
                    value = enumValue.name();
                }
            } else if (pc.p1.isTypeOf(ValueExportable.class)) {
                final ValueExportable exportableValue = (ValueExportable) value;
                value = exportableValue.exportToString();
            } else if (isRelations() && pc.p1.isTypeOf(Ujo.class)) {
                UjoTranslator ut = relationMap.get(pc.p1);
                value = ut.translateToClient((Ujo) value);
            } else if (pc.p1.isTypeOf(Color.class)) {
                Color colorValue = (Color) value;
                if (pc.p2.isTypeOf(ColorGxt.class)) {
                    value = new ColorGxt(getUjoCoder().encodeValue(colorValue, false));
                } else {
                    value = getUjoCoder().encodeValue(colorValue, false);
                }
            }
            cujo.set(pc.p1.getName(), value);
        }

        copyToClient(ujo, cujo);
        return cujo;
    }

    /** Returns an instance of the related server class. Ujo properties of the related clasec are ignored. <>
     * If the primary key is not NULL than the result object has got an dummy session assigned.
     */
    @SuppressWarnings({"unchecked"})
    public <T extends OrmUjo> T translateToServer(CUJO cujo) {

        if (cujo == null) {
            return null;
        }

        T result = (T) serverClassConfig.newServerObject(cujo.getClass().getName());
        boolean insert = true;

        for (PropContainer pc : properties) {

            // Set a session to control a modified properties.
            final boolean hasSession = result.readSession() != null;
            if (pc.pk == hasSession) {
                result.writeSession(pc.pk ? null : dummySession);
            }

            Object value = cujo.get(pc.p2);
            if (pc.pk) {
                insert = value==null;
            }

            if (value == null) {
                // OK ;
            }
            else if (pc.p1.isTypeOf(java.sql.Date.class)) {
                // A workaround for the feature: http://code.google.com/p/google-web-toolkit/issues/detail?id=87 :
                if (value instanceof java.sql.Date) {
                    // OK
                } else if (value instanceof Date) {
                    value = new java.sql.Date(((Date)value).getTime());
                } else {
                    throw new IllegalArgumentException("Value: " + value);
                }
            }
            else if (pc.p1.isTypeOf(Enum.class)) {
                // Copy ENUM:
                if (value instanceof CEnum) {
                    value = Enum.valueOf(pc.p1.getType(), ((CEnum) value).getName());
                } else {
                    value = Enum.valueOf(pc.p1.getType(), (String) value);
                }
            }
            else if (pc.p1.isTypeOf(ValueExportable.class)) {
                try {
                    // Copy ValueExportable:
                    value = pc.p1.getType().getConstructor(String.class).newInstance((String)value);
                } catch (Exception e) {
                    throw new IllegalStateException("Can't create instance of the " + pc.p1.getType() + " for value " + value);
                }
            }
            else if (pc.p1.isTypeOf(Color.class)) {
                // Copy Color:
                value = getUjoCoder().decodeValue(pc.p1, value.toString(), null);
            }
            else if (isRelations() && relationMap.containsKey(pc.p1)) {
                UjoTranslator ut = relationMap.get(pc.p1);
                value = ut.translateToServer((Cujo) value);
            }
            else if (pc.p1.isTypeOf(OrmUjo.class)) {
                // Copy a foreign key:
                final String pkPropertyName = "id"; // TODO: find id by a meta-model
                try {
                    Object idValue = ((Cujo) value).get(pkPropertyName);
                    OrmUjo ormValue = (OrmUjo) pc.p1.getType().newInstance();
                    UjoProperty p = ormValue.readProperties().find(pkPropertyName, true);
                    p.setValue(ormValue, idValue);
                    copyToServer((Cujo) value, ormValue);
                    value = ormValue;
                } catch (Exception e) {
                    throw new IllegalStateException("Can't create instance for " + pc.p1.getType(), e);
                }
            }
            result.writeValue(pc.p1, value);
        }

        if (insert) {
            result.writeSession(null);
        }
        return (T) result;
    }

    /** Copy Ujo to CUJO. Overwrite the method by your special idea. */
    protected void copyToClient(Ujo ujo, CUJO cujo) {
        if (callBacks != null) {
            for (UjoTranslatorCallback<CUJO> cb : callBacks) {
                cb.copy(ujo, cujo);
            }
        }
    }

    /** Copy Ujo to CUJO. Overwrite the method by your special idea. */
    protected void copyToServer(Cujo cujo, Ujo ujo) {
        // Overwrite it
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

    /**
     * Create new instance
     * @param relations The depth o relations, the value 0 means no relation is converted.
     */
    @SuppressWarnings("unchecked")
    public static <CUJO extends Cujo> UjoTranslator<CUJO> newInstance(
            CQuery clientQuery,
            Class serverClass,
            int relations,
            IServerClassConfig serverClassConfig) {
        try {
            Class clientType = Class.forName(clientQuery.getTypeName());
            return new UjoTranslator<CUJO>(clientType, (Class<Ujo>) serverClass, relations, serverClassConfig);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Create new instance
     * @param relations The depth o relations, the value 0 means no relation is converted.
     */
    @SuppressWarnings("unchecked")
    public static <CUJO extends Cujo> UjoTranslator<CUJO> newInstance(
            Class clientClass,
            Class serverClass,
            int relations,
            IServerClassConfig serverClassConfig) {
        try {
            return new UjoTranslator<CUJO>(clientClass, (Class<Ujo>) serverClass, relations, serverClassConfig);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <CUJO extends Cujo> UjoTranslator<CUJO> newInstance(
            String aTargetType,
            Class<? extends Ujo> sourceType,
            int relations,
            IServerClassConfig serverClassConfig) {
        try {
            Class targetType = Class.forName(aTargetType);
            return new UjoTranslator<CUJO>((Class<CUJO>) targetType, (Class<Ujo>) sourceType, relations, serverClassConfig);
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

    /** Provide the UjoCoder instance */
    protected UjoCoder getUjoCoder() {
        if (ujoCoder==null) {
            ujoCoder = UjoManager.getInstance().getCoder();
        }
        return ujoCoder;
    }
}
