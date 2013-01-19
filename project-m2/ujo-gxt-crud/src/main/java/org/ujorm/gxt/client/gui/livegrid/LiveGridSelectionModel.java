/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2013 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */
package org.ujorm.gxt.client.gui.livegrid;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.commons.KeyCodes;

/**
 *
 * @author pelc
 */
public class LiveGridSelectionModel<CUJO extends AbstractCujo> extends GridSelectionModel<CUJO> {

    private boolean previousItemSelected = false;
    private int actualPosition;

    public void doKeyDown(GridEvent<CUJO> e) {
        LiveGridView view = (LiveGridView) grid.getView();
        if (selectionMode == SelectionMode.MULTI) {
            if (!hasNext()) {
                view.moveRowDown();
                CUJO previous = listStore.getAt(actualPosition - 1);
                CUJO sel = listStore.getAt(actualPosition);
                selectRows(e, sel, previous, getFocusComponent());
            } else {
                CUJO previous = listStore.getAt(actualPosition++);
                CUJO sel = listStore.getAt(actualPosition);
                selectRows(e, sel, previous, getFocusComponent());
            }
        } else {
            if (!hasNext()) {
                view.moveRowDown();
                if (e != null) {
                    super.onKeyDown(e);
                }
            } else {
                if (e != null) {
                    super.onKeyDown(e);
                }
            }
        }
    }

    public void doKeyUp(GridEvent<CUJO> e) {
        LiveGridView view = (LiveGridView) grid.getView();
        if (selectionMode == SelectionMode.MULTI) {
            if (!hasPrevious()) {
                view.moveRowUp();
                CUJO previous = listStore.getAt(actualPosition + 1);
                CUJO actual = listStore.getAt(actualPosition);
                selectRows(e, actual, previous, getFocusComponent());
            } else {
                CUJO previous = listStore.getAt(actualPosition--);
                CUJO actual = listStore.getAt(actualPosition);
                selectRows(e, actual, previous, getFocusComponent());
            }
        } else {
            if (!hasPrevious()) {
                view.moveRowUp();
                if (e != null) {
                    super.onKeyUp(e);
                }
            } else {
                if (e != null) {
                    super.onKeyUp(e);
                }
            }
        }
    }

    protected boolean isLoading(GridEvent<CUJO> e) {
        // eventy jsou pĹ™Ă­pustnĂ© pouze, kdyĹľ jsou k dispozici data...
        if (grid.isMasked() || ((LiveGridView) grid.getView()).getScroller().isMasked()) {
            e.setCancelled(true);
            e.stopEvent();
            return true;
        }
        return false;
    }

    @Override
    protected void onKeyUp(GridEvent<CUJO> e) {
        if (isLoading(e)) {
            return;
        }
        doKeyUp(e);
    }

    @Override
    protected void onKeyDown(GridEvent<CUJO> e) {
        if (isLoading(e)) {
            return;
        }
        doKeyDown(e);
    }

    private void selectRows(GridEvent<CUJO> e, CUJO actual, CUJO previous, Component focus) {
        // stavy, ktere nejsou vyvolane eventem
        if (e == null) {
            simpleSelect(actual, false, focus);
            return;
        }
        // stavy, ve kterych event existuje
        // TODO: pro shit / ctrl + pageup / pagedown nefunguje
        if (e != null && e.isShiftKey()) {
            select(actual, true);
            previousItemSelected = true;
        } else if (e.isControlKey()) {
            if (!previousItemSelected) {
                deselect(previous);
            }
            if (!isSelected(actual)) {
                select(actualPosition, true, focus);
                previousItemSelected = false;
            } else {
                previousItemSelected = true;
                setFocusToComponent(focus);
            }
        } else {
            simpleSelect(actual, false, focus);
        }
        e.preventDefault();
    }

    private void simpleSelect(CUJO actual, boolean keep, Component focus) {
        // oznaÄŤenĂ­
        if (actual == null) {
            select(actualPosition, keep);
        } else {
            select(actual, keep);
        }
        setFocusToComponent(focus);
    }

    private void setFocusToComponent(Component focus) {
        // fokus
        if (focus == null) {
            grid.getView().focusRow(actualPosition);
        } else {
            focus.focus();
            focus = null;
        }
    }

    public void select(int index, boolean keepExisting, Component focus) {
        super.select(index, keepExisting);
        if (focus == null) {
            grid.getView().focusRow(index);
        } else {
            focus.focus();
        }
        actualPosition = index;
    }

    @Override
    protected void onKeyPress(GridEvent<CUJO> e) {
        if (e.getKeyCode() == KeyCodes.SPACE && e.isControlKey()) {
            CUJO sel = listStore.getAt(actualPosition);
            if (previousItemSelected) {
                deselect(sel);
                previousItemSelected = false;
            } else {
                // TODO
                select(actualPosition, true, getFocusComponent());
                previousItemSelected = true;
            }
        }
    }

    @Override
    protected void handleMouseClick(GridEvent<CUJO> e) {
        super.handleMouseClick(e);
        previousItemSelected = true;
        actualPosition = e.getRowIndex();
    }

    public boolean hasNextSelect() {
        return getSelectedItem() != null && hasNext();
    }

    public boolean hasPrevSelect() {
        return getSelectedItem() != null && hasPrevious();
    }
    // TODO:
    private Component focusComponent = null;

    private Component getFocusComponent() {
        return focusComponent;
    }

    public void setFocusComponent(Component focusComponent) {
        this.focusComponent = focusComponent;
    }

    public void setActualPosition(int actualPosition) {
        this.actualPosition = actualPosition;
    }
}
