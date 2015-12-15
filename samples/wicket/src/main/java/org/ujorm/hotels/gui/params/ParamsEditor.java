/*
 * Copyright 2013-2015, Pavel Ponec
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

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.ujorm.core.UjoManager;
import org.ujorm.hotels.entity.ParamKey;
import org.ujorm.hotels.entity.ParamValue;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.wicket.component.dialog.domestic.EntityDialogPane;
import org.ujorm.wicket.component.form.fields.Field;
import org.ujorm.wicket.component.tools.LocalizedModel;

/**
 * Customer Editor
 * @author Pavel Ponec
 */
public class ParamsEditor<U extends ParamValue> extends EntityDialogPane<U> {
    private static final long serialVersionUID = 0L;

    @SpringBean private AuthService authService;

    public ParamsEditor(ModalWindow modalWindow, IModel<U> model) {
        super(modalWindow, model);

        // Editable fields:
        fields.add(ParamValue.PARAM_KEY.add(ParamKey.MODULE));
        fields.add(ParamValue.PARAM_KEY.add(ParamKey.NAME));
        fields.add(ParamValue.PARAM_KEY.add(ParamKey.CLASS_NAME));
        fields.add(ParamValue.TEXT_VALUE);
        fields.add(ParamValue.PARAM_KEY.add(ParamKey.TEXT_DEFAULT_VALUE));
        fields.add(ParamValue.PARAM_KEY.add(ParamKey.LAST_UPDATE));
        fields.add(ParamValue.PARAM_KEY.add(ParamKey.NOTE));

        for (Field field : fields.getFields()) {
            field.setEnabled(false);
        }
        fields.setEnabled(ParamValue.TEXT_VALUE, true);
        fields.addValidator(ParamValue.TEXT_VALUE, new IValidator<String>(){
            /** The validator implementation: */
            @Override @SuppressWarnings("unchecked")
            public void validate(IValidatable<String> validatable) {
                final Class paramClass = getModelObject().getParamKey().getParamClass();
                try {
                    UjoManager.getInstance().decodeValue(paramClass, validatable.getValue());
                } catch (Exception e) {
                    org.apache.wicket.validation.ValidationError wicketErr = new org.apache.wicket.validation.ValidationError();
                    wicketErr.setMessage("The value is not type of the " + paramClass.getSimpleName());
                    wicketErr.addKey("validator.type.message");
                    wicketErr.getVariables().put("simpleType", paramClass.getSimpleName());
                    wicketErr.getVariables().put("typeName", paramClass.getName());
                    validatable.error(wicketErr);
                }
            }
        });
    }

    /** Create the editor dialog */
    public static ParamsEditor create(String componentId, int width, int height) {
        IModel<ParamValue> model = Model.of(new ParamValue());
        final ModalWindow modalWindow = new ModalWindow(componentId, model);
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);

        final ParamsEditor<ParamValue> result = new ParamsEditor<ParamValue>(modalWindow, model);
        modalWindow.setInitialWidth(width);
        modalWindow.setInitialHeight(height);
        modalWindow.setTitle(new LocalizedModel("dialog.edit.title"));
        //modalWindow.setCookieName(componentId + "-modalDialog");

        return result;
    }
}
