package samples.patterDesing;

import java.util.List;
import javax.swing.table.TableModel;

/**
 * Sample of a Generic TableModel.
 * @author Pavel Ponec
 */

interface GenericTableModel<MyMap> extends TableModel {
    public void setColumns(Property ... columns);
}
