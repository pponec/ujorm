/*
 * UseTable.java
 *
 * Created on 17. October 2007, 19:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.table;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import org.ujorm.swing.UjoTableModel;
import samples.array.Person;

/**
 *
 * @author Pavel Ponec
 */
public class UseTable {
    
    /** Creates a new instance of UseTable */
    public UseTable() {
    }
    
    /** Basic usage */
    @SuppressWarnings("unchecked")
    private void doit() {
        
        JTable jTable = new JTable();
        
        // Create a model for all attributes:
        UjoTableModel<Person> model = new UjoTableModel(Person.NAME, Person.MALE, Person.BIRTH);
        // or simply: ... model = new UjoTableModel(Person.class)
        jTable.setModel(model);
        
        // We can set an data optionaly:
        List<Person> persons = new ArrayList<>();
        model.setRows(persons);
        
    }
    
    /** Selected Attributes */
    @SuppressWarnings("unchecked")
    private void doit_2() {
        
        JTable jTable = new JTable();
        
        // Create a model for selected attributes:
        UjoTableModel<Person> model = new UjoTableModel(Person.NAME, Person.BIRTH);
        
    }
    
    /** More Table facilities */
    @SuppressWarnings("unchecked")
    private void doit_3() {
        
        JTable jTable = new JTable();
        UjoTableModel<Person> model = new UjoTableModel(Person.NAME, Person.BIRTH);
        
        
        
        // Insert new row:
        model.addRow(new Person());
        
        // Set a value
        model.setValueAt("Prokop", 0, Person.NAME );
        
        // Get the last row of table:
        Person person = model.getRowLast();
        
        // Sort the model:
        model.sort(Person.NAME);
        
        
        
    }
    
    
}
