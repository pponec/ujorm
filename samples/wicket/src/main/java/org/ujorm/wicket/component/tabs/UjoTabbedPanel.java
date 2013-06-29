/*
 * Copyright 2013, Pavel Ponec (http://ujorm.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.wicket.component.tabs;

import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import static org.ujorm.wicket.CommonConstants.*;

/**
 * A child of Wicket AjaxTabbedPanel class can restore the last selected tab
 * from a session after a page reloading and supports a user tab CSS class.
 * <h4>See the simple sample:</h4>
 * <pre code=pre> {@code
 * List<ITab> tabs = new ArrayList<ITab>();
 * tabs.add(new UjoTab("Hotels", "hotel", HotelPanel.class));
 * tabs.add(new UjoTab("Booking", "booking", BookingPanel.class));
 * tabs.add(new UjoTab("Customer", "customer", CustomerPanel.class));
 * tabs.add(new UjoTab("About", "about", AboutPanel.class));
 * panel.add(new UjoTabbedPanel("tabs", tabs));
 * }
 * </pre>
 *
 * @author PavelPonec
 */
public class UjoTabbedPanel<T extends UjoTab>
        extends org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel<T> {

    /** Tab Index Key */
    private String indexKey;

    /** Constructor with a model for all tabs
     * @param id
     * @param tabs
     */
    public UjoTabbedPanel(final String id, final List<T> tabs) {
        this(id, tabs, null);
    }

    public UjoTabbedPanel(final String id, final List<T> tabs, IModel<Integer> model) {
        super(id, tabs, model);
        this.indexKey = getClass().getName() + ":" + getPath() + ".tabIndex.";
        setSelectedTab(getDefaultSelectedTab());
    }

    /** Save selected tab */
    @Override
    protected void onAjaxUpdate(AjaxRequestTarget target) {
        setDefaultSelectedTab(getSelectedTab());
    }

    /** Assign a selected tab and add a user CSS class.
     * <br/>{@inheritDoc}
     */
    @Override
    public TabbedPanel<T> setSelectedTab(final int index) {
        TabbedPanel<T> result = super.setSelectedTab(index);
        final Component panel = super.get(TAB_PANEL_ID);
        panel.add(new AttributeAppender(CSS_CLASS, Model.of(getTabs().get(index).getCssClass()), " "));
        return result;
    }

    /** Get Default selectedTab from Session */
    protected final int getDefaultSelectedTab() {
        final Object result = getSession().getAttribute(indexKey);
        return result instanceof Integer ? (Integer) result : 0;
    }

    /** Save selected tab to a Session */
    protected void setDefaultSelectedTab(Integer index) {
        getSession().setAttribute(indexKey, index);
    }

    /** @{@inheritDoc } */
    @Override
    protected WebMarkupContainer newLink(String linkId, final int index) {
        final WebMarkupContainer result = super.newLink(linkId, index);
        final String cssClass = getTabs().get(index).getCssClass();
        if (cssClass != null) {
            result.add(new AttributeAppender(CSS_CLASS, cssClass));
        }
        return result;
    }
}
