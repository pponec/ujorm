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
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.gui.hotel.action.ActionPanel;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.grid.KeyColumn;
import org.ujorm.wicket.component.grid.UjoDataProvider;
import static org.ujorm.wicket.component.grid.KeyColumn.*;

/**
 * Hotel Table
 * @author Pavel Ponec
 */
public class HotelTable extends Panel {

    private HotelEditor dialog;

    public HotelTable(String id) {
        super(id);

        UjoDataProvider<Hotel> dataProvider = UjoDataProvider.of(Hotel.ACTIVE.whereEq(true));
        
        dataProvider.addColumn(Hotel.NAME);
        dataProvider.addColumn(Hotel.CITY.add(City.NAME)); // An example of relations
        dataProvider.addColumn(Hotel.STREET);
        dataProvider.addColumn(Hotel.PRICE);
        dataProvider.addColumn(KeyColumn.of(Hotel.CURRENCY, SORTING_OFF));
        dataProvider.addColumn(Hotel.STARS);
        dataProvider.addColumn(Hotel.PHONE);
        dataProvider.addColumn(newActionColumn());

        dataProvider.setSort(Hotel.NAME);
        
        add(dataProvider.createDataTable("datatable", 10));
        dialog = createDialog("dialog", 700, 360);
        add(dialog.getModalWindow());
    }

    /** Nabídka akcí: */
    private AbstractColumn<Hotel, KeyRing<Hotel>> newActionColumn() {
        return new KeyColumn<Hotel, Integer>(KeyRing.of(Hotel.ID), null, null) {
            @Override
            public void populateItem(Item item, String componentId, IModel model) {
                final Hotel hotel = (Hotel) model.getObject();
                final ActionPanel panel = new ActionPanel(componentId, hotel);
                item.add(panel);
            }
        };
    }

    @Override
    public void onEvent(IEvent<?> event) {
        if (event.getPayload() instanceof UjoEvent) {
            UjoEvent ujoEvent = (UjoEvent) event.getPayload();
            if (UjoEvent.UPDATE.equals(ujoEvent.getContext())) {
                dialog.show(ujoEvent.getUjo(), "Edit Hotel", ujoEvent.getTarget());
            }
        }
    }

    /** Create the editor dialog */
    private HotelEditor createDialog(String componentId, int width, int height) {
        IModel<Hotel> model = Model.of(new Hotel());
        final ModalWindow modalWindow = new ModalWindow(componentId, model);
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);

        final HotelEditor result = new HotelEditor(modalWindow, model);
        modalWindow.setInitialWidth(width);
        modalWindow.setInitialHeight(height);
        modalWindow.setTitle(new ResourceModel("dialog.edit.title"));
        //modalWindow.setCookieName("modal-dialog");

        modalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
            @Override public void onClose(AjaxRequestTarget target) {
                result.clearInput();
            }
        });

        return result;
    }

}
