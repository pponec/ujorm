/*
 *  Copyright 2011-2012 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujorm.wicket.component.ujoGrid;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * @author Pavel Ponec
 */
abstract public class GridCellLink extends Panel {

    private static final long serialVersionUID = 1L;

    private final IModel<String> label;

    public GridCellLink(String id) {
        this(id, "UNDEFINED");
    }

    public GridCellLink(String id, String label) {
        super(id);
        this.label = new Model<String>(label);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        // Create Link:
        final AjaxLink<?> linkComponent = new AjaxLink<Void>("link") {
            private static final long serialVersionUID = 1L;
            @Override
            public final void onClick(final AjaxRequestTarget target) {
                GridCellLink.this.onClick(target);
            }
        };
        add(linkComponent);

        // Add label:
        linkComponent.add( new Label("linkLabel", label) );
    }

    /** Událost na klik */
    abstract public void onClick(AjaxRequestTarget target);

    /** Set a Link label */
    public GridCellLink setLabel(String label) {
        this.label.setObject(label);
        return this;
    }

}
