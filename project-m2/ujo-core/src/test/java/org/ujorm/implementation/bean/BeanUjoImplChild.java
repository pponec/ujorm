/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.bean;
import java.util.Date;

/**
 * A BEAN Object
 * @author Pavel Ponec
 */
public class BeanUjoImplChild extends BeanUjoImpl {

    /** (Long) */
    public static final BeanProperty<BeanUjoImplChild,Long>    PRO_P5 = newKey("P5");
    /** (Integer) */
    public static final BeanProperty<BeanUjoImplChild,Integer> PRO_P6 = newKey("P6");
    /** (String) */
    public static final BeanProperty<BeanUjoImplChild,String>  PRO_P7 = newKey("P7");
    /** (Date) */
    public static final BeanProperty<BeanUjoImplChild,Date>    PRO_P8 = newKey("P8");
    /** (Float) */
    public static final BeanProperty<BeanUjoImplChild,Float>   PRO_P9 = newKey("P9");

    /** Verify unique constants */
    static{
        init(BeanUjoImplChild.class,true);
    }
    
    // ------- STANDARD BEAN --------------------
    
    
    /** (Date) */
    protected Long p5;
    /** (Integer) */
    protected Integer p6;
    /** (String) */
    protected String p7;
    /** (Date) */
    protected Date p8;
    /** (Float) */
    protected Float p9;
    
    /** Creates a new instance of UnifiedDataObjectImlp */
    public BeanUjoImplChild() {
    }

    public Long getP5() {
        return p5;
    }
    
    public void setP5(Long p5) {
        this.p5 = p5;
    }

    public Integer getP6() {
        return p6;
    }

    public void setP6(Integer p6) {
        this.p6 = p6;
    }

    public String getP7() {
        return p7;
    }

    public void setP7(String p7) {
        this.p7 = p7;
    }

    public Date getP8() {
        return p8;
    }

    public void setP8(Date p8) {
        this.p8 = p8;
    }

    public Float getP9() {
        return p9;
    }

    public void setP9(Float p9) {
        this.p9 = p9;
    }

}
