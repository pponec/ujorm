package net.ponec.x2j.service;

import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;

/**
 *
 * @author Pavel Ponec
 */
@Service
public class XmlParserService {

    public Document parse(URL url) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(url);
        return document;
    }

    public Document parse(String xml) throws DocumentException {
        Reader resource = new StringReader(xml);
        SAXReader reader = new SAXReader();
        Document document = reader.read(resource);
        return document;
    }

}
