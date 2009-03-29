/*
 *  Copyright 2007 Paul Ponec
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

package org.ujoframework.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoComparator;
import org.ujoframework.core.UjoManager;
import org.ujoframework.extensions.UjoCloneable;

/**
 * An Ujo implementation of TableModel.
 * @author Pavel Ponec
 */
public class UjoTableModel<ROW extends Ujo> extends AbstractTableModel {
    
    /** A repaint header event. */
    public static final int EVENT_REPAINT_HEADER = 1000;
    
    /** Columns definition */
    protected UjoProperty[] columns;
    
    /** A data store */
    protected List<ROW> rows;
    
    /**
     * Creates a new instance of UjoTableModel
     * @param columns Columns in a required order.
     */
    public UjoTableModel(UjoProperty ... columns) {;
        this.columns = columns;
        initData();
    }
    
    /** Creates a new instance of UjoTableModel for all attributes. */
    public UjoTableModel(Class ujoType) {
        this(UjoManager.getInstance().readProperties(ujoType));
    }
    
    /** An Initialization */
    protected void initData() {
        rows = new ArrayList<ROW>();
    }
    
    /** Returns an UjoManager */
    protected UjoManager getUjoManager() {
        return UjoManager.getInstance();
    }
    
    /** Assign a table rows */
    public void setRows(List<ROW> rows) {
        if (rows==null) { throw new IllegalArgumentException("Can't assign a null value"); }
        this.rows = rows;
        fireTableDataChanged();
    }
    
    /** Get Row Count */
    public int getRowCount() {
        return rows.size();
    }
    
    /** Count of table columns. */
    public int getColumnCount() {
        return columns.length;
    }
    
    /** Convert columnIndex to a UjoProperty. */
    public UjoProperty getColumn(int columnIndex) {
        return columns[columnIndex];
    }
    
    /** Set columns into table */
    public void setColumns(UjoProperty ... columns) {
        this.columns = columns;
        fireTableStructureChanged();
    }
    
    /** Get value from cell.
     * @deprecated Use a method with column type of UjoProperty instead of.
     */
    final public Object getValueAt(int rowIndex, int columnIndex) {
        return getValueAt(rowIndex, columns[columnIndex]);
    }
    
    /** Set value to cell.
     * @deprecated Use a method with column type of UjoProperty instead of.
     */
    final public void setValueAt(Object value, int rowIndex, int columnIndex) {
        setValueAt(value, rowIndex, columns[columnIndex]);
    }
    
    /** Column Name */
    final public String getColumnName(int columnIndex) {
        return getColumnName(columns[columnIndex]);
    }
    
    /** Is the Cell Editable?
     * @deprecated Use a method with column type of UjoProperty instead of.
     */
    final public boolean isCellEditable(int rowIndex, int columnIndex) {
        return isCellEditable(rowIndex, columns[columnIndex]);
    }
    
    /**
     * Returns the most specific superclass for all the cell values in the column.
     * @return the common ancestor class of the object values in the model.
     * @deprecated Use a method with column type of UjoProperty instead of.
     */
    final public Class getColumnClass(int columnIndex) {
        return getColumnClass(columns[columnIndex]);
    }
    
    // ================= NEW Ujo API ====================
    
    /**
     * Returns a row by a parameter. The mothod can throw an exception called "IndexOutOfBoundsException"
     * @param rowIndex
     */
    public ROW getRow(int rowIndex) throws IndexOutOfBoundsException {
        return rows.get(rowIndex);
    }
    
    /**
     * Returns a row by a parameter. The result can be a null value if a rowIndex is out of a range.
     * @param rowIndex
     */
    public ROW getRowNullable(int rowIndex) {
        return rowIndex>=0 && rowIndex<rows.size() ? rows.get(rowIndex) : null ;
    }
    
    /** Returns the lastRow of the model or null, if the model have got no rows. */
    public ROW getRowLast() {
        return getRowNullable(rows.size()-1);
    }
    
    /** Returns a value from the cell. */
    public Object getValueAt(int rowIndex, UjoProperty column) {
        return rows.get(rowIndex).readValue(column);
    }
    
    /** Set a value to a cell of table model. */
    public void setValueAt(Object value, int rowIndex, UjoProperty column) {
        getRow(rowIndex).writeValue(column, value);
        fireTableCellUpdated(rowIndex, getColumnIndex(column));
    }
    
    /** Column Name */
    public String getColumnName(UjoProperty column) {
        return column.getName();
    }
    
    /** Is the cell editable? */
    public boolean isCellEditable(int rowIndex, UjoProperty column) {
        return true;
    }
    
    /**
     * Returns the most specific superclass for all the cell values in the column.
     * @return the common ancestor class of the object values in the model.
     */
    public Class getColumnClass(UjoProperty column) {
        return column.getType();
    }
    
    /** Returns a table column index. */
    public int getColumnIndex(UjoProperty column) {
        for (int i=columns.length-1; i>=0; i--) {
            if (columns[i]==column) {
                return i;
            }
        }
        throw new IllegalArgumentException("Bad column: " + column);
    }
    
    /** Returns a table column index or -1 if the row was not found. */
    public int getRowIndex(ROW row) {
        int count = rows.size();
        for (int i=0 ; i<count; i++) {
            if (rows.get(i)==row) {
                return i;
            }
        }
        return -1;
    }
    
    /** Delete row. */
    public ROW deleteRow(int rowIndex) {
        final ROW result = rows.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
        return result;
    }
    
    /** Add a row to end of the model. */
    public void addRow(ROW row) {
        int lastRow = rows.size();
        rows.add(row);
        fireTableRowsInserted(lastRow, lastRow);
    }
    
    /** Clone row. */
    @SuppressWarnings("unchecked")
    public ROW cloneRow(int rowIndex, int depth, Object context) {
        ROW row1 = getRow(rowIndex);
        ROW row2 = (row1 instanceof UjoCloneable)
        ? (ROW) ((UjoCloneable) row1).clone(depth, this)
        : (ROW) getUjoManager().clone(row1, depth, context)
        ;
        addRow(row2);
        return row2;
    }
    
    /** Repaint header */
    public void fireTableHeaderRepainted() {
        final TableModelEvent evt = new TableModelEvent
        (this
        , 0
        , 0
        , TableModelEvent.ALL_COLUMNS
        , EVENT_REPAINT_HEADER
        );
        fireTableChanged(evt);
    }
    
    // ============ UTILS ===================
    
    /**
     * Sort data by a property list.
     * @param asc Ascending / Descending. Null value are always on the end.
     * @param properties Array of properties. A property value must be comparable.
     */
    public void sort(boolean asc, UjoProperty ... properties) {
        final Comparator<Ujo> comp = new UjoComparator(asc, properties);
        sort(comp);
    }
    
    /** Sort data by a Comparator object. */
    public void sort(Comparator<Ujo> comparator) {
        Collections.sort(rows, comparator);
        fireTableAllRowUpdated();
    }
    
    /** Fire an sing, that all rows was updated. */
    public void fireTableAllRowUpdated() {
        fireTableRowsUpdated(0, getRowCount()-1);
    }
    
    
    /** Fire an sing, that column was updated. */
    public void fireTableColumnUpdated(UjoProperty columnProp) {
        int column = getColumnIndex(columnProp);
        for (int i=getRowCount()-1; i>=0; i--) {
            fireTableCellUpdated(i, column);
        }
    }
    
}

