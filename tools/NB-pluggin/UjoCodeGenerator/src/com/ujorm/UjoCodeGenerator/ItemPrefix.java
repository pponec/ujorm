package com.ujorm.UjoCodeGenerator;

/**
 * Item prefix
 * @author Ponec
 */
public enum ItemPrefix {
    
    SET("set"),
    GET("get"),
    IS("is"),
    EMPTY("");
    
    private final String code;

    private ItemPrefix(String code) {
        this.code = code;
    }
    
    public String toString() {
        return code;
    }
    
    /** Length of code */
    public int length() {
        return code.length();
    }
    
}
