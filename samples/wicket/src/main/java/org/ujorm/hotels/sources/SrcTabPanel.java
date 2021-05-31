/*
 * Copyright 2019-2021, Pavel Ponec
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

import java.io.InputStream;
import javax.annotation.Nonnull;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujorm.tools.msg.MsgFormatter;
import org.ujorm.tools.common.StringUtils;

/**
 * AboutPanel
 * @author Pavel Ponec
 */
public class SrcTabPanel extends GenericPanel<Class> {

    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SrcTabPanel.class);

    public SrcTabPanel(String id, IModel<Class> model) {
        super(id, model);

       add(new Label("javaVersion", System.getProperty("java.version")));
       add(new Label("source", getResourceAsString(getModelObject())));

       add(new AttributeAppender("onclick", new Model("prettyPrint();"), ";"));
    }

    /**
     * Reads given resource file as a string.
     *
     * @param javaClass the path to the resource file
     * @return the file's contents or null if the file could not be opened
     */
    public static String getResourceAsString(@Nonnull Class javaClass) {
        try {
            final String javaSource = MsgFormatter.format("/{}.java", javaClass.getName().replace('.', '/'));
            final InputStream is = javaClass.getClassLoader().getResourceAsStream(javaSource); //
            if (is != null) {
                return StringUtils.read(is);
            }
        } catch (Exception e) {
            LOGGER.warn("Wrong resource " + javaClass, e);
        }
        return String.valueOf(javaClass);
    }
}
