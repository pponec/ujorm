/*
 *  Copyright 2009-2016 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujorm.orm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.core.UjoManager;
import org.ujorm.criterion.BinaryCriterion;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.criterion.ValueCriterion;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaTable;
import static org.ujorm.core.UjoTools.SPACE;

/**
 * SQL Criterion Decoder.
 * @author Pavel Ponec
 * @composed 1 - 1 SqlDialect
 */
public class CriterionDecoder {

    final protected OrmHandler handler;
    final protected SqlDialect dialect;

    final protected Criterion criterion;
    final protected List<Key> orderBy;
    /** List of the non-null criterion values */
    final protected List<ValueCriterion> values;
    /** List of the nullable criterion values */
    final protected List<ValueCriterion> nullValues;
    /** All table set where a predicable order is required (by inserts) */
    final protected Set<TableWrapper> tables;
    final protected MetaTable baseTable;
    /** Relations */
    final List<Relation> relations = new ArrayList<Relation>();
    /** The WHERE condition in SQL format */
    protected final String where;

    /**
     * Constructor
     * @param criterion Criterion non-null
     * @param baseTable Base ORM table model
     */
    public CriterionDecoder(Criterion criterion, MetaTable baseTable) {
        this(criterion, baseTable, null);
    }

    /**
     * Constructor
     * @param criterion Criterion non-null
     * @param baseTable Base ORM table model
     * @param orderByItems The order item list is not mandatory (can be null).
     */
    public CriterionDecoder(Criterion criterion, MetaTable baseTable, List<Key> orderByItems) {
        final MetaDatabase database = baseTable.getDatabase();
        this.baseTable = baseTable;
        this.criterion = criterion;
        this.dialect = database.getDialect();
        this.orderBy = orderByItems;
        this.handler = database.getOrmHandler();
        this.values = new ArrayList<>();
        this.nullValues = new ArrayList<>();
        this.tables = new LinkedHashSet<>(); // Predicable order is required
        this.tables.add(baseTable);
        this.where = initWhere();
    }

    /**
     * Unpack criterion and write value conditions to SQL buffer
     * @param c The non-null criterion
     * @param sql SQL output buffer;
     */
    protected void unpack(final Criterion c, final StringBuilder sql) {
        if (c.isBinary()) {
            unpackBinary((BinaryCriterion)c, sql);
        } else try {
            final ValueCriterion origCriterion = ((ValueCriterion) c).freeze();
            final ValueCriterion valueCriterion = dialect.printCriterion(origCriterion, sql);
            if (valueCriterion != null) {
                values.add(valueCriterion);
            } else {
                nullValues.add(origCriterion);
            }
        } catch (RuntimeException | IOException e) {
            throw new IllegalUjormException("Unpack failed for criterion: " + c, e);
        }
    }

    /** Unpack criterion. */
    @SuppressWarnings("fallthrough")
    private void unpackBinary(final BinaryCriterion eb, final StringBuilder sql) {

        boolean or = false;
        switch (eb.getOperator()) {
            case OR:
                or = true;
            case AND:
                if (or) sql.append(" (");
                unpack(eb.getLeftNode(), sql);
                sql.append(SPACE);
                sql.append(eb.getOperator().name());
                sql.append(SPACE);
                unpack(eb.getRightNode(), sql);
                if (or) sql.append(") ");
                break;
            case NOT:
                sql.append(SPACE);
                sql.append(eb.getOperator().name());
                sql.append(" (");
                unpack(eb.getRightNode(), sql); // same criterion in both nodes
                sql.append(") ");
                break;
            default:
                String message = "Operator is not supported in the SQL statement: " + eb.getOperator();
                throw new UnsupportedOperationException(message);
        }
    }

    /** Returns a column count */
    public int getColumnCount() {
        return values.size();
    }

    /** Returns direct column or throw an exception */
    public MetaColumn getColumn(int i) throws IllegalArgumentException {
        final Key p = values.get(i).getLeftNode();
        final MetaColumn ormColumn = (MetaColumn) handler.findColumnModel(p, true);
        return ormColumn;
    }

    /** Returns operator. */
    public Operator getOperator(int i) {
        final Operator result = values.get(i).getOperator();
        return result;
    }

    /** Returns value */
    public Object getValue(int i) {
        Object result = values.get(i).getRightNode();
        return result;
    }

    /** Returns an extended value to the SQL statement */
    public Object getValueExtended(int i) {
        ValueCriterion crit = values.get(i);
        Object value = crit.getRightNode();

        if (value==null) {
            return value;
        }
        if (crit.isInsensitive()) {
            // Note: ! "Geß".toUpperCase().equals("GES")
            value = value.toString().toLowerCase();
        }
        switch (crit.getOperator()) {
            case CONTAINS:
            case CONTAINS_CASE_INSENSITIVE:
                return "%"+value+"%";
            case STARTS:
            case STARTS_CASE_INSENSITIVE:
                return value+"%";
            case ENDS:
            case ENDS_CASE_INSENSITIVE:
                return "%"+value;
            default:
                return value;
        }
    }

