/*
 *  Copyright 2014-2022 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.core;

import java.util.HashMap;
import java.util.Map;
import org.ujorm.tools.Assert;

/**
 * XML header
 * @author Pavel Ponec
 */
public class XmlHeader {

    /** The root element name, the default name is {@code "body"} */
    private final String rootElement;
    /** XML header, the default value is {@code UjoManagerXML#XML_HEADER} */
    private String header;
    /** XML comment */
    private String comment = "Generated by the module 'ujo-xsd' " + UjoManager.version();
    /** The root tag attributes */
    private Map attributes;

    public XmlHeader() {
        this("body");
    }

    public XmlHeader(String rootElement) {
        this.rootElement = Assert.notNull(rootElement, "rootElement is required");
    }

    /**
     * The root tag name
     * @return the rootTag
     */
    public String getRootElement() {
        return rootElement;
    }

    /**
     * XML header
     * @return the header
     */
    public String getHeader() {
        if (header == null) {
            header = UjoManagerXML.XML_HEADER;
        }
        return header;
    }

    /**
     * XML header
     * @param header the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * XML comment
     * @return the description
     */
    public String getComment() {
        return comment;
    }

    /**
     * XML comment
     * @param comment the description to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * The root tag attributes
     * @return the not null result
     */
    public Map getAttributes() {
        if (attributes == null) {
            attributes = new HashMap();
        }
        return attributes;
    }
}
