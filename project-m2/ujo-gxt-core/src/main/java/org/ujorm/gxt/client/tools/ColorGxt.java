/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2013 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.tools;

import com.extjs.gxt.ui.client.widget.ColorPalette;
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

    /** Alpha 50% with the WHITE color */
    public String getWhiteAlpha50() {
        int r = Integer.parseInt(color.substring(0,2), 16);
        int g = Integer.parseInt(color.substring(2,4), 16);
        int b = Integer.parseInt(color.substring(4,6), 16);

        // Alpha 50% with the WHITE color
        int col = 0, max = 256;
        col = col * max + ((r + max) >> 1);
        col = col * max + ((g + max) >> 1);
        col = col * max + ((b + max) >> 1);

        String result = Integer.toHexString(col & 0xffffff | 0x1000000).substring(1).toUpperCase();
        return result;
    }

    /** Alpha 50% with the BLACK color */
    public String getBlackAlpha50() {
        int r = Integer.parseInt(color.substring(0,2), 16);
        int g = Integer.parseInt(color.substring(2,4), 16);
        int b = Integer.parseInt(color.substring(4,6), 16);

        // Alpha 50% with the BLACK color
        int col = 0, max = 256;
        col = col * max + (r >> 1);
        col = col * max + (g >> 1);
        col = col * max + (b >> 1);

        String result = Integer.toHexString(col & 0xffffff | 0x1000000).substring(1).toUpperCase();
        return result;
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

    /** Returns random color */
    public static String getRandomColor() {
        String[] colors = new ColorPalette().getColors();
        int i = com.google.gwt.user.client.Random.nextInt(colors.length);
        return colors[i];
    }
}
