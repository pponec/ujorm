/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujorm.gxt.client;

import com.extjs.gxt.ui.client.store.ListStore;
import java.util.List;

/**
 * Client Ujo Property for enum types.
 * The object is not serializable.
 * @author Pavel Ponec
 */
public class CPropertyEnum<UJO extends Cujo, VALUE extends CEnum> extends CProperty<UJO, VALUE> {

    private String enumKey;

    public CPropertyEnum(String name, Class type, VALUE defaultValue, int index, String enumKey) {
        super(name, type, defaultValue, index);
        this.enumKey = enumKey;
    }

    public String getEnumKey() {
        return enumKey;
    }

    public List<CEnum> getItems() {
        return ClientClassConfig.getEnumItems(enumKey);
    }

    public boolean isValidValue(String value) {
        boolean result = ClientClassConfig.isValidEnum(enumKey, value);
        return result;
    }

    public void setValueValid(UJO ujo, String aValue) {
        if (!isValidValue(aValue)) {
            throw new RuntimeException("Invalid value '" + aValue + "' for enum key: " + enumKey);
        }
        @SuppressWarnings("unchecked")
        VALUE value = (VALUE) ClientClassConfig.getEnumItem(enumKey, aValue.toString());
        super.setValue(ujo, value);
    }

    /** Returns an Enum items */
    public List<CEnum> getEnumItems() {
        return ClientClassConfig.getEnumItems(enumKey);
    }

    /** Returns an Enum items */
    public ListStore<CEnum> getItemStore() {
        ListStore<CEnum> result = new ListStore<CEnum>();
        result.add(getEnumItems());
        return result;
    }

//    public void setValue(UJO ujo, VALUE value) {
//        ujo.set(this, value);
//    }





}
