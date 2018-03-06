package org.ujorm.wicket.component.label;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.ujorm.wicket.UjoModel;
import org.ujorm.wicket.function.UjoSupplier;

/**
 * Extended Wicket Label supporting the UjoSupplier interface.
 * @author Pavel Ponec
 */
public class UjoLabel extends Label {

    public <V extends Serializable> UjoLabel(@Nonnull final String id, @Nonnull final UjoSupplier<V> label) {
        super(id, new UjoModel(label));
    }

    public UjoLabel(@Nonnull final String id, @Nonnull final Serializable label) {
        super(id, label);
    }

    public UjoLabel(@Nonnull final String id, @Nonnull final IModel<?> model) {
        super(id, model);
    }

}
