package net.ponec.x2j.service;

import net.ponec.x2j.model.Message;
import org.jetbrains.annotations.NotNull;
import lombok.RequiredArgsConstructor;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.ujorm.tools.common.StringUtils;

/**
 *
 * @author Pavel Ponec
 */
@Service
@RequiredArgsConstructor
public class ConverterService {

    private final XmlParserService parserService;

    /** XML demo data */
    private static final String DEMO_FILE = "/data/sample.html";

    /** Create a CSS */
    @NotNull
    public CharSequence[] getCss() {
        return new CharSequence[] { ""
                , "body   { margin-left:20px; background-color: #f3f6f7;}"
                , "h1, h2 { color: SteelBlue;}"
                , "form   { width: 500px;}"
                , ".subtitle{ font-size: 10px; color: silver;}"
                , "textarea { height: 120px; margin: 3px 0;}"
                , ".out   { min-height: 100px; margin-top: 10px;"
                + " height: inherit; white-space: pre-wrap;}"
                , ".out span { background-color: yellow;}"
                , ".out .error { background-color: white; color: red;}"
        };
    }

    /**
     * Convert XML to Java
     *
     * @param xml An original XML content t
     * @return Raw HTML text.
     */
    public Message toJavaCode(String xml) {
        try {
            Document doc = parserService.parse(xml);
            return Message.of(doc.toString());
        } catch (DocumentException e) {
            return Message.of(e);
        }
    }

    public String getDemoXml() {
        return StringUtils.read(getClass().getResourceAsStream(DEMO_FILE));
    }

}
