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

    public String[] getParameterValues(String string) {
        return super.values().toArray(new String[0]);
    }

    public void setCharacterEncoding(String charset) {
    }

    public void setParameter(String name, String value) {
        super.put(name, value);
    }
}
