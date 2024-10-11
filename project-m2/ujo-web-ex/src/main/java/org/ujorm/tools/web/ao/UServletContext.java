package org.ujorm.tools.web.ao;

public final class UServletContext {

    private final UServletRequest servletRequest;
    private final UServletResponse servletResponse;

    public UServletContext(UServletRequest servletRequest, UServletResponse servletResponse) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }

    public UServletRequest request() {
        return servletRequest;
    }

    public UServletResponse response() {
        return servletResponse;
    }
}
