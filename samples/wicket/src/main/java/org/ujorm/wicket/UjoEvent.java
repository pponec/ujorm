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
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Args;

/**
 * UjoEvent
 * @author Pavel Ponec
 */
public class UjoEvent<U> {

    final private String action;
    final private boolean showDialog;
    final private U domain;
    final private AjaxRequestTarget target;

    /**
     * Constructor
     * @param action Required action code
     * @param target target
     */
    public UjoEvent(String action, AjaxRequestTarget target) {
        this(action, (U) null, target);
    }

    /**
     * Constructor
     * @param action Required action code
     * @param ujo Optional data context
     * @param target target
     */
    public UjoEvent(String action, U ujo, AjaxRequestTarget target) {
        this(action, true, ujo, target);
    }

    /**
     * Constructor for an Event
     * @param action Required action code
     * @param dialogRequest A request to open a dialog
     * @param ujo Optional data context type of Ujo
     * @param target Target
     */
    public UjoEvent(String action, boolean dialogRequest, U ujo, AjaxRequestTarget target) {
        this.action = Args.notNull(action, "action");
        this.domain = ujo;
        this.showDialog = dialogRequest;
        this.target = target;
    }

    /** Get the ujo domain object */
    public U getDomain() {
        return domain;
    }

    /** Get Ujo domain model */
    public IModel<U> getUjoModel() {
        return new Model((Serializable)domain);
    }

    /** Get target */
    public AjaxRequestTarget getTarget() {
        return target;
    }

    /** Add required component to the target */
    public void addTarget(Component... components) {
        target.add(components);
    }

    /** Get action */
    public String getAction() {
        return action;
    }

    /**
     * Is it the required action?
     * @param action Nullable argument
     * @return The true value for the match.
     */
    public final boolean isAction(String action) {
        return action != null
            && this.action.hashCode() == action.hashCode()
            && this.action.equals(action);
    }

    /**
     * Check the the required actions from argument to match.
     * @param action Nullable argument
     * @return The true value for the match.
     */
    public final boolean isAction(String ... actions) {
        for (String act : actions) {
            if (isAction(act)) {
                return true;
            }
        }
        return false;
    }

    /** A request to open a dialog */
    public boolean showDialog() {
        return showDialog;
    }

    /** To string */
    @Override
    public String toString() {
        return "UjoEvent"
                + "{ action=" + action
                + ", showDialog=" + showDialog
                + ", domain=" + domain
                + '}';
    }

}
