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

import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.core.KeyRing;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.gui.hotel.action.ActionPanel;
import org.ujorm.hotels.services.DbService;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.dialog.DialogContent;
import org.ujorm.wicket.component.grid.KeyColumn;
import org.ujorm.wicket.component.grid.UjoDataProvider;
import static org.ujorm.hotels.gui.hotel.action.Actions.*;
import static org.ujorm.wicket.component.grid.KeyColumn.*;

/**
 * Hotel Table
 * @author Pavel Ponec
 */
public class HotelTable extends Panel {

    @SpringBean(name="dbService") DbService dbService;
    private HotelEditor editDialog;
    private DialogContent removeDialog;

    public HotelTable(String id) {
        super(id);

        UjoDataProvider<Hotel> columns = UjoDataProvider.of(Hotel.ACTIVE.whereEq(true));

        columns.addColumn(Hotel.NAME);
        columns.addColumn(Hotel.CITY.add(City.NAME)); // An example of relations
        columns.addColumn(Hotel.STREET);
        columns.addColumn(Hotel.PRICE);
        columns.addColumn(KeyColumn.of(Hotel.CURRENCY, SORTING_OFF));
        columns.addColumn(Hotel.STARS);
        columns.addColumn(Hotel.PHONE);
        columns.addColumn(newActionColumn());
        columns.setSort(Hotel.NAME);

        add(columns.createDataTable(10));
        add((editDialog = createEditDialog("editDialog", 700, 390)).getModalWindow());
        add((removeDialog = createMessageDialog("removeDialog", 290, 160)).getModalWindow());
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
            final UjoEvent<Hotel> ujoEvent = (UjoEvent<Hotel>) event.getPayload();

            if (ujoEvent.isAction(SHOW_UPDATE)) {
                editDialog.setAction(MAKE_UPDATE);
                editDialog.show
                        ( new ResourceModel("dialog.edit.title")
                        , ujoEvent.getUjoModel()
                        , ujoEvent.getTarget());
            }
            else if (ujoEvent.isAction(SHOW_DELETE)) {
                removeDialog.setAction(MAKE_DELETE);
                removeDialog.setMessage(new Model("Do you want to remove selected Hotel really?"));
                removeDialog.show
                        ( new ResourceModel("dialog.delete.title")
                        , ujoEvent.getUjoModel()
                        , "delete"
                        , ujoEvent.getTarget());
            }
            else if (ujoEvent.isAction(MAKE_DELETE)) {
                dbService.deleteHotel(ujoEvent.getContent());
                ujoEvent.getTarget().add(get(UjoDataProvider.DEFAULT_DATATABLE_ID));
            }
            else if (ujoEvent.isAction(MAKE_UPDATE)) {
                dbService.updateHotel(ujoEvent.getContent());
                ujoEvent.getTarget().add(get(UjoDataProvider.DEFAULT_DATATABLE_ID));
            }
        }
    }

    /** Create the editor dialog */
    private HotelEditor createEditDialog(String componentId, int width, int height) {
        IModel<Hotel> model = Model.of(new Hotel());
        final ModalWindow modalWindow = new ModalWindow(componentId, model);
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);

        final HotelEditor result = new HotelEditor(modalWindow, model);
        modalWindow.setInitialWidth(width);
        modalWindow.setInitialHeight(height);
        modalWindow.setTitle(new ResourceModel("dialog.edit.title"));
        //modalWindow.setCookieName("modal-dialog");

        return result;
    }

    /** Create the editor dialog */
    private DialogContent createMessageDialog(String componentId, int width, int height) {
        IModel<String> model = Model.of("");
        final ModalWindow modalWindow = new ModalWindow(componentId, model);
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);

        final DialogContent result = new DialogContent(modalWindow, model);
        modalWindow.setInitialWidth(width);
        modalWindow.setInitialHeight(height);
        //modalWindow.setCookieName("modal-dialog");

        return result;
    }

}
