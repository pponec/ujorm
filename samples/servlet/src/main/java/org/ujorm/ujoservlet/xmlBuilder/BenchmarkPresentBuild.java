/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
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

package org.ujorm.ujoservlet.xmlBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.xml.Html;
import org.ujorm.tools.xml.XmlBuilder;
import org.ujorm.tools.xml.XmlPrinter;

/**
 * A live example of the HtmlElement inside a servlet.
 * @see https://dzone.com/articles/modern-type-safe-template-engines-part-2
 * @author Pavel Ponec
 */
@WebServlet(BenchmarkPresentBuild.URL_PATTERN)
public class BenchmarkPresentBuild extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/PresentkBuildServler";

    /** Show the first line of soufce code */
    public static final short SHOW_LINE = 53;

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {

        final XmlPrinter writer = XmlPrinter.forHtml(output);
        try (XmlBuilder html = new XmlBuilder(Html.HTML, writer)) {
            try (XmlBuilder head = html.addElement(Html.HEAD)) {
                head.addElement(Html.META, Html.A_CHARSET, "utf-8");
                head.addElement(Html.META,
                        Html.A_NAME, "viewport",
                        Html.A_CONTENT, "width=device-width, initial-scale=1.0");
                head.addElement(Html.META,
                        "http-equiv", "content-language",
                        Html.A_CONTENT, "IE=Edge");
                head.addElement(Html.TITLE)
                        .addText("JFall 2013 Presentations - htmlApi");
                head.addElement(Html.LINK,
                         Html.A_REL, "Stylesheet",
                         Html.A_HREF, "/webjars/bootstrap/3.3.7-1/css/bootstrap.min.css",
                         Html.A_MEDIA, "screen");
            }

            try (XmlBuilder container = html.addElement(Html.BODY, Html.A_CLASS, "container")) {
                try (XmlBuilder pageHeader = container.addElement(Html.DIV, Html.A_CLASS, "page-header")) {
                    pageHeader.addElement(Html.H1).addText("JFall 2013 Presentations - htmlApi");

                    Collection<Presentation> presentations = dummyItems();
                    for (Presentation presentation : presentations) {
                        XmlBuilder panelDefault = container.addElement(Html.DIV, Html.A_CLASS, "panel panel-default");
                        try (XmlBuilder panelHeading = panelDefault.addElement(Html.DIV, Html.A_CLASS, "panel-heading")) {
                            panelHeading.addElement(Html.H3, Html.A_CLASS, "panel-title")
                                    .addText(presentation.getTitle())
                                    .addText(" - ")
                                    .addText(presentation.getSpeakerName());
                            container.addElement(Html.DIV, Html.A_CLASS, "panel-body")
                                    .addRawText(presentation.getSummary());
                        }
                    }
                }

                container.addElement(Html.SCRIPT, Html.A_SRC, "/webjars/jquery/3.1.1/jquery.min.js").addText("");
                container.addElement(Html.SCRIPT, Html.A_SRC, "/webjars/bootstrap/3.3.7-1/js/bootstrap.min.js").addText("");
            }
        }
    }

    private static class Presentation {

        private Long id;
        private String title;
        private String speakerName;
        private String summary;
        private String room;
        private Date startTime;
        private Date endTime;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(final String title) {
            this.title = title;
        }

        public String getSpeakerName() {
            return speakerName;
        }

        public void setSpeakerName(final String speakerName) {
            this.speakerName = speakerName;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(final String summary) {
            this.summary = summary;
        }

        public String getRoom() {
            return room;
        }

        public void setRoom(final String room) {
            this.room = room;
        }

        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(final Date startTime) {
            this.startTime = startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(final Date endTime) {
            this.endTime = endTime;
        }
    }

    public static Collection<Presentation> dummyItems() {
        AtomicLong counter = new AtomicLong();
        Map<Long, Presentation> presentations = new HashMap<>();
        Presentation preso1 = new Presentation();
        preso1.setId(counter.incrementAndGet());
        preso1.setTitle("Shootout! Template engines on the JVM");
        preso1.setSpeakerName("Jeroen Reijn");
        preso1.setSummary("Are you still using JavaServer Pages as your main template language? With the popularity of template engines for other languages like Ruby and Scala and the shift in doing more MVC in the browser there are quite some new and interesting new template languages available for the JVM. During this session we will take a look at the less known, but quite interesting new template engines and see how they compare with the industries standards. \r<br/>");

        Presentation preso2 = new Presentation();
        preso2.setId(counter.incrementAndGet());
        preso2.setTitle("HoneySpider Network: a Java based system to hunt down malicious websites");
        preso2.setSpeakerName("Niels van Eijck");
        preso2.setSummary("Legitimate websites such as news sites happen to get compromised by attackers injecting malicious content. The aim of these so-called &#8220;watering hole attacks&#8221; is to infect as many visitors of a website as possible, and are sometimes even targeted at a specific group of individuals. It is increasingly important to detect these infections at an early stage.\r<br/>\r<br/>HoneySpider Network to the rescue! \r<br/>\r<br/>It is a Java based open source framework that automatically scans website urls, analyses the results and reports on any malware detected.\r<br/>Attend this talk to gain a better understanding of malware detection and client honeypots and get an overview of the HoneySpider Network&#8217;s architecture, its code and its plugins it uses. A live demo is also included!");

        Presentation preso3 = new Presentation();
        preso3.setId(counter.incrementAndGet());
        preso3.setTitle("Building scalable network applications with Netty");
        preso3.setSpeakerName("Jaap ter Woerds");
        preso3.setSummary("Since the introduction of the Java NIO API&apos;s with Java 4, developers have\r<br/>access to modern operating system facilities to perform asynchronous IO.\r<br/>Using these facilities it is possible to write networking application that that\r<br/>serve thousands of connected clients efficiently. Unfortunately, the NIO API&apos;s\r<br/>are quite low level and require a fair share of boilerplate to get started.\r<br/>In this presentation, I will introduce the Netty framework and how its\r<br/>architecture helps you as a developer stay focused on the interesting parts\r<br/>of your network application. At the end of the presentation I will give some\r<br/>real world examples and show how we use Netty in the architecture of our\r<br/>mobile messaging platform XMS.");

        Presentation preso4 = new Presentation();
        preso4.setId(counter.incrementAndGet());
        preso4.setTitle("Opening");
        preso4.setSpeakerName("Bert Ertman");
        preso4.setSummary("De openingssessie van de conferentie met aandacht voor de dag zelf en nieuws vanuit de NLJUG. De sessie wordt gepresenteerd door Bert Ertman.");

        Presentation preso5 = new Presentation();
        preso5.setId(counter.incrementAndGet());
        preso5.setTitle("Keynote door ING");
        preso5.setSpeakerName("Amir Arroni");
        preso5.setSummary("Keynote van ING, gepresenteerd door Amir Arooni en Peter Jacobs.");

        Presentation preso6 = new Presentation();
        preso6.setId(counter.incrementAndGet());
        preso6.setTitle("Keynote door Oracle");
        preso6.setSpeakerName("Sharat Chander");
        preso6.setSummary("Keynote van Oracle, gepresenteerd door Sharat Chander.");

        Presentation preso7 = new Presentation();
        preso7.setId(counter.incrementAndGet());
        preso7.setTitle("Reactieve applicaties ? klaar voor te toekomst");
        preso7.setSpeakerName("Allard Buijze");
        preso7.setSummary("De technische eisen aan webapplicaties veranderen in hoog tempo. Enkele jaren geleden nog gebruikten de grootere applicaties enkele tientallen servers en werden response tijden van een seconde en onderhoudsvensters van enkele uren nog geaccepteerd. Tegenwoordig moeten applicaties 100% beschikbaar zijn, terwijl de gebruiker in enkele milliseconden antwoord wil krijgen. Om pieken in gebruik op te kunnen vangen moeten de applicaties op duizenden processoren in een cloud omgeving kunnen draaien.\r<br/>De tekortkomingen van de huidige standaard architectuurprincipes kunnen worden opgevangen door een zogenaamde &#8220;reactive architecture&#8221;. Reactieve applicaties bezitten een aantal eigenschappen waardoor ze beter kunnen omgaan met opschalen, bestand zijn tegen fouten en bovendien efficienter gebruik maken van beschikbare server-bronnen.\r<br/>In deze presentatie laat Allard zien hoe deze eigenschappen gerealiseerd kunnen worden en welke reeds bekende architectuurpatronen en frameworks hieraan een bijdrage leveren.");

        Presentation preso8 = new Presentation();
        preso8.setId(counter.incrementAndGet());
        preso8.setTitle("HTML 5 Geolocation + WebSockets + Scalable JavaEE Backend === Awesome Realtime Location Aware Applications");
        preso8.setSpeakerName("Shekhar Gulati");
        preso8.setSummary("Location Aware apps are everywhere and we use them heavily in our day to day life. You have seen the stuff that Foursquare has done with spatial and you want some of that hotness for your app. But, where to start? In this session, we will build a location aware app using HTML 5 on the client and scalable JavaEE + MongoDB on the server side. HTML 5 GeoLocation API help us to find user current location and MongoDB offers Geospatial indexing support which provides an easy way to get started and enables a variety of location-based applications - ranging from field resource management to social check-ins. Next we will add realtime capabilities to our application using Pusher. Pusher provides scalable WebSockets as a service. The Java EE 6 backend will be built using couple of Java EE 6 technologies -- JAXRS and CDI. Finally , we will deploy our Java EE application on OpenShift -- Red Hat&apos;s public, scalable Platform as a Service.");

        Presentation preso9 = new Presentation();
        preso9.setId(counter.incrementAndGet());
        preso9.setTitle("Retro Gaming with Lambdas");
        preso9.setSpeakerName("Stephen Chin");
        preso9.setSummary("Lambda expressions are coming in Java 8 and dramatically change the programming model.  They allow new functional programming patterns that were not possible before, increasing the expressiveness and power of the Java language.\r<br/>\r<br/>In this university session, you will learn how to take advantage of the new lambda-enabled Java 8 APIs by building out a retro video game in JavaFX.\r<br/>\r<br/>Some of the Java 8 features you will learn about include enhanced collections, functional interfaces, simplified event handlers, and the new stream API.  Start using these in your application today leveraging the latest OpenJDK builds so you can prepare for the future Java 8 release.");

        Presentation preso10 = new Presentation();
        preso10.setId(counter.incrementAndGet());
        preso10.setTitle("Data Science with R for Java Developers");
        preso10.setSpeakerName("Sander Mak");
        preso10.setSummary("Understanding data is increasingly important to create cutting-edge applications. A whole new data science field is emerging, with the open source R language as a leading technology. This statistical programming language is specifically designed for analyzing and understanding data. \r<br/>\r<br/>In this session we approach R from the perspective of Java developers. How do you get up to speed quickly, what are the pitfalls to look out for?  Also we discuss how to bridge the divide between the R language and the JVM. After this session you can use your new skills to explore an exciting world of data analytics and machine learning! ");

        presentations.put(preso1.getId(),preso1);
        presentations.put(preso2.getId(),preso2);
        presentations.put(preso3.getId(),preso3);
        presentations.put(preso4.getId(),preso4);
        presentations.put(preso5.getId(),preso5);
        presentations.put(preso6.getId(),preso6);
        presentations.put(preso7.getId(),preso7);
        presentations.put(preso8.getId(),preso8);
        presentations.put(preso9.getId(),preso9);
        presentations.put(preso10.getId(),preso10);

        return presentations.values();
    }

    @Override
    protected void doPost(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        doGet(input, output);
    }
}
