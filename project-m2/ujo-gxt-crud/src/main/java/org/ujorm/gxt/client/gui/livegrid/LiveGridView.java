/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujorm.gxt.client.gui.livegrid;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.cquery.CQuery;

/**
 * New Events:
 *  - beforeHeaderClick
 *  - afterHeaderClick
 *
 * @author Pelc
 */
public abstract class LiveGridView<CUJO extends AbstractCujo> extends com.extjs.gxt.ui.client.widget.grid.LiveGridView {

    public static EventType beforeHeaderClick = new EventType();
    public static EventType afterHeaderClick = new EventType();
    protected String activeColumnCcell = "activeColumnCcell";
    private int lastSelected = 0;
    private SortOperation<CUJO> sort;

    public LiveGridView(SortOperation<CUJO> sort) {
        this.sort = sort;
    }

    /** VyznaÄŤĂ­me sloupec, podle kterĂ©ho se aktuĂˇlnÄ› Ĺ™adĂ­ */
    public void selectColumn(int colIndex) {
        if (colIndex < 0) {
            // TODO: co dÄ›lat v pĹ™Ă­padÄ›, Ĺľe sloupec nebyl nalezen...
            return;
        }
        int rowCount = Math.max(0, Math.min(totalCount, getVisibleRowCount()));
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Element cellElement = getCell(rowIndex, colIndex);
            if (cellElement != null) {
                cellElement.addClassName(activeColumnCcell);
            }
        }

    }

    /** PovolĂ­me horizontĂˇlnĂ­ scrollovĂˇnĂ­. */
    @Override
    protected void afterRender() {
        super.afterRender();
        // HACK - horizontĂˇlnĂ­ scrollovĂˇnĂ­ v LG pomocĂ­ css...
        // overflow: auto;
        // overflow-y: hidden;
        // You may also need to add for IE8:
        // -ms-overflow-y: hidden;
        getBody().setStyleAttribute("overflow", "auto");
        getBody().setStyleAttribute("overflow-y", "hidden");
        getBody().setStyleAttribute("-ms-overflow-y", "hidden");
    }

    @Override
    protected void onHeaderClick(final Grid grid, final int column) {
        sort.setReload(true);
        grid.mask(translate("Sorting..."));
        GridEvent<CUJO> gridEvent = new GridEvent<CUJO>(grid);
        gridEvent.setColIndex(column);
        //

        fireEvent(beforeHeaderClick, gridEvent);
        //
        CUJO row = (CUJO) grid.getSelectionModel().getSelectedItem();

        if (row == null) {
            // v pripade, ze row je null neni vybrany zadny radek...
            super.onHeaderClick(grid, column);
            return;
        }

        // ID zĂˇznamu na aktivnĂ­m Ĺ™Ăˇdku...
        Long id = row.get("id");
        // ID sloupce...
        String activeColumnId = grid.getColumnModel().getColumn(column).getId();
        // hodnota aktivniho sloupce...
        Object selectedColumValue = row.get(activeColumnId);

        // z konfigu naÄŤteme aktivnĂ­ sloupec a zpĹŻsob Ĺ™azenĂ­ pĹ™ed udĂˇlostĂ­..
        PagingLoadConfig config = (PagingLoadConfig) grid.getStore().getLoadConfig();
        boolean descending = config.getSortDir().equals(SortDir.DESC);
        String orderBy = config.getSortField();

        // pokud Ĺ™adime opakovane podle stejneho sloupce, meni se descending v ascending a opaÄŤnÄ›...
        if (orderBy.equals(activeColumnId)) {
            descending = !descending;
        } else {
            descending = false;
        }

        AsyncCallback callback = new AsyncCallback<Integer>() {

            @Override
            public void onFailure(Throwable caught) {
                LiveGridView.this.fireEvent(afterHeaderClick);
                Info.display(translate("Info"), translate("Error-by-sorting"));
                grid.focus();
            }

            @Override
            public void onSuccess(Integer result) {
                sort.setReload(true);
                LiveGridView.super.onHeaderClick(grid, column);
                LiveGridView.this.fireEvent(afterHeaderClick);
                moveTo(result.intValue());
                grid.focus();
            }
        };
        //
        CQuery<CUJO> query = sort.getQuery();
        Class<CUJO> myClass = sort.getCujoType();
        String myClassName = myClass.getName();
        //
        if (selectedColumValue instanceof Cujo) {
            sort.getController().getSearchedRow(myClassName, descending, activeColumnId, (Cujo) selectedColumValue, id, query, callback);
        } else if (selectedColumValue instanceof Integer) {
            sort.getController().getSearchedRow(myClassName, descending, activeColumnId, (Integer) selectedColumValue, id, query, callback);
        } else if (selectedColumValue instanceof Long) {
            sort.getController().getSearchedRow(myClassName, descending, activeColumnId, id, query, callback);
        } else {
            sort.getController().getSearchedRow(myClassName, descending, activeColumnId, (String) selectedColumValue, id, query, callback);
        }
    }

    public abstract String translate(String name);

    /** Posun View o Ĺ™Ăˇdek nahoru. */
    public void moveRowUp() {
        if (viewIndex > 0) {
            moveView(-1, true, false);
        }
    }

    /** Posun View o Ĺ™Ăˇdek dolu. */
    public void moveRowDown() {
        if ((totalCount - getVisibleRowCount()) > viewIndex) {
            moveView(1, false, false);
        }
    }

    /** Posun View o strĂˇnku nahoru. */
    public void movePageUp() {
        moveView(-getVisibleRowCount(), true, false);
    }

    /** Posun View o strĂˇnku dolu. */
    public void movePageDown() {
        moveView(getVisibleRowCount(), false, false);
    }

    /** Posun View na zaÄŤĂˇtek. */
    public void moveHome() {
        moveView(-totalCount, true, false);
    }

    /** Posun View na konec. */
    public void moveEnd() {
        moveView(totalCount, false, false);
    }

    /** RelativnĂ­ posun (o hodnotu) view po store pĹ™Ă­padnÄ› DB. Po posunu se oznamaÄŤĂ­ buÄŹ prvnĂ­ nebo poslednĂ­ Ĺ™Ăˇdek.
     * @param rowsMove poÄŤet Ĺ™ĂˇdkĹŻ, o kterĂ© se chcete posunout (pĹ™.: Ĺˇipka nahoru => rowsMove=-1, Ĺˇipka dolĹŻ => rowsMove=1)
     * @param selectFirst pokud se posouvĂˇte smÄ›rem nahoru (up, pageUp, home) vloĹľte TRUE, pokud smÄ›rem dolĹŻ (down, pageDown, end) vloĹľte FALSE
     * @param keepExistSelectedItems ponechat oznaÄŤenĂ© jiĹľ dĹ™Ă­ve oznaÄŤenĂ© Ĺ™Ăˇdky...
     */
    protected void moveView(int rowsMove, boolean selectFirst, boolean keepExistSelectedItems) {
        // 1. scroll with liveGridScroller
        int newTop = liveScroller.getScrollTop() + getCalculatedRowHeight() * rowsMove;
        // 2. update View from Store cache or DB
        int newViewIndex = Math.min(totalCount, Math.max(0, viewIndex + rowsMove));
        // 3. select row (first row: up, pageUp, Home; last row: down, pageDown, End)
        int selectIndex = selectFirst ? 0 : Math.max(0, Math.min(totalCount - 1, getVisibleRowCount() - 1));

        moveViewTo(newTop, newViewIndex, selectIndex, keepExistSelectedItems, null);
    }

    /** AbsolutnĂ­ posun view na hodnotu newViewIndex */
    protected void moveViewTo(int newTop, int newViewIndex, int selectIndex, boolean keepExistSelectedItems, Component focus) {
        sort.setReload(true);
        scrollTo(newTop);
        updateRows(newViewIndex, false);
        //
        LiveGridSelectionModel selection = (LiveGridSelectionModel) grid.getSelectionModel();
        selection.select(selectIndex, keepExistSelectedItems, focus);
        lastSelected = selectIndex;
        selection.setActualPosition(selectIndex);
    }

    /** AbsolutnĂ­ posun view na hodnotu newViewIndex */
    public void moveTo(int newViewIndex) {
        moveTo(newViewIndex, null);
    }

    /** AbsolutnĂ­ posun view na hodnotu newViewIndex */
    public void moveTo(int newViewIndex, Component focus) {
        int selectIndex = 0;
        if (getVisibleRowCount() > totalCount) {
            selectIndex = newViewIndex;
        } else if (totalCount - newViewIndex < getVisibleRowCount()) {
            selectIndex = getVisibleRowCount() - (totalCount - newViewIndex);
        }
        moveViewTo(
                newViewIndex * getCalculatedRowHeight(),
                newViewIndex,
                selectIndex,
                false,
                focus);
    }

    /** Posune scroll bar na novou hodnot. */
    protected void scrollTo(final int newTop) {
        liveScroller.setScrollTop(newTop);
    }

    public int getLastSelected() {
        return lastSelected;
    }

    public Integer getTotalCount() {
        return totalCount;
    }
}
