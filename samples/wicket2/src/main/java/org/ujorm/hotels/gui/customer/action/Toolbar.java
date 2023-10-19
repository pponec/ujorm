package org.ujorm.hotels.gui.customer.action;

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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.wicket.CommonActions;
import org.ujorm.wicket.component.toolbar.AbstractToolbar;
import static org.ujorm.tools.Check.hasLength;

/**
 * The common action panel
 * @author Pavel Ponec
 */
public final class Toolbar<U extends Customer> extends AbstractToolbar<U> {
    /** Event action */
    public static final String FILTER_ACTION = CommonActions.FILTER;

    /** Default criterion */
    public static final Criterion<Customer> DEFAULT_CRITERION = Customer.ID.forAll();

    @SpringBean private AuthService authService;

    /** search login */
    private final TextField searchLogin;

    public Toolbar(String id) {
        super(id);

        final Form form = new Form("form");
        this.add(form);
        form.add(searchLogin = createSearchField("searchLogin"));

        buildCriterion();
    }

    /** Build a Criterion for a Ujorm QUERY.
     * @see #getCriterion()
     */
    @Override
    protected void buildCriterion() {

        Criterion<Customer> result = (authService.isAdmin()
            ? DEFAULT_CRITERION
            : Customer.ACTIVE.whereEq(true)).cast();

        String value = searchLogin.getValue();
        if (hasLength(value)) {
            result = result.and(Customer.LOGIN.where(Operator.STARTS, value));
        }

        getCriterion().setObject(result.cast());
    }

    /** Default action name is {@link CommonActions#FILTER} */
    @Override
    public String getDefaultActionName() {
        return FILTER_ACTION;
    }
}
