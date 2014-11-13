package com.ujorm.UjoCodeGenerator.bo;


/**
 * Prefix enumerator of the class/field method.
 * @author Ponec
 */
public enum PrefixEnum {

    SET("set"),
    GET("get"),
    IS("is"),
    EMPTY("");

    private final String code;

    private PrefixEnum(String code) {
        this.code = code;
    }

    public String toString() {
        return code;
    }

    /** Empty length of the code */
    public boolean isEmpty() {
        return code.isEmpty();
    }

}
