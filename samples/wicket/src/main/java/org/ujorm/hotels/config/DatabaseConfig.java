/*
 *  Copyright 2013-2013 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.hotels.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.ujorm.hotels.config.demoData.DataLoader;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmHandlerProvider;
import org.ujorm.orm.ao.CheckReport;
import org.ujorm.orm.metaModel.MetaParams;

/** Build and configure database meta-model */
@Service
public final class DatabaseConfig implements OrmHandlerProvider {

    /** Database meta-model */
    final OrmHandler handler = new OrmHandler();
    
    @Autowired
    private ApplicationContext applicationContext;

    /** Initializa handler */
    public void init() {
        // There are prefered default properties for a production environment:
        final boolean yesIWantToChangeDefaultParameters = true;
        if (yesIWantToChangeDefaultParameters) {
            final MetaParams params = new MetaParams();
            params.set(MetaParams.SEQUENCE_SCHEMA_SYMBOL, true);
            params.set(MetaParams.LOG_METAMODEL_INFO, !!! true);
            params.set(MetaParams.LOG_SQL_MULTI_INSERT, false);
            params.set(MetaParams.CHECK_KEYWORDS, CheckReport.QUOTE_SQL_NAMES);
            params.set(MetaParams.INITIALIZATION_BATCH, DataLoader.class);
            params.set(MetaParams.APPL_CONTEXT, applicationContext);
            params.setQuotedSqlNames(false); // It is a default value
            handler.config(params);
        }
        // External Configuration:
        final boolean yesIWantToLoadExternalConfig = false;
        if (yesIWantToLoadExternalConfig) {
            java.net.URL config = getClass().getResource("DatabaseMappingFull.xml");
            handler.config(config, true);
        }

        handler.loadDatabase(DatabaseMapping.class);
    }

    /** {@inheritDoc } */
    @Override
    public OrmHandler getOrmHandler() {
        return handler;
    }

}
