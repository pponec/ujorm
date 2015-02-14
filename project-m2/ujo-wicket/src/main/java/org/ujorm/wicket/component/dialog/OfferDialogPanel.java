/*
 * Copyright 2015, Pavel Ponec
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
package org.ujorm.wicket.component.dialog;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Ujo;
import org.ujorm.wicket.component.grid.AbstractDataProvider;

/**
 * Offer Dialog Model
 * @author Pavel Ponec
 */
public class OfferDialogPanel<T extends Ujo> extends AbstractDialogPanel<T> {
    private static final long serialVersionUID = 20150212L;

    /** Input fields provider */
    protected final OfferModel<T> model;

    public OfferDialogPanel(ModalWindow modalWindow, OfferModel<T> model) {
        super(modalWindow, new Model(), false);
        this.repeater.setVisibilityAllowed(false);
        this.model = model;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        // Create the data table:
        form.add(model.createDataTable());
    }

    /** Get Table Data Provider
     * @return  */
    public AbstractDataProvider<T> getColumns() {
        return model.getProvider();
    }

    /**
     * Feedback method is not supported
     * @param message
     * @deprecated
     */
    @Override
    @Deprecated
    protected void setFeedback(IModel<String> message) {
        if (message != null) {
            throw new UnsupportedOperationException("Not supported");
        }
    }

    /** Refresh DataTable */
    public void reloadTable(final AjaxRequestTarget target) {
        target.add(form.get(AbstractDataProvider.DEFAULT_DATATABLE_ID));
    }

}
