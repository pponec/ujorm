/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. èerven 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.field;

import java.util.Date;
import org.ujoframework.extensions.ValueAgent;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class FieldUjoImplChild extends FieldUjoImpl {
    
    private Long    p5;
    private Integer p6;
    private String  p7;
    private Date    p8;
    private Float   p9;

    /** (Long) */
    public static final FieldProperty<FieldUjoImplChild,Long> PRO_P5 = newProperty("P5", Long.class, new ValueAgent<FieldUjoImplChild,Long>() {
        public final void writeValue(final FieldUjoImplChild ujo, final Long value) { ujo.p5 = value; }
        public final Long readValue (final FieldUjoImplChild ujo) { return ujo.p5;  }
    });
    
    /** (Integer) */
    public static final FieldProperty<FieldUjoImplChild,Integer> PRO_P6 = newProperty("P6", Integer.class, new ValueAgent<FieldUjoImplChild,Integer>() {
        public final void writeValue   (final FieldUjoImplChild ujo, final Integer value) { ujo.p6 = value; }
        public final Integer readValue (final FieldUjoImplChild ujo) { return ujo.p6;  }
    });
    
    /** (String) */
    public static final FieldProperty<FieldUjoImplChild,String> PRO_P7 = newProperty("P7", String.class, new ValueAgent<FieldUjoImplChild,String>() {
        public final void writeValue (final FieldUjoImplChild ujo, final String value) { ujo.p7 = value; }
        public final String readValue(final FieldUjoImplChild ujo) { return ujo.p7;  }
    });
    
    /** (Date) */
    public static final FieldProperty<FieldUjoImplChild,Date> PRO_P8 = newProperty("P8", Date.class, new ValueAgent<FieldUjoImplChild,Date>() {
        public final void writeValue(final FieldUjoImplChild ujo, final Date value) { ujo.p8 = value; }
        public final Date readValue (final FieldUjoImplChild ujo) { return ujo.p8;  }
    });
    
    /** (Float) */
    public static final FieldProperty<FieldUjoImplChild,Float> PRO_P9 = newProperty("P9", Float.class, new ValueAgent<FieldUjoImplChild,Float>() {
        public final void writeValue(final FieldUjoImplChild ujo, final Float value) { ujo.p9 = value; }
        public final Float readValue(final FieldUjoImplChild ujo)  { return ujo.p9;  }
    });

        
    
    /** Creates a new instance of UnifiedDataObjectImlp */
    public FieldUjoImplChild() {
    }
    

    
}
