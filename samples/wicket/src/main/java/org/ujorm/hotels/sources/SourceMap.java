/*
 * Copyright 2019-2019, Pavel Ponec
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
package org.ujorm.hotels.sources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.wicket.markup.html.panel.Panel;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.entity.ParamKey;
import org.ujorm.hotels.entity.ParamValue;
import org.ujorm.hotels.gui.booking.BookingEditor;
import org.ujorm.hotels.gui.booking.BookingTable;
import org.ujorm.hotels.gui.customer.CustomerEditor;
import org.ujorm.hotels.gui.customer.CustomerTable;
import org.ujorm.hotels.gui.hotel.HotelEditor;
import org.ujorm.hotels.gui.hotel.HotelTable;
import org.ujorm.hotels.gui.params.ParamsEditor;
import org.ujorm.hotels.gui.params.ParamsTable;


/** Mapping the sources */
public class SourceMap implements Serializable {

    /** Servlet mapping */
    final Map<Class,Class[]> classMap = new HashMap<>();

    /** Map servlet to dependecies */
    SourceMap() {
        classMap.put(HotelTable.class, array(HotelEditor.class, Hotel.class));
        classMap.put(BookingTable.class, array(BookingEditor.class, Booking.class));
        classMap.put(CustomerTable.class, array(CustomerEditor.class, Customer.class));
        classMap.put(ParamsTable.class, array(ParamsEditor.class, ParamValue.class, ParamKey.class));
    }

    /** Build an array */
    Class[] array(final Class ... item) {
        return item;
    }

    /** Get panel dependencies */
    private Optional<Class[]> getDependences(final Class<? extends Panel> servletClass) {
        return Optional.ofNullable(classMap.get(servletClass));
    }

    /** Return an array of related classes */
    public List<Class> getClasses(final Class<? extends Panel> panelClass) {
        final ArrayList result = new ArrayList(8);
        result.add(panelClass);
        getDependences(panelClass).ifPresent(items -> result.addAll(Arrays.asList(items)));
        return result;
    }
}
