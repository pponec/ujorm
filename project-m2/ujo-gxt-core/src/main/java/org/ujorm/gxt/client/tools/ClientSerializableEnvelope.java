/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2012 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujorm.gxt.client.tools;

import java.io.Serializable;

/**
 * ClientSerializableEnvelope (workaround for the GXT serialization)
 * @author Ponec
 */
public class ClientSerializableEnvelope implements Serializable {

    private ColorGxt colorGxt;

    public ColorGxt getColorGxt() {
        return colorGxt;
    }

    public void setColorGxt(ColorGxt colorGxt) {
        this.colorGxt = colorGxt;
    }


}
