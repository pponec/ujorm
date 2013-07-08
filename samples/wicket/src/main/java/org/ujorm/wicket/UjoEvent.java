/*
 * Copyright 2013 Pavel Ponec
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
package org.ujorm.wicket;

import java.io.Serializable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Ujo;

/**
 * UjoEvent
 * @see http://www.wicket-library.com/wicket-examples/events/wicket/bookmarkable/org.apache.wicket.examples.source.SourcesPage?0&SourcesPage_class=org.apache.wicket.examples.events.IndexPage&source=DecoupledAjaxUpdatePage.java
 * @see http://savicprvoslav.blogspot.cz/2012/06/wicket-15-inter-component-events.html
 * @see http://balamaci.wordpress.com/2011/04/19/wicket-1-5-intercomponent-comunication/
 * @see http://wickeria.com/blog/12-05-23-par-tipu-pro-praci-s-apache-wicket
 * @author Pavel Ponec
 */
public class UjoEvent<T extends Ujo> {

    public static final String CREATE = "CREATE";
    public static final String READ = "DISPLAY";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String CLONE = "COPY";
    public static final String EXIT = "EXIT";


    private T ujo;
    private AjaxRequestTarget target;
    private String context;

    public UjoEvent(String context, T ujo, AjaxRequestTarget target) {
        this.context = context;
        this.ujo = ujo;
        this.target = target;
    }

    /** Get Ujo domain object */
    public T getUjo() {
        return ujo;
    }

    /** Get Ujo domain model */
    public IModel<T> getUjoModel() {
        return new Model((Serializable)ujo);
    }

    public AjaxRequestTarget getTarget() {
        return target;
    }

    public String getContext() {
        return context;
    }

}
