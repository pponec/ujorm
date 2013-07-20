package org.ujorm.hotels.gui.customer.action;
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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.panel.Panel;
import org.ujorm.Ujo;
import org.ujorm.wicket.UjoEvent;
import static org.ujorm.wicket.CommonActions.*;

/**
 * The common action panel
 * @author Pavel Ponec
 */
public class CustActionPanel<T extends Ujo> extends Panel {

    public CustActionPanel(String id, final T row) {
        super(id);

        add(new AjaxLink(UPDATE) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                send(getPage(), Broadcast.BREADTH, new UjoEvent(UPDATE, row, target));
            }
        });

        add(new AjaxLink(DELETE) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                send(getPage(), Broadcast.BREADTH, new UjoEvent(DELETE, row, target));
            }
        });
    }

    /** Enable or disable actions */
    public void setActionEnabled(boolean enabled) {
        get(UPDATE).setEnabled(enabled);
        get(DELETE).setEnabled(enabled);
    }
}
