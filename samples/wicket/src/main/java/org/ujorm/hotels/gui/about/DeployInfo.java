/*
 * Copyright 2016, Pavel Ponec
 */
package org.ujorm.hotels.gui.about;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class DeployInfo extends Label {
    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployInfo.class);

    /** New line */
    private static final char NEW_LINE = '\n';
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final Date STARTUP = new Date();

    public DeployInfo(String id) {
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
        final SimpleDateFormat format = new SimpleDateFormat(FORMAT);
        final StringBuilder result = new StringBuilder();

        result.append(NEW_LINE).append("<!-- Deploy info:");
        result.append(NEW_LINE).append("Startup Date: ").append(format.format(STARTUP));
        result.append(NEW_LINE).append("Build Time..: ").append(getBuildDate());
        result.append(NEW_LINE).append("-->");
        return result.toString();
    }

    /** Returns a build date from manifest */
    protected String getBuildDate() {
        final ServletContext application = WebApplication.get().getServletContext();
        InputStream is = null;
        try {
            is = application.getResourceAsStream("/" + JarFile.MANIFEST_NAME);
            Attributes mainAttribs = new Manifest(is).getMainAttributes();
            return mainAttribs.getValue("Build-Time");
        } catch (Exception e) {
            LOGGER.warn("Illegal file {}", JarFile.MANIFEST_NAME, e);
            return "-";
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
