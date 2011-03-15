/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.gxt.client.tools;

import java.io.Serializable;

/**
 * GxtColor class
 * @author Ponec
 */
final public class ColorGxt implements Serializable, Comparable<ColorGxt> {

    /** Color RRGGBB */
    private String color;

    /** DO NOT USE IT!
     * This constructor is only for GWT serialization
     */
    @Deprecated
    public ColorGxt() {
    }

    public ColorGxt(String color) {
        if (color==null) {
           throw new IllegalArgumentException("Undefined value");
        }
        this.color = color;
    }

    /** Get Color as RRGGBB */
    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return color;
    }

    @Override
    public int compareTo(ColorGxt o) {
        return color.compareTo(o.color);
    }

    // ============ STATIC METHODS ============

    /** RED */
    public static ColorGxt netRed() {
        return new ColorGxt("FF0000");
    }
    /** GREEN */
    public static ColorGxt getGreen() {
        return new ColorGxt("00FF00");
    }
    /** BLUE */
    public static ColorGxt getBlue() {
        return new ColorGxt("0000FF");
    }
    /** WHITE */
    public static ColorGxt getWhite() {
        return new ColorGxt("FFFFFF");
    }
    /** BLACK */
    public static ColorGxt getBlack() {
        return new ColorGxt("000000");
    }

}
