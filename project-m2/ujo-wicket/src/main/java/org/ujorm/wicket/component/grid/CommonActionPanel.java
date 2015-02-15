/*
 * Copyright 2014, Pavel Ponec
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
package org.ujorm.wicket.component.grid;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.ujorm.Ujo;
import org.ujorm.wicket.UjoEvent;

/**
 * The common action panel
 * @author Pavel Ponec
 */
public class CommonActionPanel<U extends Ujo> extends GenericPanel<U> {

    /** Row of the grid */
    protected final U row;

    public CommonActionPanel(String id, final U row, CommonAction ... actions) {
        super(id);
        this.row = row;

        final RepeatingView rvActions = new RepeatingView("actions");
        for (CommonAction action : actions) {
            rvActions.add(createLink(rvActions.newChildId(), action));
        }
        add(rvActions);
    }

    /** Create action */
    protected final AjaxLink createLink(String id, final CommonAction<U> action) {
        final AjaxLink result = new AjaxLink(id) {
            @Override public void onClick(final AjaxRequestTarget target) {
                CommonActionPanel.this.onClick(target, action);
            }
            @Override protected void onConfigure() {
                super.onConfigure();
                setVisibilityAllowed(action.isVisible(row));
            }
        };

        result.setOutputMarkupPlaceholderTag(true);
        result.add(new Label("label", action.getLabel()));
        return result;
    }

    /** On click event */
    protected void onClick(final AjaxRequestTarget target, final CommonAction<U> action) {
        send(getPage(), Broadcast.BREADTH, new UjoEvent(action.getActionId(), row, target));
    }
}
