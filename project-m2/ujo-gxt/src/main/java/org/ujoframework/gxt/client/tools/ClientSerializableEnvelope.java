/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.gxt.client.tools;

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
