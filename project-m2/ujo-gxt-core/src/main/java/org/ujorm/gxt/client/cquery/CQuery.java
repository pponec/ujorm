/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */
package org.ujorm.gxt.client.cquery;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoManager;
import org.ujorm.gxt.client.CujoModel;
import org.ujorm.gxt.client.CujoProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Customer CQuery
 * @author Pavel Ponec
 */
public class CQuery<UJO extends Cujo> implements Serializable {

    private static final long serialVersionUID = 42010L;
    //
    transient private Class type;
    transient private ColumnModel columnModel;
    private String autoExpandColumn;
    private String typeName;
    transient private List<CujoProperty<UJO, ?>> orderBy = new ArrayList<CujoProperty<UJO, ?>>(1);
    private List<String> orderByName = new ArrayList<String>(1);
    private List<String> orderByDirect = new ArrayList<String>(1);
    private CCriterion<UJO> criterion;
    /** Depth to Load relations. Value 0 means no relations, value 1 means load the first level of relations. */
    private int relations = 1;
    private int columnOrderSize = 0;
    private int offset = 0;
    private int limit = -1;
    private int fetchSize = -1;
    /** The attribute for a common use. */
    private String context;

    protected CQuery() {
    }

    /** Clone the parameter query */
    public CQuery(CQuery template) {
        this.type = template.type;
        this.columnModel = template.columnModel;
        this.autoExpandColumn = template.autoExpandColumn;
        this.typeName = template.typeName;
        this.orderBy = new ArrayList(template.orderBy);
        this.orderByName = new ArrayList(template.orderByName);
        this.orderByDirect = new ArrayList(template.orderByDirect);
        this.criterion = template.criterion;
        this.relations = template.relations;
        this.columnOrderSize = template.columnOrderSize;
        this.offset = template.offset;
        this.limit = template.limit;
        this.fetchSize = template.fetchSize;
        this.context = template.context;
    }

    /** Constructor creates a default ColumnModel */
    public CQuery(Class<? extends UJO> type) {
        this(type, CujoManager.find(type).createColumnModel());
    }

    public CQuery(Class<? extends UJO> type, ColumnModel columnModel) {
        this.type = type;
        this.typeName = type.getName();
        this.columnModel = columnModel;
    }

    /** The initialization for a serialization. */
    private void prepareSerialization() {
        orderByName.clear();
        orderByDirect.clear();
        for (CujoProperty p : orderBy) {
            orderByName.add(p.getName());
            orderByDirect.add(p.isAscending() ? "a" : "d");
        }
    }

    public Class getType() {
        if (type == null) {
            type = typeName.getClass();
        }
        return type;
    }

    public String getTypeName() {
        return typeName;
    }

    public CCriterion<UJO> getCriterion() {
        return criterion;
    }

    public void setCriterion(CCriterion<UJO> criterion) {
        this.criterion = criterion;
    }

    /** Get the order item list. The method returns a not null result always. */
    final public List<CujoProperty<UJO, ?>> getOrderBy() {
        return orderBy;
    }

    /** Set an order of the rows by a SQL ORDER BY phrase. */
    public CQuery<UJO> orderBy(CujoProperty<UJO, ?> orderItem) {
        return orderByMany(new CujoProperty[]{orderItem});
    }

    /** Set an order of the rows by a SQL ORDER BY phrase. */
    public CQuery<UJO> orderBy(CujoProperty<UJO, ?> orderItem1, CujoProperty<UJO, ?> orderItem2) {
        return orderByMany(new CujoProperty[]{orderItem1, orderItem2});
    }

    /** Set an order of the rows by a SQL ORDER BY phrase. */
    public CQuery<UJO> orderBy(CujoProperty<UJO, ?> orderItem1, CujoProperty<UJO, ?> orderItem2, CujoProperty<UJO, ?> orderItem3) {
        return orderByMany(new CujoProperty[]{orderItem1, orderItem2, orderItem3});
    }

    /** Set an order of the rows by a SQL ORDER BY phrase.
     * <br/>WARNING: the parameters are not type checked.
     */
    @SuppressWarnings("unchecked")
    public CQuery<UJO> orderByMany(CujoProperty... orderItems) {
        this.orderBy = new ArrayList(Math.max(orderItems.length, 4));
        for (final CujoProperty p : orderItems) {
            this.orderBy.add(p);
        }
        prepareSerialization();
        return this;
    }

    /** Set an order of the rows by a SQL ORDER BY phrase.
     * WARNING: the list items are not type checked. If you need an item chacking,
     * use the method {@link #addOrderBy(org.ujorm.CujoProperty)} rather.
     * @see #addOrderBy(org.ujorm.CujoProperty)
     */
    @SuppressWarnings("unchecked")
    public CQuery<UJO> orderBy(Collection<CujoProperty> orderItems) {
        if (orderItems == null) {
            return orderByMany(); // empty sorting
        } else {
            this.orderBy.clear();
            this.orderBy.addAll((Collection) orderItems);
        }
        prepareSerialization();
        return this;
    }

