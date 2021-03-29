package net.ponec.x2j.service;

import net.ponec.x2j.model.Message;
import org.ujorm.tools.xml.builder.XmlPrinter;
import org.ujorm.tools.xml.config.XmlConfig;

import javax.annotation.Nonnull;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;

/**
 *
 * @author Pavel Ponec
 */
@Service
public class ConverterService {

    /** Max text length */
    private static final int MAX_LENGTH = 1_100;

    /** Create a CSS */
    @Nonnull
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
        return Message.of(xml);
    }
}
