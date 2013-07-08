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
import org.apache.wicket.util.lang.Args;

/**
 * UjoEvent
 * @author Pavel Ponec
 */
public class UjoEvent<T> {

    public static final String SHOW_CREATE = "SHOW_CREATE";
    public static final String SHOW_READ = "SHOW_DISPLAY";
    public static final String SHOW_UPDATE = "SHOW_UPDATE";
    public static final String SHOW_DELETE = "SHOW_DELETE";
    public static final String SHOW_CLONE = "SHOW_COPY";
    public static final String SHOW_EXIT = "SHOW_EXIT";

    public static final String MAKE_CREATE = "MAKE_CREATE";
    public static final String MAKE_READ = "MAKE_DISPLAY";
    public static final String MAKE_UPDATE = "MAKE_UPDATE";
    public static final String MAKE_DELETE = "MAKE_DELETE";
    public static final String MAKE_CLONE = "MAKE_COPY";
    public static final String MAKE_EXIT = "MAKE_EXIT";

    private T content;
    private AjaxRequestTarget target;
    private String action;

    /**
     * Constructor
     * @param action Required action code
     * @param ujo Optional data context
     * @param target target
     */
    public UjoEvent(String action, T ujo, AjaxRequestTarget target) {
        this.action = Args.notNull(action, "action");
        this.content = ujo;
        this.target = target;
    }

    /** Get Ujo domain object */
    public T getContent() {
        return content;
    }

    /** Get Ujo domain model */
    public IModel<T> getUjoModel() {
        return new Model((Serializable)content);
    }

    /** Get target */
    public AjaxRequestTarget getTarget() {
        return target;
    }

    /** Get action */
    public String getAction() {
        return action;
    }

    /**
     * Is the required action?
     * @param action Nullable argument
     * @return The true value for the match.
     */
    public final boolean isAction(String action) {
        return action != null
            && this.action.hashCode() == action.hashCode()
            && this.action.equals(action);
    }


}
