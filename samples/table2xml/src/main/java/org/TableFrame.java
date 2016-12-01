package org;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.core.XmlHeader;
import org.ujorm.swing.UjoTableModel;
import static org.Company.*;
import static org.Person.*;

/** Simple Table Frame. */
public class TableFrame extends JFrame implements ActionListener, Runnable  {

  private File dataFile = new File(System.getProperty("user.home"),"ujo-company.xml");
  private Company company;
  private UjoTableModel<Person> model;
  private JTable table;

  /** Creates a new instance of TableFrame */
  public TableFrame() {
    initComponents();

    // Create a TableModel with Columns:
    model = new UjoTableModel<Person>(ID, FIRSTNAME, SURNAME, AGE, CASH, MALE);
    // ... or use simply:
    // model = new UjoTableModel<Person>(Person.class);
    table.setModel(model);

    // Assign Data into TableModel:
    company = loadCompany();
    List<Person> persons = PERSONS.getList(company); // returns a not null list always
    model.setRows(persons);

    // Register a Close Listener:
    Runtime.getRuntime().addShutdownHook(new Thread(this));
  }

  @Override public void run() {
    saveCompany();
  }

  /** Load company from file. */
  private Company loadCompany() {
    if (dataFile.isFile()) try {
      return UjoManagerXML.getInstance().parseXML(dataFile, Company.class, "Load company");
    } catch (RuntimeException | OutOfMemoryError e) {
      e.printStackTrace();
    }
    return new Company();
  }

  /** Save company to file. */
  private void saveCompany() {
    try {
      XmlHeader defaultXmlHeader = null;
      UjoManagerXML.getInstance().saveXML(dataFile, company, defaultXmlHeader, "Save company");
    } catch (IOException | RuntimeException | OutOfMemoryError e) {
      e.printStackTrace();
    }
  }

  /** Button Actions */
  @Override public void actionPerformed(ActionEvent e) {
    String label = ((JButton)e.getSource()).getText();
    int index = table.getSelectedRow();

    if ("New".equals(label)) {
      model.addRow(new Person());
      // Very primitive ID generator:
      model.getRowLast().set(ID, model.getRowCount());
    }
    if ("Delete".equals(label) && index>=0) {
      model.deleteRow(index);
    }
    if ("Copy".equals(label) && index>=0) {
      int depth = 2;
      model.cloneRow(index, depth, this);
    }
    if ("Add $10".equals(label) && index>=0) {
      model.getRow(index).addCash(10d);
      model.fireTableColumnUpdated(Person.CASH);
    }
    if ("3 columns".equals(label)) {
      model.setColumns(ID, SURNAME, CASH);
    }
    if ("6 columns".equals(label)) {
      model.setColumns(ID, FIRSTNAME, SURNAME, AGE, CASH, MALE);
    }
  }

  /** Init GUI Components */
  private void initComponents() {
    setTitle("Persons of Company");
    table = new JTable();
    JPanel panel = new JPanel(new java.awt.GridLayout(6, 1, 0, 1));
    getContentPane().add(new JScrollPane(table), java.awt.BorderLayout.CENTER);
    getContentPane().add(panel, java.awt.BorderLayout.EAST);
    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setBounds(100,200,550,165);

    String[] labels = {"New", "Delete", "Copy", "Add $10", "3 columns", "6 columns"};
    for (String label : labels){
       JButton button = new JButton(label);
       button.addActionListener(this);
       panel.add(button);
    }
  }

  public static void main(String args[]) {
    new TableFrame().setVisible(true);
  }
}