    /** Add an item to the end of order list. */
    public CQuery<UJO> addOrderBy(CujoProperty<UJO, ?> property) {
        orderBy.add(property);
        prepareSerialization();
        return this;
    }

    /** Returns all direct properties.  */
    public CujoProperty[] readProperties() {
        return CujoManager.find(type).getProperties();
    }

    /** Returns the column model */
    public ColumnModel getColumnModel() {
        return columnModel;
    }

    /** Returns the column model */
    public CujoModel getCujoModel() {
        return (CujoModel) columnModel;
    }

    /** Returns the ColumnConfig by property  */
    public ColumnConfig getColumnConfig(CujoProperty property) {
        return getCujoModel().getConfig(property);
    }

    /** Is restored the object after serialization? */
    public boolean isRestored() {
        return this.type != null;
    }

    /** Restore the internal data after the serialiation.
     * @param aType Use the code <code>Class.forName(query.getTypeName())</code>
     */
    @SuppressWarnings("unchecked")
    public void restore(Class aType) {
        if (isRestored()) {
            return;
        }

        this.type = aType;
        if (criterion != null) {
            criterion.restore(type);
        }
        orderBy = new ArrayList<CujoProperty<UJO, ?>>(orderByName.size());

        for (int i = 0; i < orderByName.size(); i++) {
            String t = orderByName.get(i);
            boolean desc = "d".equals(orderByDirect.get(i));

            CujoProperty cp = CujoManager.findIndirectProperty(type, t);
            if (desc) {
                cp = cp.descending();
            }
            orderBy.add(cp);
        }
    }

    /** Factory creates a default ColumnModel */
    public static <T extends Cujo> CQuery<T> newInstance(Class<? extends T> type) {
        return new CQuery<T>(type);
    }

    /**
     * Factory creates a clienta query with required properties
     * @param type The base query class
     * @param properties An array of table columns. The null or empty array means show all Properties of the base class.
     * @return Client Query.
     */
    public static <T extends Cujo> CQuery<T> newInstance(Class<? extends T> type, CujoProperty<T,?> ... properties) {
        return properties==null || properties.length==0
          ? new CQuery<T>(type)
          : new CQuery<T>(type, new CujoModel(properties))
          ;
    }

    /** Factory creates a clienta query with a required ColumnModel */
    public static <T extends Cujo> CQuery<T> newInstance(Class<? extends T> type, ColumnModel columnModel) {
        return new CQuery<T>(type, columnModel);
    }

    @Override
    public String toString() {

        if (!isRestored()) {
            return "[CQuery is not restored yet]";
        }

        String result = getType().getName();
        result = result.substring(1 + result.lastIndexOf('.'));
        if (criterion != null) {
            result += ' ' + criterion.toString();
        }
        return result;
    }

    /** Request to load relatins to requered depth. 
     * Value 0 means no relations, value 1 means load the first level of relations.
     */
    public int getRelations() {
        return relations;
    }

    /** Request to load relatins to requered depth. 
     * Value 0 means no relations, value 1 means load the first level of relations.
     */
    public void setRelations(int relations) {
        this.relations = relations;
    }

    public void clearColumOrder() {
        columnOrderSize = 0;
    }

    public String getAutoExpandColumn() {
        return autoExpandColumn;
    }

    public void setAutoExpandColumn(String autoExpandColumn) {
        this.autoExpandColumn = autoExpandColumn;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    /** The attribute for a common use. */
    public String getContext() {
        return context;
    }

    /** The attribute for a common use. */
    public void setContext(String context) {
        this.context = context;
    }

    public void addColumOrder(CujoProperty cujo) {

        int actualPIndex = getColumnModel().findColumnIndex(cujo.getName());
        //getCujoModel().getPropertyList().findProperty(cujo.getName()).getIndex();

        String h = new String();
        for (ColumnConfig columnConfig2 : getColumnModel().getColumns()) {

            h = h + "--" + columnConfig2.getHeader();
        }

        //change also in cujo model ?

        CujoProperty[] cpl = getCujoModel().getPropertyList().toArray(new CujoProperty[0]);
        CujoProperty tmp = cpl[actualPIndex];
        cpl[actualPIndex] = cpl[columnOrderSize];
        cpl[columnOrderSize] = tmp;

        //change colum config - swap in list ?
        List<ColumnConfig> ccl = getColumnModel().getColumns();
        ColumnConfig tmpcc1 = ccl.get(actualPIndex);
        ColumnConfig tmpcc2 = ccl.get(columnOrderSize);

        ccl.remove(actualPIndex);
        ccl.remove(columnOrderSize);

        ccl.add(columnOrderSize, tmpcc1);
        ccl.add(actualPIndex, tmpcc2);



        //
        h = new String();
        for (ColumnConfig columnConfig2 : getColumnModel().getColumns()) {

            h = h + "--" + columnConfig2.getHeader();
        }

        columnOrderSize++;
    }

    /** Add a new Criterion */
    public void addCriterion(CCriterion<UJO> criterion) {
        this.criterion = this.criterion!=null
            ? this.criterion.and(criterion)
            : criterion
            ;
    }


}
