/*
 * Copyright 2014-2022 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.wicket.component.tools;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.Strings;

/**
 * {@link IChoiceRenderer} implementation that makes it easy to work with java 5 enums. This
 * renderer will attempt to lookup strings used for the display value using a localizer of a given
 * component. If the component is not specified, the global instance of localizer will be used for
 * lookups.
 * <p>
 * display value resource key format: {@code <enum.getSimpleClassName()>.<enum.name()>}
 * </p>
 * <p>
 * id value format: {@code <enum.name()>}
 * </p>
 *
 * @author igor.vaynbergm, pavel.ponec
 *
 * @param <T>
 */
public class ChoiceRendererNullable<T extends Enum<T>> implements IChoiceRenderer<T> {

    private static final long serialVersionUID = 1L;

    /** Undefined item Key */
    protected static final String UNDEFINED_ITEM_KEY = "undefinedItem";

    /**
     * Component used to resolve i18n resources for this renderer.
     */
    private final Component resourceSource;

    /**
     * Constructor that creates the choice renderer that will use global instance of localizer to
     * resolve resource keys.
     */
    public ChoiceRendererNullable() {
        this(null);
    }

    /**
     * Constructor
     * @param resourceSource
     */
    public ChoiceRendererNullable(Component resourceSource) {
        this.resourceSource = resourceSource;
    }

    /** {@inheritDoc} */
    @Override
    public Object getDisplayValue(T item) throws NullPointerException {
        final String key = resourceKey(item);
        final String defaultValue = item != null ? item.name() : "-";
        final String value = resourceSource != null
                ? resourceSource.getString(key, null, defaultValue)
                : Application.get().getResourceSettings().getLocalizer().getString(key, null, defaultValue);
        return postprocess(value);
    }

    /**
     * Translates the {@code object} into resource key that will be used to lookup the value shown
     * to the user
     *
     * @param item
     * @return resource key
     */
    protected String resourceKey(T item) {
        return item != null
                ? Classes.simpleName(item.getDeclaringClass()) + '.' + item.name()
                : getUndefinedKey();
    }

    /**
     * Post-processes the {@code value} after it is retrieved from the localizer. Default
     * implementation escapes any markup found in the {@code value}.
     *
     * @param value
     * @return post-processed value
     */
    protected CharSequence postprocess(String value) {
        return Strings.escapeMarkup(value);
    }

    /** {@inheritDoc} */
    @Override
    public String getIdValue(T item, int index) {
        return item != null ? item.name() : getUndefinedKey();
    }

    /** Localization key for undefined value */
    protected String getUndefinedKey() {
        return UNDEFINED_ITEM_KEY;
    }

    @Override
    @Nullable
    public T getObject(String id, IModel<? extends List<? extends T>> choices) {
        final List<? extends T> choiceList = choices.getObject();
        for (int i = 0, max = choiceList.size(); i < max; i++) {
            final T choice = choiceList.get(i);
            if (getIdValue(choice, i).equals(id)) {
                return choice;
            }
        }
        return null;
    }
}