    /** Returns the criterion from constructor. */
    public Criterion getCriterion() {
        return criterion;
    }

//    /** Returns the relation criterion for the INNER JOIN phase. */
//    public Criterion getCriterionForRelations() {
//        if (criterion instanceof BinaryCriterion) {
//            return ((BinaryCriterion) criterion).getLeftNode();
//        }
//        return null;
//    }
//
//    /** Returns the relation criterion for the INNER JOIN phase. */
//    public Criterion getCriterionForConditions() {
//        if (criterion instanceof BinaryCriterion) {
//            return ((BinaryCriterion) criterion).getRightNode();
//        }
//        return null;
//    }

    /** Returns a SQL WHERE 'expression' of an empty string if no condition is found. */
    protected final String initWhere() {
        final StringBuilder result = new StringBuilder(64);
        writeConditions(result);
        writeRelations(result);
        return result.toString();
    }

    /** Returns a SQL WHERE 'expression' of an empty string if no condition is found. */
    public String getWhere() {
        return where;
    }

    /** Is the SQL statement empty?  */
    public boolean isEmpty() {
        return getWhere().isEmpty();
    }

    /** Returns the first direct key. */
    public Key getBaseProperty() {
        Key result = null;
        for (ValueCriterion eval : values) {
            if (eval.getLeftNode()!=null) {
                result = eval.getLeftNode();
                break;
            }
        }
        while(UjoManager.isCompositeKey(result)) {
            result = ((CompositeKey)result).getFirstKey();
        }
        return result;
    }

   /** Unpack binary criterions and write all value conditions */
    @SuppressWarnings("unchecked")
    protected void writeConditions(final StringBuilder sql) {
        if (criterion != null) {
            unpack(criterion, sql);
        }
    }

    /** Write the relation conditions */
    @SuppressWarnings("unchecked")
    protected void writeRelations(final StringBuilder sql) {
        if (criterion==null && orderBy==null) {
            return;
        }

        final Collection<AliasKey> relations = getPropertyRelations();

        for (AliasKey key : relations) try {
            final ColumnWrapper fk1 = key.getColumn(handler);
            final MetaTable tab1 = fk1.getModel().getTable();
            final ColumnWrapper pk2 = fk1.getModel().getForeignColumns().get(0).addTableAlias(key.aliasTo);
            final MetaTable tab2 = pk2.getModel().getTable();
            //
            tables.add(tab1.addAlias(key.getAliasFrom()));
            tables.add(tab2.addAlias(key.getAliasTo()));

            this.relations.add(new Relation(fk1, pk2));
        } catch (RuntimeException e) {
            throw new IllegalUjormException(e.getMessage(), e);
        }

    }

    /** Returns the unique direct key relation set with the predicable order (by inserts). */
    protected Collection<AliasKey> getPropertyRelations() {
        final Set<AliasKey> result = new LinkedHashSet<>(); // the predicable order is required (by inserts)
        final ArrayList<ValueCriterion> allValues = new ArrayList<>
                (values.size() + nullValues.size());
        allValues.addAll(values);
        allValues.addAll(nullValues);

        for (ValueCriterion<?> value : allValues) {
            final Key<?,?> p1 = value.getLeftNode();
            if (p1 != null) {
                AliasKey.addRelations(p1, result);
                final Object p2 = value.getRightNode();
                if (p2 instanceof CompositeKey) {
                    AliasKey.addRelations((CompositeKey)p2, result);
                }
            }
        }

        // Get relations from the 'order by':
        if (orderBy != null) {
            for (Key p1 : orderBy) {
                AliasKey.addRelations((CompositeKey) p1, result);
            }
        }

        return result;
    }

    /** Get Base Table */
    public MetaTable getBaseTable() {
        return baseTable;
    }

    /** Returns all participated tables include the parameter table. */
    public int getTableCount() {
        return tables.size();
    }

    /** Returns all participated tables include the parameter table. */
    public TableWrapper[] getTables() {
        return tables.toArray(new TableWrapper[tables.size()]);
    }

    /** Returns all participated tables include the parameter table. The 'baseTable' is on the first position always. */
    public TableWrapper[] getTablesSorted() {
        final TableWrapper[] result = getTables();
        if (result.length > 1 && result[0] != baseTable) {
            for (int i = result.length - 1; i >= 1; i--) {
                if (result[i] == baseTable) {
                    result[i] = result[0];
                    result[0] = baseTable;
                }
            }
        }
        return result;
    }

    /** Returns handler */
    public OrmHandler getHandler() {
        return handler;
    }

    /** Return relations */
    public List<Relation> getRelations() {
        return relations;
    }

    /** Returns the criterion */
    @Override
    public String toString() {
        return criterion!=null ? criterion.toString() : null ;
    }

    /** Relation definition */
    public static final class Relation {
        private final ColumnWrapper left;
        private final ColumnWrapper right;

        public Relation(final ColumnWrapper left, final ColumnWrapper right) {
            this.left = left;
            this.right = right;
        }

        /** Get left column */
        public ColumnWrapper getLeft() {
            return left;
        }

        /** Get right column */
        public ColumnWrapper getRight() {
            return right;
        }
    }
}
