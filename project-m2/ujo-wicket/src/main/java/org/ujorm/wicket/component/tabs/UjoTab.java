/*
 * Copyright 2013-2018 Pavel Ponec
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.tools.msg.MsgFormatter;
import org.ujorm.wicket.function.UjoSupplier;

/**
 * Convenience class that takes care of common ITab functionality
 * @author Pavel Ponec
 */
public class UjoTab extends AbstractTab {

    /** Name of the CSS class */
    private final String cssClass;
    /** Default class for creating a panel of the tab */
    private final Class<? extends WebMarkupContainer> panel;
    /** Model for tab's component panel */
    private IModel<?> tabModel;
    /** Optional visible model */
    private UjoSupplier<Boolean> visible;

    /** Constructor */
    public UjoTab(@NotNull final IModel<String> title, @NotNull final Class<? extends WebMarkupContainer> panel) {
        this(title, null, panel);
    }

    /** Constructor */
    public UjoTab(@NotNull final String title, String cssClass, @NotNull final Class<? extends WebMarkupContainer> panel) {
        this(Model.of(title), cssClass, panel);
    }

    /** Constructor */
    public UjoTab
        ( @NotNull final String title
        , @Nullable final String cssClass
        , @NotNull final Class<? extends WebMarkupContainer> panel
        , @Nullable final UjoSupplier<Boolean> visible) {
        this(Model.of(title), cssClass, panel, visible);
    }

    /** Constructor */
    public UjoTab
        ( @NotNull final IModel<String> title
        , @Nullable final String cssClass
        , @NotNull final Class<? extends WebMarkupContainer> panel) {
        this(title, cssClass, panel, (UjoSupplier<Boolean>) null);
    }

    /**
     * Constructor
     * @param title Title model
     * @param cssClass CSS class
     * @param panel Panel class
     * @param visible Optional visible model
     */
    public UjoTab
            ( @NotNull IModel<String> title
            , @Nullable String cssClass
            , @NotNull Class<? extends WebMarkupContainer> panel
            , @Nullable UjoSupplier<Boolean> visible) {
        super(title);
        this.cssClass = cssClass;
        this.panel = panel;
        this.visible = visible;
    }

    /** The method get a visibleModel if any */
    @Override
    public boolean isVisible() {
        return visible != null
             ? Boolean.TRUE.equals(visible.get())
             : super.isVisible();
    }

    /** Model for constructor of the Tab components */
    public IModel<?> getTabModel() {
        return tabModel;
    }

    /** Model for tab's component panel */
    public UjoTab setTabModel(IModel<?> tabModel) {
        this.tabModel = tabModel;
        return this;
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
            String msg = MsgFormatter.format
                    ( "Can't create an instance of the class {} with {} constructor argument(s)."
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
