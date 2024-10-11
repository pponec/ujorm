package org.ujorm.tools.web.ao;

import java.util.HashMap;
import java.util.Map;

public class UServletRequest extends HashMap<String, String> {

    public UServletRequest(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public UServletRequest(int initialCapacity) {
        super(initialCapacity);
    }

    public UServletRequest() {
    }

    public UServletRequest(Map<? extends String, ? extends String> m) {
        super(m);
    }

    public String[] getParameterValues(final String key) {
        final String result = get(key);
        return result != null ? new String[] { result } : new String[0];
    }

    public void setCharacterEncoding(String charset) {
    }

    public void setParameter(String name, String value) {
        super.put(name, value);
    }
}
