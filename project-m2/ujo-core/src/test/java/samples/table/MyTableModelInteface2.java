/*
 * MyTableModel.java
 *
 * Created on 17. June 2007, 11:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.table;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Table Model Interface 2
 * @author Pavel Ponec
 */
interface MyTableModelInteface2 extends TableModel {

    int getRowCount();
    int getColumnCount();
    Object getValueAt(int rowIndex, int columnIndex);
    void   setValueAt(Object value, int rowIndex, int columnIndex);
    String getColumnName(int column);
    boolean isCellEditable(int rowIndex, int columnIndex);

}
