package org.ujorm.tools.web.context;

public final class UContext {

    private final URequest servletRequest;
    private final Appendable servletResponse;

    public UContext(URequest servletRequest, Appendable servletResponse) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }

    public UContext(URequest servletRequest) {
        this(servletRequest, new StringBuilder());
    }

    public UContext() {
        this(new URequest(), new StringBuilder());
    }

    public URequest request() {
        return servletRequest;
    }

    public Appendable response() {
        return servletResponse;
    }
}
