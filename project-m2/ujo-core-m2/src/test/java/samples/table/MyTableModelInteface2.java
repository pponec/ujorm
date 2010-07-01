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
    
    public int getRowCount();
    public int getColumnCount();
    public Object getValueAt(int rowIndex, int columnIndex);
    public void   setValueAt(Object value, int rowIndex, int columnIndex);
    public String getColumnName(int column);
    public boolean isCellEditable(int rowIndex, int columnIndex);
    
}
