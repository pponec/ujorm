package net.ponec.x2j.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.json.JsonBuilder;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.web.bind.annotation.RequestParam;
import net.ponec.x2j.controller.ao.ConverterService;
import net.ponec.x2j.controller.ao.Message;
import static net.ponec.x2j.controller.Converter.Constants.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.ujorm.tools.web.ajax.JavaScriptWriter;

@RestController
public class Converter {
    /** A service */
    private final ConverterService service = new ConverterService();

    @RequestMapping(path = {"/converter"}, method = {GET, POST}, produces = MediaType.TEXT_HTML_VALUE)
    public void regexp(
            @RequestParam(defaultValue = "") String text,
            @RequestParam(defaultValue = "") String regexp,
            @RequestParam(defaultValue = "false", name = "_ajax") boolean ajax,
            HttpServletResponse response) throws IOException {
        if (ajax) {
            ajax(text, regexp, response);
            return;
        }
        try (HtmlElement html = HtmlElement.of(getConfig("Convert XML file to Java code on-line"), response)) {
            html.addCssLink(BOOTSTRAP_CSS);
            html.addCssBodies(html.getConfig().getNewLine(), service.getCss());
            writeJavascript(html);
            try (Element body = html.getBody()) {
                body.addHeading(html.getTitle());
                try (Element form = body.addForm()
                        .setId(FORM_ID)
                        .setMethod(Html.V_POST).setAction("?")) {
                    form.addInput(CONTROL_CSS)
                            .setId(REGEXP)
                            .setName(REGEXP)
                            .setValue(regexp)
                            .setAttribute(Html.A_PLACEHOLDER, "Regular expression");
                    form.addTextArea(CONTROL_CSS)
                            .setId(TEXT)
                            .setName(TEXT)
                            .setAttribute(Html.A_PLACEHOLDER, "Plain Text")
                            .addText(text);
                    form.addDiv().addButton("btn", "btn-primary").addText("Evaluate");
                    form.addDiv(CONTROL_CSS, OUTPUT_CSS).addRawText(service.highlight(text, regexp));
                }
            }
        }
    }

    private void ajax(String text, String regexp, HttpServletResponse response)
            throws IOException {
        try (JsonBuilder builder = JsonBuilder.of(getConfig(""), response)) {
            final Message msg = service.highlight(text, regexp);
            builder.writeClass(OUTPUT_CSS, e ->
                    e.addElementIf(msg.isError(), Html.SPAN, "error")
                    .addRawText(msg));
        }
    }

    private void writeJavascript(HtmlElement html) {
        new JavaScriptWriter(
                "#" + REGEXP,
                "#" + TEXT)
                .setSubtitleSelector("." + SUBTITLE_CSS)
                .setFormSelector("#" + FORM_ID)
                .write(html.getHead());
    }

    /** Create a configuration of HTML model */
    private DefaultHtmlConfig getConfig(@Nonnull String title) {
        DefaultHtmlConfig config;
        config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        return config;
    }

    /** CSS constants */
    static class Constants {
        /** Bootstrap form control CSS class name */
        static final String CONTROL_CSS = "form-control";
        /** CSS class name for the output box */
        static final String OUTPUT_CSS = "out";
        /** CSS class name for the output box */
        static final String SUBTITLE_CSS = "subtitle";
        /** Form identifier */
        static final String FORM_ID = "form";
        /** Link to a Bootstrap URL of CDN */
        static final String BOOTSTRAP_CSS = "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";
        /** Link to jQuery of CDN */
        static final String JQUERY_JS = "https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js";
        /** Regula expression parameter */
        static final HttpParameter REGEXP = HttpParameter.of("regexp");
        /** Text value parameter */
        static final HttpParameter TEXT = HttpParameter.of("text");
    }
}
