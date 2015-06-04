/*
 * Copyright 2015, Pavel Ponec (http://ujorm.org/)
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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;

/**
 * The Wizard component
 * @author Pavel Ponec
 */
public class UjoWizard extends GenericPanel<Object> {
    protected static final String WIZARD_PANEL_ID = "wizardPanel";
    protected static final String WIZARD_BAR_ID = "wizardBar";

    protected final UjoWizardBar wizardBar;

    public UjoWizard(String id,  List<UjoTab> tabs) {
        super(id);

        // Wizard Panel:
        final UjoWizardPanel<UjoTab> tabbedPanel;
         add(tabbedPanel = new UjoWizardPanel<UjoTab>(WIZARD_PANEL_ID, tabs){
            @Override protected void onAjaxUpdate(AjaxRequestTarget target) {
                super.onAjaxUpdate(target);
            }
        });

        // Wizard Bar:
        add(wizardBar = new UjoWizardBar(WIZARD_BAR_ID, tabbedPanel){
            @Override protected void onUpdate(AjaxRequestTarget target) {
                onUpdateWizardBar(target);
            }
        });
    }

    /** For a common use */
    protected UjoWizard(String id, UjoWizardBar paramWizardBar) {
        super(id);
        this.wizardBar = paramWizardBar;
        add(wizardBar.getWizardPanel());
        add(wizardBar);
    }

    /** Refresh jQuery components */
    protected void onUpdateWizardBar(AjaxRequestTarget target) {
        target.appendJavaScript("location.reload(false);");
    }

    /** Get wizard bar */
    public UjoWizardBar getWizardBar() {
        return wizardBar;
    }

    /** Select the next/prev tab */
    public void selectNextTab(final boolean next, final AjaxRequestTarget target) {
        wizardBar.getWizardPanel().selectNextTab(next, target);
        target.add(wizardBar);
    }

    /** Get selected tab panel */
    public WebMarkupContainer getSelectedTabPanel() {
        return wizardBar.getWizardPanel().getSelectedTabPanel();
    }
}
