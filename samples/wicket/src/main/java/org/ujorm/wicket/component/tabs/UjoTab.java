/*
 * Copyright 2013 Pavel Ponec
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

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Convenience class that takes care of common ITab functionality
 * @author Pavel Ponec
 */
public class UjoTab extends AbstractTab {

    /** Name of the CSS class */
    private final String cssClass;
    /** Default class for creating a panel of the tab */
    private final Class<? extends WebMarkupContainer> panel;

    /** Model for constructor of the Tab components */
    private IModel<?> tabModel;

    /** Constructor */
    public UjoTab(IModel<String> title, Class<? extends WebMarkupContainer> panel) {
        this(title, null, panel);
    }

    /** Constructor */
    public UjoTab(String title, String cssClass, Class<? extends WebMarkupContainer> panel) {
        this(Model.of(title), cssClass, panel);
    }

    /** Constructor */
    public UjoTab(IModel<String> title, String cssClass, Class<? extends WebMarkupContainer> panel) {
        super(title);
        this.cssClass = cssClass;
        this.panel = panel;
    }

    /** Model for constructor of the Tab components */
    public IModel<?> getTabModel() {
        return tabModel;
    }

    /** Model for constructor of the Tab components */
    public void setTabModel(IModel<?> tabModel) {
        this.tabModel = tabModel;
    }

    /** Get CSS class name */
    public String getCssClass() {
        return cssClass;
    }

    /** Create new instance of the {@code panel} class.
     * You can owerwrite the method for a special requirements.
     * <br/>Original documentation: @{inheritDoc}
     */
    @Override
    public WebMarkupContainer getPanel(String panelId) throws IllegalStateException {
        try {
            return tabModel != null
                 ? panel.getConstructor(String.class, IModel.class).newInstance(panelId, tabModel)
                 : panel.getConstructor(String.class).newInstance(panelId) ;
        } catch(Throwable e) {
            String msg = String.format
                    ( "Can't create an instance of the class %s with %s constructor argument(s)."
                    , panel.getName()
                    , tabModel != null ? 2 : 1);
            throw new IllegalStateException(msg, e);
        }
    }

}
