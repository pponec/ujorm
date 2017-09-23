/*
 * Copyright 2014, Pavel Ponec (http://ujorm.org/)
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.tools.MsgFormatter;

/**
 * The ajax wizard is a child of UjoTabbedPanel class.
 * @see UjoWizardBar
 * @author PavelPonec
 */
public class UjoWizardPanel<T extends UjoTab> extends UjoTabbedPanel<T>  {
    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(UjoWizardPanel.class);
    /** ID for the TabsContainer from the parent component */
    private static final String TABS_BAR_ID = "tabs-container";

    /** Constructor */
    public UjoWizardPanel(String id, List<T> tabs) {
        this(id, tabs, null);
    }

    /** Constructor */
    public UjoWizardPanel(String id, List<T> tabs, IModel<Integer> model) {
        super(id, tabs, model);

        // Disable the Tab Container
        final Component component = getTabBar();
        if (component != null) {
            component.setOutputMarkupId(false);
            component.setVisibilityAllowed(false);
        } else {
            final String msg = MsgFormatter.format("Component with the id '{}' is not found", TABS_BAR_ID);
            LOGGER.log(UjoLogger.WARN, msg, new IllegalUjormException(msg));
        }
    }

    /** Returns a tab container */
    protected final WebMarkupContainer getTabBar() {
        return (WebMarkupContainer) get(TABS_BAR_ID);
    }

    /** Get a title of the required tab or returns the {@code null} */
    @Nonnull
    public IModel<String> getNextTitleModel(final boolean next) {
        int i = getSelectedTab() + (next ? 1 : -1);
        return (i >= 0 && i < getTabSize())
            ? getTabs().get(i).getTitle()
            : new Model<String>(null) ;
    }

    /** Get a title of the required tab or returns the {@code null} */
    @Nullable
    public final String getNextTitle(final boolean next) {
        return getNextTitleModel(next).getObject();
    }

}
