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
package org.ujorm.hotels.gui.params;

import java.util.List;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.hotels.entity.ParamKey;
import org.ujorm.hotels.entity.ParamValue;
import org.ujorm.hotels.gui.params.action.ParamFinder;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.hotels.service.ParamService;
import org.ujorm.hotels.service.param.ApplicationRoles;
import org.ujorm.hotels.sources.SrcLinkPanel;
import org.ujorm.wicket.CommonActions;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.grid.CommonAction;
import org.ujorm.wicket.component.grid.ListDataProvider;
import static org.ujorm.wicket.component.grid.AbstractDataProvider.DEFAULT_DATATABLE_ID;

/** ParamValue Panel
 * @author Pavel Ponec
 */
public class ParamsTable<U extends ParamValue> extends GenericPanel<U> {

    @SpringBean private AuthService authService;
    @SpringBean(name=ParamService.CACHED)
    private ParamService paramService;
    private final ListDataProvider<U> columnBuilder;
    private final ParamFinder<U> toolbar = new ParamFinder("paramFinder");
    private final ParamsEditor editDialog;

    /** Use the service to calling a PostConstruct method */
    @SpringBean
    protected ApplicationRoles appRoles;

    public ParamsTable(String id) {
        super(id);

        columnBuilder = ListDataProvider.of(toolbar.getCriterion(), ParamValue.KEY_MODULE$);
        columnBuilder.add(ParamValue.KEY_MODULE$);
        columnBuilder.add(ParamValue.KEY_NAME$);
        columnBuilder.add(ParamValue.KEY_SYSTEM$);
        columnBuilder.add(ParamValue.PARAM_KEY.add(ParamKey.CLASS_NAME));
        columnBuilder.add(ParamValue.TEXT_VALUE);
        columnBuilder.add(ParamValue.PARAM_KEY.add(ParamKey.LAST_UPDATE));
        columnBuilder.add(ParamValue.ID, getActionUpdate(CommonActions.UPDATE));
        columnBuilder.setRows(getDbRows());
        add(columnBuilder.createDataTable(20));

        // Dialogs:
        add((editDialog = ParamsEditor.create("editDialog", 700, 390)).getModalWindow());
        add(toolbar);
        add(new SrcLinkPanel("sourceLink", this));
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        final UjoEvent<U> event = UjoEvent.get(argEvent);
        switch (event.getAction()) {
            case ParamFinder.FILTER_ACTION:
                reloadTable(event, false);
                break;
            case CommonActions.LOGIN_CHANGED:
                reloadTable(event, true);
                break;
            case CommonActions.UPDATE:
                if (event.showDialog()) {
                    editDialog.show(event, Model.of("Edit parameter"));
                } else {
                    paramService.updateValue(event.getDomain());
                    reloadTable(event, true);
                }
                break;
        }
    }

    /** Get user parameters include system params */
    private List<? super U> getDbRows() {
        return paramService.getValues(ParamValue.ID.forAll());
    }

    /** Reload the data table */
    private void reloadTable(UjoEvent event, boolean dbRequest) {
        if (dbRequest) {
            columnBuilder.setRows(getDbRows());
        }
        event.addTarget(get(DEFAULT_DATATABLE_ID));
    }

    /** Action UPDATE */
    private CommonAction getActionUpdate(String action) {
        return new CommonAction<U>(action) {
            @Override public boolean isVisible(U row) {
                return row.isPersonalParam()
                     ? authService.isLogged()
                     : authService.isAdmin();
            }
        };
    }
}
