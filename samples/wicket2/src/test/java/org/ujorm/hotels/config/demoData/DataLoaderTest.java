/*
 * Copyright 2013 Pavel Ponec
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
package org.ujorm.hotels.config.demoData;

import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import jakarta.xml.transform.Result;
import jakarta.xml.transform.Templates;
import jakarta.xml.transform.Transformer;
import jakarta.xml.transform.TransformerConfigurationException;
import jakarta.xml.transform.TransformerException;
import jakarta.xml.transform.TransformerFactory;
import jakarta.xml.transform.stream.StreamResult;
import jakarta.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujorm.hotels.entity.City;
import org.ujorm.tools.msg.MsgFormatter;

/**
 * Class to load hotes.<br>
 * Join all CSV files using a linux statement:
 * <pre class=pre>
 * {@code head -q -n 20 *.csv}
 * </pre>
 *
 * @author Pavel Ponec
 */
public class DataLoaderTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(DataLoaderTest.class);

    /** Distance of locations from city center [km] */
    protected static final Integer DISTANCE_FROM_CENTER = 10;

    /** Test method to download data from source: http://api.hotelsbase.org/ */
    //@Test
    public void testDownloadData() throws Exception {
        System.out.println("testDownloadData");

        for (City city : getCities()) {
            URL dataUrl = createDataUrl(city);
            LOGGER.info("{}({}): {}",city.getName(), city.getId(), dataUrl.toString());
            StreamSource xsl = new StreamSource(getClass().getResourceAsStream("hotels-trans.xsl"));
            makeXslTransformation(openStreamExt(dataUrl), xsl, new String[]{"CITY_ID", city.get(City.ID).toString()});
            Thread.sleep(1000);
        }
    }

    /** Test of run method, of class DataLoader. */
    //@Test
    public void testConversion() throws Exception {
        System.out.println("testConversion");

        City city = new LinkedList<City>(getCities()).getLast();
        InputStream dataUrl = getClass().getResourceAsStream("hotels-raw.xml");
        StreamSource source = new StreamSource(dataUrl);
        StreamSource xsl = new StreamSource(getClass().getResourceAsStream("hotels-trans.xsl"));
        String[] cityArg = {"CITY_ID", city.get(City.ID).toString()};
        makeXslTransformation(source, xsl, cityArg, cityArg);
    }

    /** Load data from URL by an example: <br>
     * http://api.hotelsbase.org/search.php?longitude=14.421138&latitude=50.087533
     */
    public URL createDataUrl(City city) throws MalformedURLException {
        String result = MsgFormatter.format
                ( "http://api.hotelsbase.org/search.php?latitude={}&longitude={}&distanceMax={}"
                , city.get(City.LATITUDE)
                , city.get(City.LONGITUDE)
                , DISTANCE_FROM_CENTER // [km]
                );
       return new URL(result);
    }

    /** Basic method to open resource */
    protected StreamSource openStreamBase(URL dataUrl) throws IOException {
        return new StreamSource(dataUrl.openStream());
    }

    /** Extended method to open resource to fix some errors of data source */
    protected StreamSource openStreamExt(URL dataUrl) throws IOException {
        Reader reader = new InputStreamReader(dataUrl.openStream(), StandardCharsets.UTF_8);
        StringBuilder writer = new StringBuilder(10000);
        int c;
        while((c=reader.read())!=-1) {
            writer.append(c);
        }
        reader.close();

        String body = writer.toString();
        writer = null;
        body = body.replaceFirst("iso-8859-1", "utf-8");
        body = body.replaceAll("& ", "&amp; ");
        reader = new CharArrayReader(body.toCharArray());
        return new StreamSource(reader);
    }

    /** Get all cities from a local CSV file */
    public List<City> getCities() throws Exception {
        return new DataLoader().getCities();
    }

    /** Make a XSL transformation. */
    public static File makeXslTransformation(StreamSource source, StreamSource xsl, String[] ... params)
    throws TransformerException, IOException {
        // Create transformer factory
        TransformerFactory factory = TransformerFactory.newInstance();

        // Use the factory to create a template containing the xsl file
        Templates template = factory.newTemplates(xsl);

        // Use the template to create a transformer
        Transformer xformer = template.newTransformer();
        for (String[] p : params) {
            xformer.setParameter(p[0], p[1]);
        }

        //StringBuilder writer = new StringBuilder(128);
        File file = new File(System.getProperty("user.home"), "HotelData" + params[0][1] + ".csv");
        Result result = new StreamResult(file);

        // Apply the xsl file to the source file and write the result to the output file
        xformer.transform(source, result);

        return file;
    }


    /** Test of run method, of class DataLoader. */
    @Test
    public void testDummy() {
    }

}
