/*
 * Copyright 2013-2014, Pavel Ponec
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
package org.ujorm.wicket.component.toolbar;

import org.jetbrains.annotations.Nullable;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.ujorm.Ujo;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.wicket.UjoEvent;
import static org.ujorm.wicket.CommonActions.*;

/**
 * The common action Toolbar for an insert
 * @author Pavel Ponec
 */
public class InsertToolbar<U extends Ujo> extends AbstractToolbar {

    /** Type of a domain object */
    private final Class<U> domainType;

    /** Actions are enabled  */
    @Nullable
    private IModel<Boolean> disableModel;

    /**
     * Constructor with a default action name {@link org.ujorm.wicket.CommonActions#UPDATE}
     * @param dataTable DataTable
     * @param domainType Type of the domain object
     */
    public InsertToolbar(DataTable dataTable, Class<U> domainType) {
        this(dataTable, domainType, UPDATE);
    }

    /**
     * Constructor
     * @param dataTable DataTable
     * @param domainType Type of the domain object
     */
    public InsertToolbar(final DataTable dataTable, final Class<U> domainType, final String actionName) {
        super(dataTable);
        super.setOutputMarkupPlaceholderTag(true);
        this.domainType = domainType;

        final WebMarkupContainer td = new WebMarkupContainer("space");
        td.add(new AttributeModifier("colspan", Math.max(1, dataTable.getColumns().size() - 1)));
        add(td);

        final AjaxLink link = createLink(actionName);
        add(link);
        link.add(createLabel("label"));
    }

    /** Is visible */
    @Override
    public boolean isVisible() {
        return disableModel != null
            ? !disableModel.getObject()
            : super.isVisible();
    }

    /** Create action Link and registre an event on the click */
    protected AjaxLink createLink(final String action) {
        return new AjaxLink("insert") {
            @Override public void onClick(AjaxRequestTarget target) {
                onLinkClick(target, action);
            }
        };
    }

    /**
     * Default implementation on Link click
     * @param target AJAX target
     * @param action Action name
     */
    protected void onLinkClick(final AjaxRequestTarget target, final String action) {
        send(getPage(), Broadcast.BREADTH, new UjoEvent(action, newTypeInstance(), target));
    }

    /** Create a Label compoment */
    protected Label createLabel(String id) {
        return new Label("label", new ResourceModel("action.insert", "Create"));
    }

    /** Return an instance of the type */
    protected U newTypeInstance() throws IllegalStateException {
        try {
            return getDomainType().newInstance();
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalUjormException("Can't create instance of the " + getDomainType(), e);
        }
    }

    /** The type of a domain object */
    public Class<U> getDomainType() {
        return domainType;
    }

    /** @return the disableModel */
    public IModel<Boolean> getDisableModel() {
        return disableModel;
    }

    /** @param disableModel the disableModel to set */
    public InsertToolbar setDisableModel(IModel<Boolean> disableModel) {
        this.disableModel = disableModel;
        return this;
    }
}
