/*
 * Copyright 2019-2019 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/XmlModel.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm.tools.web;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedHashMap;
import org.junit.*;
import org.ujorm.tools.web.ao.Column;
import org.ujorm.tools.web.ao.MockServletResponse;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.ujorm.tools.web.ao.Injector;

/**
 * @author Pavel Ponec
 */
public class ElementTest {

    /** Link to a Bootstrap URL */
    private static final String BOOTSTRAP_CSS = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";

    /** Link to a bootstra URL */
    private static final String SEMANTIC_CSS = "https://semantic-ui.com/dist/semantic.min.css";

    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testAddSelect_3args() {
        System.out.println("addSelect");
        DefaultHtmlConfig config = HtmlConfig.ofDefault();
        HtmlElement resInstance = createHtmlPage(config);

        String result = resInstance.toString();
        String expectedResult = "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "<meta charset=\"UTF-8\"/>\n"
                + "<title>Demo</title></head>\n"
                + "<body>\n"
                + "<select name=\"selectName\">\n"
                + "<option value=\"1\">one</option>\n"
                + "<option value=\"2\" selected=\"selected\">two</option>\n"
                + "<option value=\"3\">three</option>"
                + "</select>"
                + "</body>"
                + "</html>";
        assertEquals(expectedResult, result);

        // --- DOM model ---

        config.setDocumentObjectModel(true);
        HtmlElement domInstance = createHtmlPage(config);
        result = domInstance.toString();
        assertEquals(expectedResult, result);
    }

    /** Create HTML page */
    private HtmlElement createHtmlPage(HtmlConfig config) throws IllegalStateException {
        HtmlElement result = HtmlElement.of(config);
        try (HtmlElement instance = result) {
            Element body = instance.getBody();

            Object value = 2;
            LinkedHashMap options = new LinkedHashMap<>();
            options.put(1, "one");
            options.put(value, "two");
            options.put(3, "three");
            body.addSelect().setName("selectName")
                    .addSelectOptions(value, options, "mySelect");
        }
        return result;
    }


    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testAddElementIf() {
        System.out.println("addElementIf");
        DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setDocumentObjectModel(false);
        config.setHtmlHeader(true);

        HtmlElement resInstance = HtmlElement.of(config);
        try (HtmlElement instance = resInstance) {
            instance.getBody()
                    .addElementIf(false, Html.DIV)
                    .addText("text");
        }
        String result = resInstance.toString();
        String expectedResult = String.join("\n",
                "<!DOCTYPE html>",
                "<html lang=\"en\">",
                "<head>",
                "<meta charset=\"UTF-8\"/>",
                "<title>Demo</title></head>",
                "<body>text</body></html>");
        assertEquals(expectedResult, result);
    }
    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testAddElementIf_dom() {
        System.out.println("addElementIf");
        DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setDocumentObjectModel(true);
        config.setHtmlHeader(true);

        HtmlElement resInstance = HtmlElement.of(config);
        try (HtmlElement instance = resInstance) {
            instance.getBody()
                    .addElementIf(false, Html.DIV)
                    .addText("text");
        }
        String result = resInstance.toString();
        String expectedResult = String.join("\n",
                "<!DOCTYPE html>",
                "<html lang=\"en\">",
                "<head>",
                "<meta charset=\"UTF-8\"/>",
                "<title>Demo</title></head>",
                "<body>text</body></html>");
        assertEquals(expectedResult, result);
    }

    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testAddTestTemplated() {
        System.out.println("addTestTemplated");
        CharSequence[] cssClasses = null;
        HtmlElement resInstance = HtmlElement.of(null);
        try (HtmlElement instance = resInstance) {
            instance.getBody().addTextTemplated("Test <{}.{}{}", 1, 2, ">");
        }

        String result = resInstance.toString();
        String expectedResult = String.join("\n",
                "<!DOCTYPE html>",
                "<html lang=\"en\">",
                "<head>",
                "<meta charset=\"UTF-8\"/>",
                "<title>Demo</title></head>",
                "<body>Test &lt;1.2&gt;</body></html>");
        assertEquals(expectedResult, result);
    }

    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testAddFieldset() {
        System.out.println("addFieldset");
        CharSequence[] cssClasses = null;
        HtmlElement resInstance = HtmlElement.of(null);
        try (HtmlElement instance = resInstance) {
            Element body = instance.getBody();
            body.addFieldset("MyTitle", "myCssClass").addText("Lorem ipsum ...");
        }

        String result = resInstance.toString();
        String expectedResult = "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "<meta charset=\"UTF-8\"/>\n"
                + "<title>Demo</title></head>\n"
                + "<body>\n"
                + "<fieldset class=\"myCssClass\">\n"
                + "<legend>MyTitle</legend>"
                + "Lorem ipsum ..."
                + "</fieldset>"
                + "</body>"
                + "</html>";
        assertEquals(expectedResult, result);
    }

    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testRootElement() {
        StringBuilder writer = new StringBuilder(256);
        DefaultHtmlConfig config = HtmlConfig.ofElementName(Html.DIV);
        config.setNewLine("");

        try (HtmlElement html = HtmlElement.of(config, writer)) {
            html.addElement(Html.SPAN).addText("test");
        }
        String expected = "<div><span>test</span></div>";
        assertEquals(expected, writer.toString());
    }

    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testAddTable() {
        System.out.println("addTable");
        MockServletResponse response = new MockServletResponse();

        CharSequence[] cssClasses = {"table"};
        CharSequence[] titles = {"Id", "Name", "Enabled"};
        try (HtmlElement html = HtmlElement.of(response, BOOTSTRAP_CSS)) {
            html.addBody().addHeading("Cars");
            html.addBody().addTable(getCars().stream(), cssClasses, titles,
                    Car::getId,
                    Car::getName,
                    Car::getEnabled);
        }
        assertTrue(response.toString().contains("<td>Scala</td>"));
    }

    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testAddTableExtended() {
        System.out.println("addTableExtended");
        MockServletResponse response = new MockServletResponse();

        CharSequence[] cssClasses = {"table"};
        CharSequence[] titles = {"Id", "Name", "Enabled", 
                    (Injector) td -> td.addSpan("red").addText("Home page")};
        try (HtmlElement html = HtmlElement.of(response, BOOTSTRAP_CSS)) {
            html.addBody().addHeading("Cars");
            html.addBody().addTable(getCars().stream(), cssClasses, titles,
                    Car::getId,
                    Car::getName,
                    Car::getEnabled,
                    (Column<Car>) (td, car) -> td.addLinkedText(car.getHomePage(), "link")
            );
        }   
        assertTrue(response.toString().contains("<td>Scala</td>"));
        assertTrue(response.toString().contains("<th>\n<span class=\"red\">Home page</span></th>"));
        assertTrue(response.toString().contains("<td>\n<a href=\"http://demo.car.org"));
    }

    private Collection<Car> getCars() {
        Collection result = new ArrayDeque<>();
        result.add(new Car(1, "Scala", true));
        result.add(new Car(2, "Auris", true));
        result.add(new Car(3, "Ford Escort", true));
        result.add(new Car(4, "Hyundai i10", true));

        return result;
    }

    private class Car {

        private final Integer id;
        private final String name;
        private final Boolean enabled;
        private final String homePage;

        public Car(Integer id, String name, Boolean enabled) {
            this.id = id;
            this.name = name;
            this.enabled = enabled;
            this.homePage = "http://demo.car.org/" + name.replace(' ', '-');
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public String getHomePage() {
            return homePage;
        }
    }
}
