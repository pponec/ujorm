package net.ponec.x2j.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.request.ManyMap;
import org.ujorm.tools.web.request.UContext;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestParam;
import net.ponec.x2j.service.ConverterService;
import static net.ponec.x2j.controller.ConverterController.Constants.*;
import net.ponec.x2j.model.Message;
import static org.springframework.web.bind.annotation.RequestMethod.*;

//@RequiredArgsConstructor
@RestController
public class ConverterController {

    /**
     * A service
     */
    private final ConverterService service;

    public ConverterController(ConverterService service) {
        this.service = service;
    }

    @RequestMapping(path = {"/converter", ""}, method = {GET, POST}, produces = MediaType.TEXT_HTML_VALUE)
    public String converter(
            @RequestParam(defaultValue = "") String text,
            @RequestParam(defaultValue = "") String submit
    ) {

        if (DEMO.equals(submit)) {
            text = service.getDemoXml();
        }

        final Message message = service.toJavaCode(text);
        final UContext uContext = UContext.of();
        try (HtmlElement html = HtmlElement.of(uContext, getConfig("Convert XML file to Java code on-line"))) {
            html.addCssLink(CSS_STYLE);
            html.addCssBodies(html.getConfig().getNewLine(), service.getCss());
            try ( Element body = html.getBody()) {
                body.addHeading(html.getTitle());
                try ( Element form = body.addForm()
                        .setId(FORM_ID)
                        .setMethod(Html.V_POST).setAction("?")) {
                    form.addDiv().addLabel().addText("Enter a XML file:");
                    form.addTextArea(CONTROL_CSS)
                            .setId(TEXT)
                            .setName(TEXT)
                            .setAttribute(Html.A_PLACEHOLDER, "XML file")
                            .addText(text);
                    try (Element buttons = form.addDiv()) {
                        buttons.addSubmitButton("btn", "btn-primary")
                                .setName(SUBMIT)
                                .setValue("send")
                                .addText("Evaluate");
                        buttons.addSubmitButton("btn", "btn-secondary")
                                .setName(SUBMIT)
                                .setValue(DEMO)
                                .addText("Demo");
                    }
                    form.addDiv(CONTROL_CSS, OUTPUT_CSS).addText(message);
                }
            }
        }
        return uContext.response().toString();
    }

    /**
     * Create a configuration of HTML model
     */
    private DefaultHtmlConfig getConfig(@NotNull String title) {
        DefaultHtmlConfig config;
        config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        return config;
    }

    /**
     * CSS constants
     */
    static class Constants {

        /**
         * Bootstrap form control CSS class name
         */
        static final String CONTROL_CSS = "form-control";
        /**
         * CSS class name for the output box
         */
        static final String OUTPUT_CSS = "out";
        /**
         * CSS class name for the output box
         */
        static final String SUBTITLE_CSS = "subtitle";
        /**
         * Form identifier
         */
        static final String FORM_ID = "form";
        /**
         * Link to CSS including a Bootstrap URL of CDN
         */
        static final String CSS_STYLE = "/css/main.css";
        /**
         * Text value parameter
         */
        static final HttpParameter TEXT = HttpParameter.of("text");
        /**
         * Submit button name
         */
        static final String SUBMIT = "submit";
        /**
         * Demo value
         */
        static final String DEMO = "demo";
    }
}
