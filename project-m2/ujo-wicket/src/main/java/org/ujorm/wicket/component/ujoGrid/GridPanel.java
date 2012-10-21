/*
 *  Copyright 2011-2012 Pavel Ponec
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
package org.ujorm.wicket.component.ujoGrid;

import java.util.Locale;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.ujorm.Ujo;
import org.ujorm.Key;

import org.ujorm.wicket.KeyModel;
import static org.ujorm.wicket.component.ujoGrid.GridPanel.Constants.*;

/**
 * Wicket GridPanel
 * <pre class="pre">
 * KeyRing TABLE_COLUMNS = KeyRing.of
 *     ( Employee.ID
 *     , Employee.FIRSTNAME
 *     , Employee.LASTNAME
 *     , Employee.EMAIL
 *     );
 * GridPanel&lt;Employee&gt; grid = <span class="keyword-directive">new</span> GridPanel&lt;Employee&gt;(<span class="character">&quot;</span><span class="character">gridPanel</span><span class="character">&quot;</span>, 
 * 	<span class="keyword-directive">new</span> GridDataListProvider&lt;Employee&gt;(getPeople(), TABLE_COLUMNS));
 * grid.setItemsPerPage(20);
 * add(grid);
 * add(grid.createNavigator(<span class="character">&quot;</span><span class="character">gridNavigator</span><span class="character">&quot;</span>));
 * </pre>
 * @author Pavel Ponec
 * @see GridDataProvider
 */
public class GridPanel<T extends Ujo> extends Panel implements IPageable {
    private static final long serialVersionUID = 1L;

    /** Data Provicer */
    protected final GridDataProvider<T> dataProvider;
    /** Data View */
    private DataView<T> dataView;
    /** CSS stype flag */
    private String sortedCssFlag = "sorted";
    /** Default sorted column, the value -1 means: no sorted column to display */
    private int sortedColumn = -1;
    /** Is header visible ? */
    final WebMarkupContainer header;

    public GridPanel(String id, final GridDataProvider<T> dataProvider) {
        super(id);
        this.dataProvider = dataProvider;
        this.header = new WebMarkupContainer(ID_HEADER_ROW);

        // ----- TABLE ----------------------------------

        dataView = new DataView<T>(ID_TABLE_ROW, dataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(Item<T> item) {
                final T row = (T) item.getModelObject();
                final GridPanelCell<T> cell = new GridPanelCell<T>(dataView.newChildId(), row, dataProvider, GridPanel.this);
                dataView.add(cell);
            }
        };
    }

