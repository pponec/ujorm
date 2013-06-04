/*
 * Copyright 2013 No company.
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

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author ponec
 */
public class UjoTab extends AbstractTab {

    private final String cssClass;
    private final Class<? extends WebMarkupContainer> panel;

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

    /** Get CSS class name */
    public String getCssClass() {
        return cssClass;
    }

    /** @{@inheritDoc } */
    @Override
    public WebMarkupContainer getPanel(String panelId) throws IllegalStateException {
        try {
            return panel.getConstructor(panelId.getClass()).newInstance(panelId);
        } catch(Throwable e) {
            throw new IllegalStateException("Can't create an instance of the " + panel);
        }
    }

}
