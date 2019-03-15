/*
 * Copyright 2013-2019, Pavel Ponec
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

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.wicket.component.dialog.domestic.EntityDialogPane;
import org.ujorm.wicket.component.form.FieldProvider;
import org.ujorm.wicket.component.tools.LocalizedModel;

/** Hotel Editor
 * @author Pavel Ponec
 * @param <U> Hotel
 */
public class HotelEditor<U extends Hotel> extends EntityDialogPane<U> {
    private static final long serialVersionUID = 0L;

    public HotelEditor(ModalWindow modalWindow, IModel<U> model) {
        super(modalWindow, model);

        // Create form fields:
        FieldProvider<U> fieldBuilder = getFieldBuilder();
        fieldBuilder.add(Hotel.NAME);
        fieldBuilder.add(Hotel.CITY);
        fieldBuilder.add(Hotel.STREET);
        fieldBuilder.add(Hotel.PHONE);
        fieldBuilder.add(Hotel.STARS);
        fieldBuilder.add(Hotel.PRICE);
        fieldBuilder.add(Hotel.CURRENCY);
        fieldBuilder.add(Hotel.HOME_PAGE);
        fieldBuilder.add(Hotel.NOTE);
        fieldBuilder.add(Hotel.ACTIVE);
    }

    /** Create the editor dialog */
    public static HotelEditor create(String componentId, int width, int height) {
        IModel<Hotel> model = Model.of(new Hotel());
        final ModalWindow modalWindow = new ModalWindow(componentId, model);
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);

        final HotelEditor<Hotel> result = new HotelEditor<Hotel>(modalWindow, model);
        modalWindow.setInitialWidth(width);
        modalWindow.setInitialHeight(height);
        modalWindow.setTitle(new LocalizedModel("dialog.edit.title"));
        //modalWindow.setCookieName(componentId + "-modalDialog");

        return result;
    }

}
