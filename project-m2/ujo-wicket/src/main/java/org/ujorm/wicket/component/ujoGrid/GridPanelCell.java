package org.ujorm.wicket.component.ujoGrid;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.ujorm.Ujo;
import org.ujorm.Key;

/**
 * TableCell COmponent
 * @author Pavel Ponec
 */
final class GridPanelCell<UJO extends Ujo> extends Panel {

    public GridPanelCell
            ( final String id
            , final UJO row
            , final GridDataProvider<UJO> data
            , final GridPanel<UJO> table
        ) {
        super(id);
        final RepeatingView cell = new RepeatingView(GridPanel.Constants.ID_TABLE_CELL);
        this.add(cell);
        for (Key column : data.getTableColumns()) {
            cell.add(table.createCell(cell.newChildId(), row, column));
        }
    }
}
