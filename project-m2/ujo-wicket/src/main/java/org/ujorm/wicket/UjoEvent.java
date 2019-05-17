/*
 * Copyright 2013-2018 Pavel Ponec
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
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.ILogData;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ujorm.tools.Assert;

/**
 * UjoEvent
 * @author Pavel Ponec
 */
public class UjoEvent<U> {

    /** Undefined event with {@code null} action */
    public static final UjoEvent EMPTY_EVENT = new UjoEvent(CommonActions.UNDEFINED, new DummyTarget());

    final private String action;
    final private boolean showDialog;
    @Nullable
    final private U domain;
    final private AjaxRequestTarget target;

    /**
     * Constructor
     * @param action Required action code
     * @param target target
     */
    public UjoEvent(@Nonnull String action, @Nonnull AjaxRequestTarget target) {
        this(action, (U) null, target);
    }

    /**
     * Constructor
     * @param action Required action code
     * @param ujo Optional data context
     * @param target target
     */
    public UjoEvent(@Nonnull String action, @Nullable U ujo, @Nonnull AjaxRequestTarget target) {
        this(action, true, ujo, target);
    }

    /**
     * Constructor for an Event
     * @param action Required action code
     * @param dialogRequest A request to open a dialog
     * @param ujo Optional data context type of Ujo
     * @param target Target
     */
    public UjoEvent(@Nonnull final String action, boolean dialogRequest, @Nullable U ujo, @Nonnull AjaxRequestTarget target) {
        this.action = Assert.notNull(action, "action");
        this.domain = ujo;
        this.showDialog = dialogRequest;
        this.target = target;
    }

    /** Get the ujo domain object */
    @Nullable
    public U getDomain() {
        return domain;
    }

    /** Get Ujo domain model */
    @Nonnull
    public IModel<U> getUjoModel() {
        return new Model((Serializable)domain);
    }

    /** Get target */
    @Nonnull
    public AjaxRequestTarget getTarget() {
        return target;
    }

    /** Add required component to the target */
    public void addTarget(@Nonnull Component... components) {
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
    public final boolean isAction(@Nonnull final String action) {
        return action != null
            && this.action.hashCode() == action.hashCode()
            && this.action.equals(action);
    }

//    /**
//     * TODO: Check event and stop it on success.
//     * @param action Nullable argument
//     * @return The true value for the match.
//     */
//    public final boolean isActionStop(String action) {
//        boolean result = isAction(action);
//        if (result) {
//            event.stop();
//        }
//        return result
//    }

    /**
     * Check the the required actions from argument to match.
     * @param actions An nonnull argument
     * @return The true value for the match.
     */
    public final boolean isAction(@Nonnull String ... actions) {
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

    // ----------- STATIC ------------

    /** Get Payload type UjoEvent from the argument or return the {@link EMPTY_EVENT}*/
    @Nonnull
    public static <T> UjoEvent<T> get(@Nonnull IEvent<?> argEvent) {
        final Object payLoad = Assert.notNull(argEvent, "argEvent").getPayload();
        return payLoad instanceof UjoEvent
                ? (UjoEvent<T>) payLoad
                : EMPTY_EVENT;
    }

    // ----------- CLASSES ------------

    /** Dumy AjaxRequestTarget */
    private static final class DummyTarget implements AjaxRequestTarget {
        private UnsupportedOperationException newException() {
            return new UnsupportedOperationException(getClass().getSimpleName());
        }

        @Override
        public void addListener(AjaxRequestTarget.IListener listener) {
            throw newException();
        }

        @Override
        public void registerRespondListener(AjaxRequestTarget.ITargetRespondListener listener) {
            throw newException();
        }

        @Override
        public String getLastFocusedElementId() {
            throw newException();
        }

        @Override
        public Page getPage() {
            throw newException();
        }

        @Override
        public void add(Component component, String markupId) {
            throw newException();
        }

        @Override
        public void add(Component... components) {
            throw newException();
        }

        @Override
        public void addChildren(MarkupContainer parent, Class<?> childCriteria) {
            throw newException();
        }

        @Override
        public void appendJavaScript(CharSequence javascript) {
            throw newException();
        }

        @Override
        public void prependJavaScript(CharSequence javascript) {
            throw newException();
        }

        @Override
        public void focusComponent(Component component) {
            throw newException();
        }

        @Override
        public Collection<? extends Component> getComponents() {
            throw newException();
        }

        @Override
        public IHeaderResponse getHeaderResponse() {
            throw newException();
        }

        @Override
        public Integer getPageId() {
            throw newException();
        }

        @Override
        public boolean isPageInstanceCreated() {
            throw newException();
        }

        @Override
        public Integer getRenderCount() {
            throw newException();
        }

        @Override
        public Class<? extends IRequestablePage> getPageClass() {
            throw newException();
        }

        @Override
        public PageParameters getPageParameters() {
            throw newException();
        }

        @Override
        public void respond(IRequestCycle irc) {
            throw newException();
        }

        @Override
        public void detach(IRequestCycle irc) {
            throw newException();
        }

        @Override
        public ILogData getLogData() {
            throw newException();
        }
    }

}
