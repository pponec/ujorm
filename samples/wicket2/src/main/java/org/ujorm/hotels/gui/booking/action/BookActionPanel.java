package org.ujorm.hotels.gui.booking.action;
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
import java.time.LocalDate;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.wicket.UjoEvent;
import static org.ujorm.wicket.CommonActions.*;

/**
 * The common action panel
 * @author Pavel Ponec
 */
public class BookActionPanel<U extends Booking> extends GenericPanel<U> {

        /** Default value is the same like the field */
    public static final String BOOKING = "BOOKING";

    /** Current row */
    private final U row;


    public BookActionPanel(String id, final U row) {
        super(id);
        this.row = row;
        add(createLink(DELETE, true));
        add(new ExternalLink("externalLink", row.getHotel().getHomePage()));
    }

    /** Create action */
    protected final AjaxLink createLink(String action, final boolean admin) {
        AjaxLink link = new AjaxLink(action) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                send(getPage(), Broadcast.BREADTH, new UjoEvent(getId(), row, target));
            }

            /** Removing is allowed in the future more than three days. */
            @Override
            public boolean isEnabled() {
                return row.getDateFrom().isAfter(LocalDate.now());
            }
        };

        link.setOutputMarkupPlaceholderTag(true);
        return link;
    }
}
