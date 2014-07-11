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
package org.ujorm.hotels.gui.params;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.ParamKey;
import org.ujorm.hotels.entity.ParamValue;
import org.ujorm.hotels.services.AuthService;
import org.ujorm.hotels.services.DbService;
import org.ujorm.wicket.component.grid.UjoDataProvider;

/**
 * ParamValue Panel
 * @author Pavel Ponec
 */
public class ParamsTable<U extends ParamValue> extends GenericPanel<U> {

    @SpringBean private DbService dbService;
    @SpringBean private AuthService authService;

    private ParamsEditor editDialog;

    public ParamsTable(String id) {
        super(id);

        UjoDataProvider<U> columns = UjoDataProvider.of(getCriterion());
        columns.add(ParamValue.KEY_MODULE$);
        columns.add(ParamValue.KEY_NAME$);
        columns.add(ParamValue.KEY_SYSTEM$);
        columns.add(ParamValue.PARAM_KEY.add(ParamKey.CLASS_NAME));
        columns.add(ParamValue.TEXT_VALUE);
        columns.add(ParamValue.PARAM_KEY.add(ParamKey.LAST_UPDATE));
        //columns.add(newActionColumn(ParamValue.ID));
        add(columns.createDataTable(20));

        // Dialogs:
        add((editDialog = ParamsEditor.create("editDialog", 700, 390)).getModalWindow());
    }


    /** Create a criterion for the table */
    @SuppressWarnings("unchecked")
    private IModel<Criterion<U>> getCriterion() {
        final Criterion<U> result;
        if (authService.isLogged()) {
            result = ParamValue.CUSTOMER.whereEq(authService.isAdmin()
                   ? DbService.SYSTEM_ACCOUNT
                   : authService.getLoggedCustomer()).cast();
        } else {
            result = ParamValue.ID.forAll().cast();
        }
        return Model.of(result);
    }

}
