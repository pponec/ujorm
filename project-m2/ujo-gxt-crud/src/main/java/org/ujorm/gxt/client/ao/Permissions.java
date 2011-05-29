/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.ao;

import java.util.Arrays;
import java.util.EnumSet;

/** Roles for permission to some acttion.
 * @author Pavel Ponec
 */
final public class Permissions {

    /** Permissions roles */
    final private EnumSet permissionRoles;

    /** Roles for permission to Display the display panel. */
    public Permissions(Class<Enum> type) {
        this(type.getEnumConstants());
    }

    /** Roles for permission to Display the display panel. */
    public Permissions(Enum ... panelRoles) {
        permissionRoles = panelRoles.length==0
            ? EnumSet.noneOf(DummyEnum.class)
            : EnumSet.copyOf(Arrays.asList(panelRoles))
            ;
    }

    /** Roles for permission to Display the display panel. */
    public EnumSet getRoles() {
        return permissionRoles;
    }

    /** Roles for permission to see the display panel ? */
    public boolean isAllowed(EnumSet userRoles) {
        for (Object object : permissionRoles) {
            if (userRoles.contains(object)) {
                return true;
            }
        }
        return false;
    }
}
