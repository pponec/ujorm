/*
 * Copyright 2016-2021, Pavel Ponec
 */
package org.ujorm.hotels.gui.about;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import java.util.jar.Attributes;
import javax.servlet.ServletContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MeasuringCode
 * @author Pavel Ponec
 */
public class BuildInfo extends Label {
    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(BuildInfo.class);

    /** New line */
    private static final char NEW_LINE = '\n';
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final LocalDateTime STARTUP = LocalDateTime.now();

    public BuildInfo(String id) {
        super(id);
        setRenderBodyOnly(true);
        setEscapeModelStrings(false);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        setDefaultModel(Model.of(getInfo()));
    }

    /** Get text */
    protected String getInfo() {
        final DateTimeFormatter format = DateTimeFormatter.ofPattern(FORMAT);
        final StringBuilder result = new StringBuilder()
              .append(NEW_LINE).append("<!-- Build:")
              .append(NEW_LINE).append("Startup Date: ").append(format.format(STARTUP))
              .append(NEW_LINE).append("Build Time..: ").append(getBuildDate())
              .append(NEW_LINE).append("-->");
        return result.toString();
    }

    /** Returns a build date from manifest */
    protected String getBuildDate() {
        final ServletContext application = WebApplication.get().getServletContext();
        try (InputStream is = application.getResourceAsStream("/" + JarFile.MANIFEST_NAME)){
            Attributes mainAttribs = new Manifest(is).getMainAttributes();
            return mainAttribs.getValue("Build-Time");
        } catch (Exception e) {
            LOGGER.debug("Illegal file {}", JarFile.MANIFEST_NAME, e);
            return "-";
        }
    }
}
