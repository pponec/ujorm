/*
 * Copyright 2019-2019, Pavel Ponec
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
package org.ujorm.hotels.sources;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.hotels.service.param.ApplicationParams;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.component.link.MessageLink;

/**
 * Source Link
 * @author Pavel Ponec
 */
public class SrcLinkPanel extends Panel {

    @SpringBean
    private ApplicationParams params;

    public SrcLinkPanel(final String id, final Panel sourceComponent) {
        super(id);

        MessageLink msgLink = new MessageLink("srcLink", Model.of("Show a source code")) {
            @Override
            protected void onClick(AjaxRequestTarget target) {
                PageParameters params = new PageParameters();
                params.add(SourcePage.SOURCE_PARAM, sourceComponent.getClass().getName());
                params.add(SourcePage.INDEX_PARAM, 0);
                setResponsePage(SourcePage.class, params);
            }
        };
        msgLink.add(new CssAppender("srcLink"));
        msgLink.setVisibilityAllowed(params.getLinkToSources());

        add(msgLink);
    }
}
