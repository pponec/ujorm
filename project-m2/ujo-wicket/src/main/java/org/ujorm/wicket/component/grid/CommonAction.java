/*
 * Copyright 2014, Pavel Ponec
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
package org.ujorm.wicket.component.grid;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.ujorm.Ujo;

/**
 * The common action panel
 * @author Pavel Ponec
 */
public class CommonAction<U extends Ujo> implements Serializable {
    /** Default Action label prefix is {@code "action.label."} */
    public static final String DEFAULT_PREFIX = "action.label.";

    /** Action ID */
    private final String actionId;
    /** Label of the action */
    private final IModel<String> label;

    /** Create Resource model label using {@code actionId} with a prefix {@code "label.action."} */
    public CommonAction(String actionId) {
        this(actionId, null);
    }

    /** Constructor with full arguments
     * @param actionId Action identifier
     * @param label Optional Label, the {@code null} value is replaced by the key: {@code "label.action." + actionId}.
     * @param visibleModel Visible model is optional
     */
    public CommonAction(@Nonnull String actionId, @Nullable IModel<String> label) {
        this.actionId = actionId;
        this.label = label != null ? label
                : new ResourceModel(DEFAULT_PREFIX + actionId, actionId);
    }

    /** Action id */
    public String getActionId() {
        return actionId;
    }

    /** Label of the action */
    public IModel<String> getLabel() {
        return label;
    }

    /** Is the action visibled? The method is ready to to owerriding. */
    public boolean isVisible(U row) {
        return true;
    }

    // ------- STATIC METHODS -------

    /** A common action factory */
    public static CommonAction[] of(String ... actions) {
        final CommonAction[] result = new CommonAction[actions.length];
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = new CommonAction(actions[i]);
        }
        return result;
    }

}
