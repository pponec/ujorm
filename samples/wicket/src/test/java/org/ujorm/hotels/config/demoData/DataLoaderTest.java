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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;
import org.ujorm.hotels.entity.City;

/**
 * Class to load hotes.
 * @author Pavel Ponec
 */
public class DataLoaderTest {

    public DataLoaderTest() {
    }

    /** Test method to dowload data from source: http://api.hotelsbase.org/ */
    //@Test
    public void testDownloadData() throws Exception {
        System.out.println("testDownloadData");

        for (City city : getCities()) {
            URL dataUrl = createDataUrl(city);
            StreamSource source = new StreamSource(dataUrl.openStream());
            StreamSource xsl = new StreamSource(getClass().getResourceAsStream("hotels-trans.xsl"));
            makeXslTransformation(source, xsl, new String[]{"CITY_ID", city.get(City.ID).toString()});
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

    /** Builda data URL: */
    public URL createDataUrl(City city) throws MalformedURLException {
        String result = String.format
                ( "http://api.hotelsbase.org/search.php?latitude=%s&longitude=%s&distanceMax=%s"
                , city.get(City.LATITUDE)
                , city.get(City.LONGITUDE)
                , 10 // [km]
                );
       return new URL(result);
    }

    /** Get all cities from a local CSV file */
    public List<City> getCities() throws Exception {
        return new DataLoader().getCities();
    }

    /** Make a XSL transformation. */
    public static File makeXslTransformation(StreamSource source, StreamSource xsl, String[] ... params)
    throws TransformerConfigurationException, TransformerException, IOException {
        // Create transformer factory
        TransformerFactory factory = TransformerFactory.newInstance();

        // Use the factory to create a template containing the xsl file
        Templates template = factory.newTemplates(xsl);

        // Use the template to create a transformer
        Transformer xformer = template.newTransformer();
        for (String[] p : params) {
            xformer.setParameter(p[0], p[1]);
        }

        //CharArrayWriter writer = new CharArrayWriter(128);
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
