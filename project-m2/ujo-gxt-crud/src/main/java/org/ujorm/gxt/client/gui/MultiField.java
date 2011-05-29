/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.widget.form.Field;
import org.ujorm.gxt.client.Cujo;
import java.util.List;

/**
 * A logical field with a multi-value setters / getters.
 * @author Ponec
 */
public class MultiField extends Field<Cujo> {

    private List<Cujo> values;

    public List<Cujo> getValues() {
        return values;
    }

    @SuppressWarnings("unchecked")
    public void setValues(List<? extends Cujo> values) {
        this.values = (List<Cujo>) values;
        this.setValue(values.size()>0 ? values.get(0) : null);
    }

}
