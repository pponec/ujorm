/*
 * Copyright 2016, Pavel Ponec (http://ujorm.org/)
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
package org.ujorm.wicket.component.waiting;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.ujorm.wicket.CssAppender;

/**
 * Waiting icon for AJAX events.
 * @author Pavel Ponec
 * @see IAjaxIndicatorAware
 */
public class WaitingIcon extends Panel {
    /** Default CSS style */
    public static final String DEFAULT_CSS_STYLE = "waitingIcon";

    /** Constructor with the CSS style: {@code "waitingIcon"}
     @see #DEFAULT_CSS_STYLE */
    public WaitingIcon(final String id) {
        this(id, DEFAULT_CSS_STYLE);
    }

    /** Common constructor */
    public WaitingIcon(final String id, final String cssStyle) {
        super(id);
        setOutputMarkupId(true);
        add(new CssAppender(cssStyle));
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(createCssResource(getClass())));
    }

    /** Create CSS resource */
    protected CssResourceReference createCssResource(final Class<?> clazz) {
        return new CssResourceReference(clazz, clazz.getSimpleName() + ".css");
    }

}
