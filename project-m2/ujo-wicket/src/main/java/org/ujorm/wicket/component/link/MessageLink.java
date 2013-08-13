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
package org.ujorm.wicket.component.link;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;


/**
 * Ajax message link
 * @author Pavel Ponec
 */
public class MessageLink extends Panel {

    /**
     * Constructor
     * @param id Component id
     * @param message Message
     */
    public MessageLink(String id, IModel<String> message) {
        super(id, message);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final AjaxLink link = new AjaxLink("link") {
            @Override public void onClick(AjaxRequestTarget target) {
                MessageLink.this.onClick(target);
            }
        };
        add(link);
        link.add(new Label("message", getDefaultModel()));

    }

    /** Owerwrite the method  */
    public void onClick(AjaxRequestTarget target) {
    }

}
