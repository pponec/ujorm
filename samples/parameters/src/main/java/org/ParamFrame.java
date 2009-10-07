package org;

import javax.swing.*;

import java.awt.event.*;
import java.io.File;
import org.ujoframework.core.UjoManagerRBundle;
import org.ujoframework.swing.SingleUjoTabModel;
import org.ujoframework.swing.UjoPropertyRow;
import static org.ujoframework.swing.UjoPropertyRow.*;


/** Parameter manager. */
public class ParamFrame extends JFrame implements ActionListener, Runnable {

  private Parameters parameters;
  private File dataFile = new File(System.getProperty("user.home"),"ujo-param.properties");
  private SingleUjoTabModel model;
  private JTable table;

  /** Creates a new instance of TableFrame */

  public ParamFrame() {
    initComponents();

    // Create a TableModel:
    parameters = loadParameters();
    model = new SingleUjoTabModel(parameters, P_NAME, P_TYPENAME, P_VALUE, P_DEFAULT);
    table.setModel(model);

    // Register a Close Listener:
    Runtime.getRuntime().addShutdownHook(new Thread(this));
  }

  @Override public void run() {
    saveParameters();
  }

  /** Load company from file. */
  private Parameters loadParameters() {
    if (dataFile.isFile()) try {
      return UjoManagerRBundle.getInstance(Parameters.class).loadResourceBundle(dataFile, false, this);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return new Parameters();
  }

  /** Save parameter to file. */
  private void saveParameters() {
    try {
      final String msg = "Configuration file:" ;
      UjoManagerRBundle.getInstance(Parameters.class).saveResourceBundle(dataFile, parameters, msg, this);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  /** Button Actions */
  public void actionPerformed(ActionEvent e) {
    String label = ((JButton)e.getSource()).getText();
    int index = table.getSelectedRow();

    if ("Default".equals(label) && index>=0) {
        UjoPropertyRow row = model.getRow(index);
        model.setValueAt(row.getProperty().getDefault(), index, UjoPropertyRow.P_VALUE);
    }
  }

  /** Init GUI Components */
  private void initComponents() {
    table = new JTable();
    getContentPane().add(new JScrollPane(table), java.awt.BorderLayout.CENTER);
    JPanel panel = new JPanel(new java.awt.GridLayout(5, 1, 0, 1));
    getContentPane().add(panel, java.awt.BorderLayout.EAST);
    addButtons(panel, "Default");

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Application parameters");
    pack();
  }

  private void addButtons(JPanel panel, String... labels) {
    for(String label: labels) {
      JButton button = new JButton(label);
      button.addActionListener(this);
      panel.add(button);
    }
  }

  public static void main(String args[]) {
    new ParamFrame().setVisible(true);
  }
}