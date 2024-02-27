package org.ujorm.tools.web.ao;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class ServletResponse extends OutputStreamWriter {

    private final OutputStream out;

    public ServletResponse(@NotNull OutputStream out, @NotNull String charsetName) throws UnsupportedEncodingException {
        super(out, charsetName);
        this.out = out;
    }

    public ServletResponse(@NotNull OutputStream out) {
        super(out);
        this.out = out;
    }

    public ServletResponse(@NotNull OutputStream out, @NotNull Charset cs) {
        super(out, cs);
        this.out = out;
    }

    public ServletResponse(@NotNull OutputStream out, @NotNull CharsetEncoder enc) {
        super(out, enc);
        this.out = out;
    }

    public ServletResponse() {
        this(new ByteArrayOutputStream());
    }

    public void setStatus(int i) {
    }

    public void addHeader(String s, String s1) {
    }

    public void setCharacterEncoding(String charset) {
    }

    public void setHeader(String s, String s1) {
    }

    public void setContentType(String contentType) {
    }

    @Override
    public String toString() {
        return this.out.toString();
    }
}
