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
package org.ujorm.wicket.component.dialog.domestic;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Ujo;
import org.ujorm.criterion.Criterion;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.grid.AbstractDataProvider;

/**
 * Offer Dialog Model
 * @author Pavel Ponec
 */
public class OfferDialogPane<T extends Ujo & Serializable> extends AbstractDialogPane<T> {
    private static final long serialVersionUID = 20150212L;
    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(OfferDialogPane.class);
    /** Finder field(s) */
    private OfferToolbar<T> toolbar;

    /** Input fields provider */
    protected final OfferModel<T> model;

    public OfferDialogPane(ModalWindow modalWindow, OfferModel<T> model) {
        super(modalWindow, new Model(), false);
        this.repeater.setVisibilityAllowed(false);
        this.model = model;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        // Finding toolbar:
        form.add(toolbar = new OfferToolbar("toolbar", model.getFinders()));
        toolbar.setVisibilityAllowed(model.isEnableToolbar());
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

    /** Creates the default Confirmation button */
    @Override protected AjaxButton createActionButton(String id, String propertyName) {
        final AjaxButton result = new AjaxButton
                ( id
                , getButtonModel(propertyName)
                , form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    // target.add(form);
                    final DataTable<T,?> dataTable = getTable();
                    final long firstRowOnPage = dataTable.getCurrentPage() * dataTable.getItemsPerPage();
                    final T row = dataTable.getItemCount() >= 0
                            ? dataTable.getDataProvider().iterator(firstRowOnPage, 1).next()
                            : null;
                    if (row != null) {
                        model.getClosable().closeDialog(target, row);
                    }
                } catch (Throwable e) {
                    LOGGER.log(UjoLogger.WARN, "Wrong selection", e);
                    // setFeedback(e); // TODO (?)
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
               target.add(form);
            }
        };
        result.add(new CssAppender("btn btn-primary"));
        form.setDefaultButton(result);
        return result;
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        final UjoEvent<T> event = UjoEvent.get(argEvent);
        if (event != null) {
            if (event.isAction(OfferToolbar.FILTER_ACTION)) {
                final Criterion<T> crn1, crn2, crn3;
                crn1 = model.getFilter();
                crn2 = toolbar.getCriterion().getObject();
                crn3 = crn2 != null ? crn1.and(crn2) : crn1;
                model.getFilterModel().setObject(crn3);
                reloadTable(event.getTarget());
                argEvent.stop();
            }
        }
    }

    /** Refresh DataTable */
    public void reloadTable(final AjaxRequestTarget target) {
        target.add(getTable());
    }

    /** Get table component */
    protected <S> DataTable<T, S> getTable() {
        return (DataTable<T, S>) form.get(AbstractDataProvider.DEFAULT_DATATABLE_ID);
    }

    /** Set a focus to the first component by default */
    public void requestFocus(@Nonnull final AjaxRequestTarget target) {
        toolbar.requestFocus(target);
    }
}
