/*
 * Copyright 2013, Pavel Ponec
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
package org.ujorm.hotels.gui.components;

import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.IModel;

/**
 * A child of Wicket AjaxTabbedPanel class can restore the last selected tab
 * from a session after a page reloading.
 * @author PavelPonec
 */
public class AjaxTabbedPanel<T extends ITab>
extends org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel {

    /** Tab Index Key */
    private String indexKey;

    /**
     * Constructor
     *
     * @param id
     * @param tabs
     */
    public AjaxTabbedPanel(final String id, final List<T> tabs) {
        this(id, tabs, null);
    }

    public AjaxTabbedPanel(final String id, final List<T> tabs, IModel<Integer> model) {
        super(id, tabs, model);
        this.indexKey = getClass().getName() + ":" + getPath() + ".tabIndex.";
    }

    /** Save selected tab */
    @Override
    protected void onAjaxUpdate(AjaxRequestTarget target) {
        setDefaultSelectedTab(getSelectedTab());
    }

    /** Get Default selectedTab from Session */
    protected int getDefaultSelectedTab() {
        final Object result = getSession().getAttribute(indexKey);
        return result instanceof Integer ? (Integer) result : 0 ;
    }

    /** Save selected tab to a Session */
    protected void setDefaultSelectedTab(Integer index) {
        getSession().setAttribute(indexKey, index);
    }
}
