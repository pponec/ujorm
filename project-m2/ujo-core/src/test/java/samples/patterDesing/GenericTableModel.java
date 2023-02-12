package samples.patterDesing;

import javax.swing.table.TableModel;

/**
 * Sample of a Generic TableModel.
 * @author Pavel Ponec
 */

interface GenericTableModel<MyMap> extends TableModel {
    void setColumns(Property ... columns);
}
