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
    /** Parameter for the source */
    public static final String SOURCE_PARAM = "src";
    /** Parameter for the source index */
    public static final String INDEX_PARAM = "idx";
    @SpringBean
    private ApplicationParams applParams;
    /** Sources */
    private final SourceMap sourceMap = new SourceMap();

    public SourcePage(PageParameters parameters) {
        super(parameters);

        // Create a list of ITab objects used to feed the tabbed panel
        final List<UjoTab> tabs = new ArrayList<UjoTab>();
        final List<Class> sources = sourceMap.getClasses(getClass(parameters));
        final int index = getIndex(INDEX_PARAM, sources, parameters);

        for (Class className : sources) {
           tabs.add(new UjoTab(className.getSimpleName() + ".java", "source", SrcTabPanel.class).setTabModel(Model.of(className)));
        }

        final UjoTabbedPanel tabsPanel = new UjoTabbedPanel("srcTabs", tabs) {
            @Override
            protected void onAjaxUpdate(AjaxRequestTarget target) {
                super.onAjaxUpdate(target);
                target.appendJavaScript("prettyPrint();");
            }
        };
        tabsPanel.setSelectedTab(index);
        add(tabsPanel);

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
        StringValue srcValue = parameters.get(SOURCE_PARAM);
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

    /** Set a MSIE compatibility mode to a response header */
    @Override
    protected void setHeaders(WebResponse response) {
        super.setHeaders(response);
        response.setHeader("X-UA-Compatible", "IE=edge");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    }

    /** Get index  */
    private int getIndex(String key, List<Class> sources, PageParameters parameters) {
        final int defaultResult = 0;
        final StringValue strValue = parameters.get(key);
        try {
            if (!strValue.isEmpty()) {
                int i = strValue.toInt(0);
                return sources.get(i) != null ? i : defaultResult;
            } else {
                return defaultResult;
            }
        } catch (Exception e) {
            LOGGER.info("Illegal argument value {}={}", key, strValue);
            return defaultResult;
        }
    }

}
