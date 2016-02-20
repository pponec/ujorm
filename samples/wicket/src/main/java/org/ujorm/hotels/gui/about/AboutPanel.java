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
package org.ujorm.hotels.gui.about;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.springframework.core.SpringVersion;
import org.ujorm.core.UjoManager;

/**
 * AboutPanel
 * @author Pavel Ponec
 */
public class AboutPanel extends Panel {

    public AboutPanel(String id) {
        super(id);

       add(new Label("javaVersion", System.getProperty("java.version")));
       add(new Label("wicketVersion", getApplication().getFrameworkSettings().getVersion()));
       add(new Label("ujormVersion", UjoManager.version()));
       add(new Label("springVersion", SpringVersion.getVersion()));
       add(new AjaxLink("waiting") {
           @Override
           public void onClick(AjaxRequestTarget target) {
               try {
                   Thread.sleep(4000);
               } catch (InterruptedException e) {
                   Thread.currentThread().interrupt();
               }
           }
       });
    }
}
