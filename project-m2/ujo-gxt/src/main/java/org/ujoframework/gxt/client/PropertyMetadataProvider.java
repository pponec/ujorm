/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujoframework.gxt.client;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Property Metadata Provider
 * @author Ponec
 */
public class PropertyMetadataProvider implements Serializable {

    private HashMap<CujoProperty, PropertyMetadata> binding;

    public PropertyMetadataProvider(HashMap<CujoProperty, PropertyMetadata> binding) {
        this.binding = binding;
    }

    public PropertyMetadata get(CujoProperty property) {
        PropertyMetadata result = binding.get(property);
        return result;
    }

    /** Get property Metadata, newer NULL value. */
    public PropertyMetadata getAlways(CujoProperty p) {
        PropertyMetadata result = get(p);
        if (result == null) {
            result = new PropertyMetadata(p);
        }
        return result;
    }



}
