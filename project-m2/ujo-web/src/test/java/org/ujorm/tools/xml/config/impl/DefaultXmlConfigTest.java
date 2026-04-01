/*
 * Copyright 2018-2026 Pavel Ponec, [https://github.com/pponec](https://github.com/pponec)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.xml.config.impl;

import org.junit.jupiter.api.Test;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.xml.AbstractWriter;
import org.ujorm.tools.xml.builder.XmlBuilder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests of the DefaultHtmlConfig class.
 *
 * @author Pavel Ponec
 */
public class DefaultXmlConfigTest {

    @Test
    public void testDefaultConstructorAndInitialValues() {
        var config = new DefaultHtmlConfig();

        assertEquals(AbstractWriter.HTML_DOCTYPE, config.getDoctype());
        assertEquals("Demo", config.getTitle());
        assertEquals(0, config.getCssLinks().length);
        assertEquals("en", config.getLanguage().orElse(null));
        assertEquals("text/html", config.getContentType());
        assertTrue(config.isHtmlHeaderRequest());
        assertNull(config.getRawHeaderText());
        assertNotNull(config.getHeaderInjector());
        assertEquals(XmlBuilder.HTML, config.getRootElementName());

        var unpairElements = config.getUnpairElements();
        assertTrue(unpairElements.contains(Html.META));
        assertTrue(unpairElements.contains(Html.LINK));
    }

    @Test
    public void testCopyConstructor() {
        var source = new DefaultHtmlConfig();
        source.setTitle("New Title")
                .setCssLinks("style1.css", "style2.css")
                .setLanguage("cs")
                .setContentType("application/xhtml+xml")
                .setHtmlHeader(false)
                .setRootElementName("div")
                .setUnpairElements(Set.of("img", "br"));

        // Implicitly tests the deprecated method as it's part of the state
        source.setRawHeaderText("<meta name='author' content='tester'>");

        var copy = new DefaultHtmlConfig(source);

        assertEquals("New Title", copy.getTitle());
        assertArrayEquals(new CharSequence[]{"style1.css", "style2.css"}, copy.getCssLinks());
        assertEquals("cs", copy.getLanguage().orElse(null));
        assertEquals("application/xhtml+xml", copy.getContentType());
        assertFalse(copy.isHtmlHeaderRequest());
        assertEquals("<meta name='author' content='tester'>", copy.getRawHeaderText());
        assertEquals("div", copy.getRootElementName());

        var unpairElements = copy.getUnpairElements();
        assertEquals(2, unpairElements.size());
        assertTrue(unpairElements.contains("img"));
    }

    @Test
    public void testSettersAndGetters() {
        var config = new DefaultHtmlConfig();

        config.setTitle("Custom Title");
        assertEquals("Custom Title", config.getTitle());

        config.setCssLinks("main.css");
        assertEquals(1, config.getCssLinks().length);
        assertEquals("main.css", config.getCssLinks()[0]);

        config.setLanguage("sk");
        assertTrue(config.getLanguage().isPresent());
        assertEquals("sk", config.getLanguage().get());

        config.setContentType("text/plain");
        assertEquals("text/plain", config.getContentType());

        config.setHtmlHeader(false);
        assertFalse(config.isHtmlHeaderRequest());

        config.setRootElementName("body");
        assertEquals("body", config.getRootElementName());

        config.setRootElementName(null);
        assertEquals(XmlBuilder.HIDDEN_NAME, config.getRootElementName());

        var customUnpairElements = Set.of("custom-tag");
        config.setUnpairElements(customUnpairElements);
        assertEquals(customUnpairElements, config.getUnpairElements());

        var result = config.setHeaderInjector(e -> e.addText("Injected"));
        assertSame(config, result);
        assertNotNull(config.getHeaderInjector());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedSetRawHeaderText() {
        var config = new DefaultHtmlConfig();
        config.setRawHeaderText("Raw Header");
        assertEquals("Raw Header", config.getRawHeaderText());
    }

    @Test
    public void testInheritedDoctypeMethod() {
        var config = new DefaultHtmlConfig();

        // Tests the default value injected via nonnull() when doctype is null
        assertEquals(AbstractWriter.HTML_DOCTYPE, config.getDoctype());

        config.setDoctype("<!DOCTYPE custom>");
        assertEquals("<!DOCTYPE custom>", config.getDoctype());
    }
}