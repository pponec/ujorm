/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujorm.gxt.client;


import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Enumeration items
 * @author Pavel Ponec
 */
public class EnumItems implements Serializable {

    /** Map an enum key (short class name) to CEnum items.  */
    private Map<String, List<CEnum>> enums;

    protected EnumItems() {
    }

    public EnumItems(Map<String, List<CEnum>> enums) {
        this.enums = enums;
    }

    /*
    public static EnumItems newInstance() {
        final List<EnumItems> result = new ArrayList<EnumItems>(1);
        TableControllerAsyncImpl service = new TableControllerAsyncImpl();
        service.getEnums(new AsyncCallback<InitItems>() {

        @Override
        public void onFailure(Throwable caught) {
        throw new RuntimeException("Can't get the EnumItems", caught);
        }

        @Override
        public void onSuccess(EnumItems _result) {
        result.add(_result);
        }
        });
    return result.get(0);
    }
     */

    /** Returns list of CEnums or throw an RuntimeException */
    public List<CEnum> getItems(String enumName) throws RuntimeException {
        List<CEnum> result = enums.get(enumName);
        if (result == null) {
            throw new RuntimeException("Enum no have loaded items for the name: " + enumName);
        }
        return result;
    }

    /** Is valid enumItem text for the enumeration type? */
    public CEnum getItem(String enumType, String enumName) throws RuntimeException {

        List<CEnum> cenumList = enums.get(enumName);
        if (cenumList != null) {
            for (CEnum cEnum : cenumList) {
                if (cEnum.getName().equals(enumName)) {
                    return cEnum;
                }
            }
        }
        throw new RuntimeException("Enum '" + enumType + "' have no item name: " + enumName);
    }


    public boolean isEnum(String enumName) {
        List<CEnum> result = enums.get(enumName);
        return result!=null;
    }

    /** Is valid enumItem text for the enumeration type? */
    public boolean isValid(String enumType, String enumName) {
        List<CEnum> items = enums.get(enumType);
        if (items == null) {
            return false;
        }
        for (CEnum cEnum : items) {
            if (CEnum.name.getValue(cEnum).equals(enumName)) {
                return true;
            }
        }
        return false;
    }
}
