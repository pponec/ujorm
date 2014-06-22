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
package org.ujorm.wicket.component.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.ujorm.Key;
import org.ujorm.core.KeyRing;

/**
 * FieldEvent
 * @author Pavel Ponec
 */
public class FieldEvent {

    final private String action;
    final private KeyRing sourceKey;
    final private AjaxRequestTarget target;

    public FieldEvent(String action, KeyRing sourceKey, AjaxRequestTarget target) {
        this.action = action;
        this.sourceKey = sourceKey;
        this.target = target;
    }

    public String getAction() {
        return action;
    }

    public Key getSourceKey() {
        return sourceKey.getFirstKey();
    }

    public AjaxRequestTarget getRequestTarget() {
        return target;
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

    /** To string */
    @Override
    public String toString() {
        return "UjoEvent"
                + "{ action=" + action
                + ", sourceKey=" + sourceKey.getFirstKey().getFullName()
                + '}';
    }

    // ----------- STATIC ------------

    /** Get Payload type UjoEvent from the argument */
    public static FieldEvent get(IEvent<?> argEvent) {
        final Object payLoad = argEvent.getPayload();
        return payLoad instanceof FieldEvent
                ? (FieldEvent) payLoad
                : null ;
    }

}
