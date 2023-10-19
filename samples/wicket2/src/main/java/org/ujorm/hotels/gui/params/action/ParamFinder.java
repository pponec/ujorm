package org.ujorm.hotels.gui.params.action;
/*
 * Copyright 2014-2018, Pavel Ponec
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
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.hotels.entity.ParamValue;
import org.ujorm.hotels.entity.enums.ModuleEnum;
import org.ujorm.wicket.CommonActions;
import org.ujorm.wicket.component.toolbar.AbstractToolbar;
import static org.ujorm.tools.Check.hasLength;

/**
 * The finder component
 * @author Pavel Ponec
 */
public final class ParamFinder<U extends ParamValue> extends AbstractToolbar<U> {
    /** Event action name */
    public static final String FILTER_ACTION = "FILTER_PARAM_ACTION";

    /** Default criterion */
    public static final Criterion<ParamValue> defaultCriterion = ParamValue.ID.forAll();

    /** Search parameter */
    private final DropDownChoice<ModuleEnum> searchModule;
    /** Search parameter */
    private final TextField searchParam;

    public ParamFinder(String id) {
        super(id);

        final Form form = new Form("form");
        this.add(form);
        form.add(searchModule = createSearchChoice("searchModule", ModuleEnum.APPLICATION, false));
        form.add(searchParam = createSearchField("searchParam"));

        buildCriterion();
    }

    /** Build a Criterion for a Ujorm QUERY.
     * @see #getCriterion()
     */
    @Override
    protected void buildCriterion() {
        final ModuleEnum module = searchModule.getModelObject();
        final String value = searchParam.getValue();
        final Criterion<ParamValue> crn1, crn2, crn3, crn4;

        crn1 = defaultCriterion;
        crn2 = module != null
             ? ParamValue.KEY_MODULE$.whereEq(module)
             : ParamValue.KEY_MODULE$.forAll();
        crn3 = hasLength(value)
             ? ParamValue.KEY_NAME$.where(Operator.CONTAINS_CASE_INSENSITIVE, value)
             : ParamValue.KEY_NAME$.forAll();
        crn4 = crn1.and(crn2).and(crn3);

        getCriterion().setObject(crn4.cast());
    }

    /** Default action name is {@link CommonActions#FILTER} */
    @Override
    public String getDefaultActionName() {
        return FILTER_ACTION;
    }
}
