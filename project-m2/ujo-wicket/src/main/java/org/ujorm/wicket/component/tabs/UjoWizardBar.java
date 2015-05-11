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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.component.link.MessageLink;

/**
 * The WizardBar
 * <h4>Example</h4>
 * <pre class="pre">
 * // Create a step list:
 * List&lt;UjoTab&gt; tabs = new ArrayList&lt;&gt;();
 * tabs.add(new UjoTab("Step 1", "step1", Step1.class).setModel(model));
 * tabs.add(new UjoTab("Step 2", "step2", Step2.class).setModel(model));
 *
 * // Wizard Panel:
 * add(tabbedPanel = new UjoWizardPanel&lt;UjoTab&gt;("wizard", tabs));
 * tabbedPanel.setVisibilityAllowed(true);
 *
 * // Wizard Bar:
 * add(new UjoWizardBar("wizardBar", tabbedPanel) {
 *     protected void onUpdate(AjaxRequestTarget target) {
 *         // Refresh jQuery components:
 *         target.appendJavaScript("location.reload(false);");
 *     }
 * });
 * </pre>
 * @author Pavel Ponec
 */
public class UjoWizardBar extends GenericPanel<Object> {
    /** The repeater  */
    private RepeatingView repeater;
    private UjoWizardPanel<UjoTab> wizard;

    public UjoWizardBar(String id, UjoWizardPanel<UjoTab> aWizard) {
        super(id);
        this.wizard = aWizard;
        add(repeater = new RepeatingView("repeaterItem"));

        final List<UjoTab> tabs = wizard.getTabs();
        for (int i = 0; i < tabs.size(); ++i) {
            final UjoTab tab = tabs.get(i);
            final int ii = i;
            final MessageLink link = new MessageLink(repeater.newChildId(), tab.getTitle()) {
                @Override protected void onClick(AjaxRequestTarget target) {
                    if (isActionAllowed()) {
                        wizard.selectTab(ii, target);
                        target.add(UjoWizardBar.this);
                        onUpdate(target);
                    }
                }
                private boolean isActionAllowed() {
                    return isEnabled() && ii < wizard.getSelectedTab();
                }
            };
            link.setOutputMarkupId(true);
            link.add(new CssAppender(tab.getCssClass()));
            repeater.add(link);
        }
    }

    /** Set CSS styles for all items before renderer */
    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        final int selected = wizard.getSelectedTab();
        int i = 0;
        for (Component item : repeater) {
            final String cssClass = wizard.getTabs().get(i).getCssClass();
            item.add(new AttributeModifier(CssAppender.CSS_CLASS, cssClass));

            if (i == selected) {
                item.add(new CssAppender("selected"));
            }
            if (i > selected) {
                item.add(new CssAppender("disabled"));
            }
            ++i;
        }
    }

    /** On update event to overriding
     * @param target target */
    protected void onUpdate(AjaxRequestTarget target) {
    }
}
