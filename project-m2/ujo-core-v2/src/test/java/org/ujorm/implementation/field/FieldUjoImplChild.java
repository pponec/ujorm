/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.field;

import java.util.Date;
import org.ujorm.extensions.ValueAgent;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class FieldUjoImplChild extends FieldUjoImpl {

    private Long    p5;
    private Integer p6;
    private String  p7;
    private Date    p8;
    private Float   p9;

    /** (Long) */
    public static final FieldProperty<FieldUjoImplChild,Long> PRO_P5 = newKey("P5", new ValueAgent<FieldUjoImplChild,Long>() {
        public void writeValue(final FieldUjoImplChild ujo, final Long value) { ujo.p5 = value; }
        public Long readValue (final FieldUjoImplChild ujo) { return ujo.p5;  }
    });

    /** (Integer) */
    public static final FieldProperty<FieldUjoImplChild,Integer> PRO_P6 = newKey("P6", new ValueAgent<FieldUjoImplChild,Integer>() {
        public void writeValue   (final FieldUjoImplChild ujo, final Integer value) { ujo.p6 = value; }
        public Integer readValue (final FieldUjoImplChild ujo) { return ujo.p6;  }
    });

    /** (String) */
    public static final FieldProperty<FieldUjoImplChild,String> PRO_P7 = newKey("P7", new ValueAgent<FieldUjoImplChild,String>() {
        public void writeValue (final FieldUjoImplChild ujo, final String value) { ujo.p7 = value; }
        public String readValue(final FieldUjoImplChild ujo) { return ujo.p7;  }
    });

    /** (Date) */
    public static final FieldProperty<FieldUjoImplChild,Date> PRO_P8 = newKey("P8", new ValueAgent<FieldUjoImplChild,Date>() {
        public void writeValue(final FieldUjoImplChild ujo, final Date value) { ujo.p8 = value; }
        public Date readValue (final FieldUjoImplChild ujo) { return ujo.p8;  }
    });

    /** (Float) */
    public static final FieldProperty<FieldUjoImplChild,Float> PRO_P9 = newKey("P9", new ValueAgent<FieldUjoImplChild,Float>() {
        public void writeValue(final FieldUjoImplChild ujo, final Float value) { ujo.p9 = value; }
        public Float readValue(final FieldUjoImplChild ujo)  { return ujo.p9;  }
    });

    static {
        init(FieldUjoImplChild.class);
    }

    /** Creates a new instance of UnifiedDataObjectImlp */
    public FieldUjoImplChild() {
    }



}
