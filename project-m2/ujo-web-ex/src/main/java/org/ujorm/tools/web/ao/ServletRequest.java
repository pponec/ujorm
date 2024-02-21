package org.ujorm.tools.web.ao;

import java.util.HashMap;
import java.util.Map;

public class ServletRequest extends HashMap<String, String> {

    public ServletRequest(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ServletRequest(int initialCapacity) {
        super(initialCapacity);
    }

    public ServletRequest() {
    }

    public ServletRequest(Map<? extends String, ? extends String> m) {
        super(m);
    }

    public String[] getParameterValues(String string) {
        return super.values().toArray(new String[0]);
    }
}
