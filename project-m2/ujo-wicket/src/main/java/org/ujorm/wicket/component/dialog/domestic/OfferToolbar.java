package org.ujorm.wicket.component.dialog.domestic;
/*
 * Copyright 2015, Pavel Ponec
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
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.TextField;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.orm.OrmUjo;
import org.ujorm.wicket.CommonActions;
import org.ujorm.wicket.component.toolbar.AbstractToolbar;
import static org.ujorm.core.UjoManager.*;

/**
 * The common action panel
 * @author Pavel Ponec
 */
public final class OfferToolbar<U extends Ujo & Serializable> extends AbstractToolbar<U> {

    /** Event action */
    public static final String FILTER_ACTION = CommonActions.FILTER;

    /** Finding field */
    private final KeyList<U> fields;

    /** Search data */
    private final TextField searching;

    public OfferToolbar(String id, KeyList<U> fields) {
        super(id);
        this.fields = fields;

        add(searching = createSearchFiled("searching", fields.getFirstKey().getType()));

        buildCriterion();
    }

    /** Is the type of value the String class */
    protected boolean isStringType() {
        return fields.getFirstKey().isTypeOf(String.class);
    }

    /** Build a Criteron for a Ujorm QUERY.
     * @see #getCriterion()
     */
    @Override
    protected void buildCriterion() {
        Criterion<U> result = null;
        Key key = fields.getFirstKey();

        if (isFilled(searching.getValue())) {
            final Object value = searching.getModelObject();

            if (isStringType()) {
                final boolean orm = OrmUjo.class.isAssignableFrom(key.getType());
                result = key.where(orm ? Operator.STARTS : Operator.STARTS_CASE_INSENSITIVE, value);
            } else {
                result = key.where(Operator.EQ, value);
            }
        }

        getCriterion().setObject(result);
    }

    /** Default action name is {@link CommonActions#FILTER} */
    @Override
    public String getDefaultActionName() {
        return FILTER_ACTION;
    }

    /** Set a focus to the Serarch Toolbar by default */
    @Override
    public void requestFocus(@Nonnull final AjaxRequestTarget target) {
        searching.setModelValue(null);
        target.focusComponent(searching);
    }
}
