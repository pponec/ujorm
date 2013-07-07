/*
 * Copyright 2013, Pavel Ponec
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
package org.ujorm.hotels.gui.hotel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Ujo;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.component.form.FieldProvider;

/**
 * Hotel Editor
 * @author Pavel Ponec
 */
public class HotelEditor extends Panel {
    private static final long serialVersionUID = 20130621L;

    private Form<?> form;
    private ModalWindow modalWindow;
    private FieldProvider fields;

    public HotelEditor(ModalWindow modalWindow, IModel<Hotel> model) {
        super(modalWindow.getContentId(), model);
        this.modalWindow = modalWindow;
        this.setOutputMarkupId(true);
        this.setOutputMarkupPlaceholderTag(true);

        // Form:
        this.add(form = new Form("hotelForm"));
        form.setOutputMarkupId(true);
        form.add(createSaveButton("saveButton", "Save"));
        form.add(createCancelButton("cancelButton", "Cancel"));

        // Field Factory:
        form.add((fields = new FieldProvider("fieldRepeater")).getRepeatingView());

        // Editable fields:
        fields.add(Hotel.NAME);
        fields.add(Hotel.CITY, City.ID.forAll(), City.NAME);
        fields.add(Hotel.STREET);
        fields.add(Hotel.PHONE);
        fields.add(Hotel.STARS);
        fields.add(Hotel.PRICE);
        fields.add(Hotel.CURRENCY);
        fields.add(Hotel.NOTE);
        fields.add(Hotel.ACTIVE);

        modalWindow.setContent(this);
    }

    /** Returns a current entity */
    final public Hotel getHotel() {
        return (Hotel) getDefaultModelObject();
    }

    /** Vytvoří textfield pro aktuání model */
    private AjaxButton createSaveButton(String id, String name) {
        final AjaxButton result = new AjaxButton(id, Model.of(name), form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
                modalWindow.close(target);
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

    /** Vytvoří textfield pro aktuání model */
    private AjaxButton createCancelButton(String id, String name) {
        final AjaxButton result = new AjaxButton(id, Model.of(name), form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                close(target, form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                close(target, form);
            }

            /** Close action */
            private void close(AjaxRequestTarget target, Form<?> form) {
                form.clearInput();
                target.add(form);
                modalWindow.close(target);
            }
        };
        result.add(new CssAppender("btn"));
        return result;
    }

    /**
     * Show dialog and assign a data from domain object
     * @param domain Domain object
     * @param title Window title
     * @param target target
     */
    public void show(Ujo domain, String title, AjaxRequestTarget target) {
        fields.setDomain(domain);
        getModalWindow().setTitle(title);
        getModalWindow().show(target);
        target.add(form);
    }

    /** Returns modal WIndow */
    public ModalWindow getModalWindow() {
        return modalWindow;
    }
}
