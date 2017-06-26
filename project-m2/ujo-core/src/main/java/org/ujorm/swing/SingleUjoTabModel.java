/*
 *  Copyright 2007-2014 Pavel Ponec
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

package org.ujorm.swing;

import java.util.Iterator;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.core.UjoActionImpl;
import org.ujorm.core.UjoCoder;
import org.ujorm.core.UjoManager;
import org.ujorm.UjoAction;

/**
 * An implementation of TableModel for a Single Ujo object.
 *
 * @author Pavel Ponec
 */
public class SingleUjoTabModel extends UjoTableModel<UjoKeyRow> implements Iterable<UjoKeyRow> {

    /** Property row */
    public static final UjoKeyRow ROWS = null;

    /**
     * Creates a new instance of SingleUjoTabModel
     */
    public SingleUjoTabModel(Ujo content) {
        this(content, UjoManager.getInstance().readKeys(UjoKeyRow.class).toArray() );
    }

    /**
     * Creates a new instance of SingleUjoTabModel
     */
    public SingleUjoTabModel(Ujo content, Key ... columns) {
        super(columns);
        rows = UjoManager.getInstance().createKeyRowList(content, new UjoActionImpl(UjoAction.ACTION_TABLE_SHOW, this));
    }

    /** Only Value is editable. */
    @Override
    public boolean isCellEditable(int rowIndex, Key column) {
        final boolean result
        =  column==UjoKeyRow.P_VALUE
        || column==UjoKeyRow.P_TEXT
        ;
        return result;
    }

    /** Set a value to a cell of table model. */
    @Override
    @SuppressWarnings("static-access")
    public void setValueAt(Object value, int rowIndex, Key column) {
        if (column==ROWS.P_VALUE
        && !column.getType().equals(String.class)
        &&  value instanceof String
        ){
            UjoKeyRow row = getRow(rowIndex);
            row.writeValueString(column, (String) value, null, new UjoActionImpl(this));
        } else {
            super.setValueAt(value, rowIndex, column);
        }
    }

    /** Get Value in a String format. */
    @Override
    @SuppressWarnings("static-access")
    public Object getValueAt(int rowIndex, Key column) {
        final Object result
        = (column==ROWS.P_VALUE)
        ?  getRow(rowIndex).readValueString(column, new UjoActionImpl(this))
        : (column==ROWS.P_DEFAULT)
        ?  getCoder().encodeValue(getRow(rowIndex).getProperty().getDefault(), false)
        : super.getValueAt(rowIndex, column)
        ;
        return result;
    }

    /** Returns an UjoCoder */
    protected UjoCoder getCoder() {
        return UjoManager.getInstance().getCoder();
    }

    /** Returns an iterator */
    @Override
    public Iterator<UjoKeyRow> iterator() {
        return rows.iterator();
    }

}
