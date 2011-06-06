package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.widget.form.Field;
import java.util.List;
import org.ujorm.gxt.client.Cujo;

public class MultiField<CUJO extends Cujo> extends Field<CUJO> {

    private List<CUJO> values;

    public List<CUJO> getValues() {
        return values;
    }

    public void setValues(List<? extends CUJO> values) {
        this.values = (List<CUJO>) values;
        this.setValue(values.size() > 0 ? values.get(0) : null);
    }
}