    @Override
    public final void onInitialize() {
        super.onInitialize();
        add(dataView);

        // ----- HEADER ----------------------------------

        this.add(header);
        header.add(new RepeatingView(ID_HEADER_ITEM) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onPopulate() {
                removeAll();

                for (int i = 0, max = dataProvider.getTableColumns().size(); i < max; ++i) {
                    final int columnNumber = i;
                    final Key header = dataProvider.getTableColumns().get(i);
                    final Label label = new Label(newChildId(), Model.of(getLocalizedText(header.getName())));
                    // Align the class
                    final String rightAlignClass = getColumnlass(header);
                    if (rightAlignClass != null) {
                        label.add(AttributeModifier.replace(CSS_CLASS, rightAlignClass));
                    }
                    label.add(new AttributeAppender(CSS_CLASS, Model.of("dataheader"), " "));

                    if (dataProvider.isSortable(i)) {
                        label.add(new AjaxEventBehavior("onclick") {
                            private static final long serialVersionUID = 1L;

                            @Override
                            protected void onEvent(AjaxRequestTarget target) {
                                orderBy(target, columnNumber, false);
                            }
                        });
                        if (i == sortedColumn && sortedCssFlag != null) {
                            label.add(new AttributeAppender(CSS_CLASS, Model.of(sortedCssFlag), " "));
                        }
                    }
                    add(label);
                }
            }
        });
    }

    /** Order by */
    public void orderBy(AjaxRequestTarget target, int headerColumn, boolean descending) {
        this.sortedColumn = headerColumn;
        this.dataProvider.setOrderBy(headerColumn, descending);
        target.add(GridPanel.this);
    }

    /**
     * @return number of items displayed per page
     */
    public int getPageLimit() {
        return dataView.getItemsPerPage();
    }

    /**
     * Sets the number of items to be displayed per page
     *
     * @param items
     *            number of items to display per page
     */
    public void setItemsPerPage(int limit) {
        dataView.setItemsPerPage(limit);
    }

    /**
     * @return The current page that is or will be rendered.
     */
    @Override
    public int getCurrentPage() {
        return dataView.getCurrentPage();
    }

    /**
     * Sets the a page that should be rendered.
     *
     * @param page
     *            The page that should be rendered.
     */
    @Override
    public void setCurrentPage(int page) {
        dataView.setCurrentPage(page);
    }

    /**
     * Gets the total number of pages this pageable object has.
     *
     * @return The total number of pages this pageable object has
     */
    @Override
    public int getPageCount() {
        return dataView.getPageCount();
    }

    /**
     * Display a sorted dolumn
     * @param sortedColumn can be {@code null}
     * @param sortedCssClass
     */
    final public void setSortedColumnClass(Key<T,?> sortedColumn, String sortedCssClass) {
        setSortedColumnClass(dataProvider.getColumnIndex(sortedColumn), sortedCssClass);
    }

    /**
     * Display a sorted dolumn
     * @param sortedColumnIndex An undefined value is -1
     * @param sortedCssClass
     */
    public void setSortedColumnClass(int sortedColumnIndex, String sortedCssClass) {
        this.sortedCssFlag = sortedCssClass;
        this.sortedColumn = sortedColumnIndex;
    }

    /** Overwrite the method to Localize the text.  */
    protected String getLocalizedText(String key) {
        String result = getLocalizer().getString("GridPanel." + key, this, "");
        if ("".equals(result)) {
            result = key.substring(0, 1).toUpperCase(Locale.ENGLISH) + key.substring(1);
        }
        return result;
    }

    /** Create new instance of the CELL. */
    public Component createCell(final String id, final T row, final Key<T,?> column) {
        final Label label = new Label(id, KeyModel.of(row, column));

        final String rightAlignClass = getColumnlass(column);
        if (rightAlignClass != null) {
            label.add(AttributeModifier.replace(CSS_CLASS, rightAlignClass));
        }
        return label;
    }

    /** Returns a column CSS class of {@code null}, of no CSS class is assigned.
     * @param column Table Column
     * @return Default value is <code>rightAlign</code> for a Nubber types.
     */
    protected String getColumnlass(final Key column) {
        return column.isTypeOf(java.lang.Number.class) ? CSS_RIGHT : null;
    }

    /** Create and add a Table Navigator */
    final public AjaxPagingNavigator createNavigator(String id) {
        return createNavigator(id, dataProvider.getDefultSortedColumn());
    }

    /** Create and add a Table Navigator and set OutputMarkupId fo rthis component. */
    public AjaxPagingNavigator createNavigator(String id, Key defaultSortedColumn) {
        this.setOutputMarkupId(true);
        this.setSortedColumnClass(defaultSortedColumn, sortedCssFlag);
        return new AjaxPagingNavigator(id, this);
    }

    /** Set a Header visible */
    public boolean isVisibleHeader() {
        return header.isVisible();
    }

    /** Set a Header visible */
    public void setVisibleHeader(boolean heeaderVisible) {
        this.header.setVisible(heeaderVisible);
    }

    /** Static Constant */
    static final class Constants {
        /** Header detail ID */
        static final String ID_HEADER_ROW = "header";
        /** Header detail ID */
        static final String ID_HEADER_ITEM = "th";
        /** Table row ID */
        static final String ID_TABLE_ROW = "row";
        /** Table cell ID */
        static final String ID_TABLE_CELL = "cell";
        /** Default CSS style to align to the right */
        static final String CSS_RIGHT = "right";
        /** CSS class key word */
        static final String CSS_CLASS = "class";
    }

}
