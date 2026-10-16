package org.ujorm.orm.converter;

/** Customer state stored by a code */
public enum CustomerState implements CodeConverter.Coded {
    ACTIVE("A"),
    INACTIVE("I");

    private final String code;

    CustomerState(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    /** Converter of the state */
    public static class Converter extends CodeConverter<CustomerState> {
        public Converter() {
            super(CustomerState.class);
        }
    }
}
