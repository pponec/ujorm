/*
 * Copyright 2013-2015 Pavel Ponec
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

import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.core.IllegalUjormException;

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
    /** Optional visible model */
    private IModel<Boolean> visibleModel;

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
        this(title, cssClass, panel, (Model<Boolean>) null);
    }

    /**
     * Constructor
     * @param title Title model
     * @param cssClass CSS class
     * @param panel Panel class
     * @param visible Optional visible model
     */
    public UjoTab
            ( @Nonnull IModel<String> title
            , @Nullable String cssClass
            , @Nonnull Class<? extends WebMarkupContainer> panel
            , @Nullable IModel<Boolean> visible) {
        super(title);
        this.cssClass = cssClass;
        this.panel = panel;
        this.visibleModel = visible;
    }

    /** The method get a visibleModel if any */
    @Override
    public boolean isVisible() {
        return visibleModel != null
                ? Boolean.TRUE.equals(visibleModel.getObject())
                : super.isVisible();
    }

    /** Model for constructor of the Tab components */
    public IModel<?> getTabModel() {
        return tabModel;
    }

    /** Model for constructor of the Tab components */
    public void setTabModel(IModel<?> tabModel) {
        this.tabModel = tabModel;
    }

    /** Model for constructor of the Tab components */
    public UjoTab setModel(IModel<?> tabModel) {
        setTabModel(tabModel);
        return this;
    }

    /** Get CSS class name */
    public String getCssClass() {
        return cssClass;
    }

    /** Create new instance of the {@code panel} class.
     * You can overwrite the method for a special requirements.
     * <br>Original documentation: @{inheritDoc}
     */
    @Override
    public WebMarkupContainer getPanel(String panelId) throws IllegalStateException {
        try {
            return tabModel != null
                 ? panel.getConstructor(String.class, IModel.class).newInstance(panelId, tabModel)
                 : panel.getConstructor(String.class).newInstance(panelId) ;
        } catch(RuntimeException | ReflectiveOperationException e) {
            String msg = String.format
                    ( "Can't create an instance of the class %s with %s constructor argument(s)."
                    , panel.getName()
                    , tabModel != null ? 2 : 1);
            throw new IllegalUjormException(msg, e);
        }
    }

    /** Get panel class */
    public Class<? extends WebMarkupContainer> getPanelClass() {
        return panel;
    }

}
