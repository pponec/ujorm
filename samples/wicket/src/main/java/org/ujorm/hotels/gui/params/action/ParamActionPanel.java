package org.ujorm.hotels.gui.params.action;
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

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.hotels.services.AuthService;
import org.ujorm.wicket.UjoEvent;
import static org.ujorm.wicket.CommonActions.*;

/**
 * The common action panel
 * @author Pavel Ponec
 */
public class ParamActionPanel<T extends KeyValue> extends GenericPanel<T> {

    @SpringBean
    private AuthService authService;

    /** Table row */
    private T row;

    public ParamActionPanel(String id, T rowPar) {
        super(id);
        this.row = rowPar;
        add(createLink(UPDATE, false));
    }

    /** Create new Link */
    protected final AjaxLink createLink(String action, final boolean admin) {
        return new AjaxLink(action) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                send(getPage(), Broadcast.BREADTH, new UjoEvent(getId(), row, target));
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (DELETE.equals(getId())) {
                    setVisibilityAllowed(admin == authService.isAdmin());
                } else {
                    boolean canUpdate = authService.isAdmin();
                    boolean isUpdate = UPDATE.equals(getId());
                    setVisibilityAllowed(isUpdate == canUpdate);
                }
            }
        };
    }
}
