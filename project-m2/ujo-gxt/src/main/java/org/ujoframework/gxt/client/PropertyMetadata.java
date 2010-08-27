/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujoframework.gxt.client;

import java.io.Serializable;

/**
 * Property meta-data
 * @author Pavel Ponec
 */
public class PropertyMetadata implements Serializable {

    private boolean primaryKey;
    private boolean mandatory;
    private int maxLength;
    private int precision;
    //
    private String columnLabel;
    private String sideLabel;
    private String description;

    protected PropertyMetadata() {
    }

    public PropertyMetadata
        ( boolean primaryKey
        , boolean mandatory
        , int maxLength
        , int precision
        , String columnLabel
        , String sideLabel
        , String description
        ){
        this.primaryKey = primaryKey;
        this.mandatory = mandatory;
        this.maxLength = maxLength;
        this.precision = precision;
        this.columnLabel = columnLabel;
        this.sideLabel = sideLabel;
        this.description = isValid(description) ? description : null;
    }

    /** Default constructor. */
    public PropertyMetadata(CujoProperty property) {
        this.primaryKey = false;
        this.mandatory = false;
        this.maxLength = Integer.MAX_VALUE;
        this.precision = 0;
        this.columnLabel = property.getCammelName();
        this.sideLabel = columnLabel;
        this.description = isValid(columnLabel) ? columnLabel : null;
    }

    static private boolean isValid(CharSequence text) {
        return text!=null && text.length()>0;
    }

    public String getColumnLabel() {
        return columnLabel;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getMaxLengthExt() {
        return hasMaxLength() ? maxLength : Integer.MAX_VALUE ;
    }

    public boolean hasMaxLength() {
        return maxLength>0;
    }

    public int getPrecision() {
        return precision;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public String getSideLabel() {
        return sideLabel;
    }

    @Override
    public String toString() {
        return sideLabel + ":" + mandatory;
    }




}
