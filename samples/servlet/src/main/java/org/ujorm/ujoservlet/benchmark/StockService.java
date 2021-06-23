/*
 * Copyright 2021-2021 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.ujoservlet.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Pavel Ponec
 */
public enum StockService {

    INSTANCE;

    /** Immutable stocks */
    final List<Stock> stocks;

    private StockService() {
        stocks = createStock();
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    private List<Stock> createStock() {
        final List<Stock> result = new ArrayList();
        result.add(new Stock("Adobe Systems", "Adobe Systems Inc.", "http://www.adobe.com", "ADBE", 39.26, 0.13, 0.33));
        result.add(new Stock("Advanced Micro Devices", "Advanced Micro Devices Inc.", "http://www.amd.com", "AMD",
                16.22, 0.17, 1.06));
        result.add(new Stock("Amazon.com", "Amazon.com Inc", "http://www.amazon.com", "AMZN", 36.85, -0.23, -0.62));
        result.add(new Stock("Apple", "Apple Inc.", "http://www.apple.com", "AAPL", 85.38, -0.87, -1.01));
        result.add(new Stock("BEA Systems", "BEA Systems Inc.", "http://www.bea.com", "BEAS", 12.46, 0.09, 0.73));
        result.add(new Stock("CA", "CA, Inc.", "http://www.ca.com", "CA", 24.66, 0.38, 1.57));
        result.add(new Stock("Cisco Systems", "Cisco Systems Inc.", "http://www.cisco.com", "CSCO", 26.35, 0.13, 0.5));
        result.add(new Stock("Dell", "Dell Corp.", "http://www.dell.com/", "DELL", 23.73, -0.42, -1.74));
        result.add(new Stock("eBay", "eBay Inc.", "http://www.ebay.com", "EBAY", 31.65, -0.8, -2.47));
        result.add(new Stock("Google", "Google Inc.", "http://www.google.com", "GOOG", 495.84, 7.75, 1.59));
        result.add(new Stock("Hewlett-Packard", "Hewlett-Packard Co.", "http://www.hp.com", "HPQ", 41.69, -0.02, -0.05));
        result.add(new Stock("IBM", "International Business Machines Corp.", "http://www.ibm.com", "IBM", 97.45, -0.06,
                -0.06));
        result.add(new Stock("Intel", "Intel Corp.", "http://www.intel.com", "INTC", 20.53, -0.07, -0.34));
        result.add(new Stock("Juniper Networks", "Juniper Networks, Inc", "http://www.juniper.net/", "JNPR", 18.96, 0.5,
                2.71));
        result.add(new Stock("Microsoft", "Microsoft Corp", "http://www.microsoft.com", "MSFT", 30.6, 0.15, 0.49));
        result.add(new Stock("Oracle", "Oracle Corp.", "http://www.oracle.com", "ORCL", 17.15, 0.17, 1.1));
        result.add(new Stock("SAP", "SAP AG", "http://www.sap.com", "SAP", 46.2, -0.16, -0.35));
        result.add(new Stock("Seagate Technology", "Seagate Technology", "http://www.seagate.com/", "STX", 27.35, -0.36,
                -1.3));
        result.add(new Stock("Sun Microsystems", "Sun Microsystems Inc.", "http://www.sun.com", "SUNW", 6.33, -0.01,
                -0.16));
        result.add(new Stock("Yahoo", "Yahoo! Inc.", "http://www.yahoo.com", "YHOO", 28.04, -0.17, -0.6));

        return Collections.unmodifiableList(result);
    }

}
