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
     * Highlights the original text according to the regular expression
     * using HTML element {@code <span>}.
     *
     * @param text An original text
     * @param regexp An regular expression
     * @return Raw HTML text.
     */
    public Message highlight(String text, String regexp) {
        try {
            if (text.length() > MAX_LENGTH) {
                String msg = String.format("Shorten text to a maximum of %s characters.",
                        MAX_LENGTH);
                throw new IllegalArgumentException(msg);
            }
            SecureRandom random = new SecureRandom();
            String begTag = "_" + random.nextLong();
            String endTag = "_" + random.nextLong();
            String rawText = text.replaceAll(
                    "(" + regexp + ")",
                    begTag + "$1" + endTag);
            XmlPrinter printer = new XmlPrinter(new StringBuilder(), XmlConfig.ofDoctype(""));
            printer.write(rawText, false);
            return Message.of(printer.toString()
                    .replaceAll(begTag, "<span>")
                    .replaceAll(endTag, "</span>")
            );
        } catch (Exception | OutOfMemoryError e) {
            return Message.of(e);
        }
    }
}
