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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujorm.hotels.gui.*;
import org.ujorm.hotels.gui.about.AboutPanel;
import org.ujorm.hotels.gui.about.BuildInfo;
import org.ujorm.hotels.gui.hotel.HotelTable;
import org.ujorm.hotels.service.param.ApplicationParams;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.component.tabs.UjoTab;
import org.ujorm.wicket.component.tabs.UjoTabbedPanel;

public class SourcePage extends WebPage {

    private static final long serialVersionUID = 1L;

    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SourcePage.class);
    /** Logout */
    public static final String CLASS_PARAM = "src";
    @SpringBean
    private ApplicationParams applParams;
    /** Sources */
    private SourceMap sourceMap = new SourceMap();

    public SourcePage(PageParameters parameters) {
        super(parameters);

        // Create a list of ITab objects used to feed the tabbed panel
        final List<UjoTab> tabs = new ArrayList<UjoTab>();
        for (Class className : sourceMap.getClasses(getClass(parameters))) {
           tabs.add(new UjoTab(className.getSimpleName() + ".java", "booking", SrcTabPanel.class).setTabModel(Model.of(className)));
        }

        add(new UjoTabbedPanel("tabs", tabs) {
            @Override
            protected void onAjaxUpdate(AjaxRequestTarget target) {
                super.onAjaxUpdate(target);
                target.appendJavaScript("prettyPrint();");
            }
        });

        Label label;
        add(new BuildInfo("buildInfo"));
        add(new Label("applicationTitle", MainApplication.APPLICATION_NAME));
        add(label = new Label("applicationHeader", MainApplication.APPLICATION_NAME + " (sources)"));

        if (applParams.isDebugMode()) {
            label.add(new CssAppender("test"));

        }
    }

    /** Returns an argument class */
    private Class<? extends Panel> getClass(PageParameters parameters) {
        Class<? extends Panel> defaultResult = HotelTable.class;
        StringValue srcValue = parameters.get(CLASS_PARAM);
        if (!srcValue.isEmpty()) {
            try {
                final String srcClass = srcValue.toString(defaultResult.getClass().getName());
                return (Class<? extends Panel>) Class.forName(srcClass);
            } catch (Exception e) {
                LOGGER.info("An argument error {}", srcValue, e);
            }

        }

        return defaultResult;
    }

    /** Show the about tab */
    private void showAboutTab(@Nonnull AjaxRequestTarget target) {
        ((UjoTabbedPanel) SourcePage.this.get("tabs")).selectTab(AboutPanel.class, target);
    }

    /** Set a MSIE compatibility mode to a response header */
    @Override
    protected void setHeaders(WebResponse response) {
        super.setHeaders(response);
        response.setHeader("X-UA-Compatible", "IE=edge");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    }

}
