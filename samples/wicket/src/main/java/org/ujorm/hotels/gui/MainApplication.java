/*
 * Copyright 2013-2014, Pavel Ponec
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
package org.ujorm.hotels.gui;

import javax.inject.Inject;
import javax.inject.Named;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.stereotype.Component;
import org.ujorm.hotels.config.SpringContext;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmHandlerProvider;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 *
 * @see com.mycompany.Start#main(String[])
 */
@Component("wicketApplicationSpringBean")
public class MainApplication extends WebApplication implements OrmHandlerProvider {

    /** The application name */
    public static final String APPLICATION_NAME = "Demo Hotels";

    /** OrmHandler Provider */
    @Inject @Named(SpringContext.ORM_HANDLER)
    private OrmHandlerProvider ormProvider;

    @Override
    protected void init() {
        super.init();
        // getMarkupSettings().setStripWicketTags(true); // jQuery UI recommendation
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));
        mountPage("/home", HomePage.class);
    }

    /** {@inheritDoc { */
    @Override
    public Class<? extends WebPage> getHomePage() {
        return HomePage.class;
    }

    /** Returns ORM handler */
    @Override
    public OrmHandler getOrmHandler() {
         return ormProvider.getOrmHandler();
    }

}
