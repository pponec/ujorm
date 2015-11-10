/*
 *  Copyright 2007-2015 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.core;

import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;

/**
 * A Manager for CSV import / export.
 * If you need take a control of a non U Key object serialization then use a UjoTextable object list.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 *  List&lt;Person&gt; people = <span class="java-keywords">new</span> ArrayList&lt;Person&gt;(<span class="java-numeric-literals">0</span>);
 *  UjoManagerCSV&lt;Person&gt; manager = UjoManagerCSV.<span class="java-layer-method">getInstance</span>(Person.<span class="java-keywords">class</span>);
 *
 *  <span class="java-block-comment">// Save CSV:</span>
 *  manager.<span class="java-layer-method">saveCSV</span>(<span class="java-keywords">new</span> <span class="java-layer-method">File</span>(<span class="java-string-literal">"file.csv"</span>), people, <span class="java-string-literal">"SaveContext"</span>);
 *
 *  <span class="java-block-comment">// Load CSV:</span>
 *  people = manager.<span class="java-layer-method">loadCSV</span>(<span class="java-keywords">new</span> <span class="java-layer-method">File</span>(<span class="java-string-literal">"file.csv"</span>), <span class="java-string-literal">"LoadContext"</span>);
 * </pre>
 * @author Pavel Ponec
 * @see org.ujorm.extensions.UjoTextable
 */
public class UjoManagerCSV<U extends Ujo> extends UjoService<U> {

    /** Quotation, the default value is {@code "} */
    final protected char QUOTATION = '"';
    /** New line character */
    final protected char NEW_LINE_CHAR = '\n';
    /** New Line String, default value from system property */
    private String newLine = System.getProperty("line.separator");
    /** CSV Separator, the default value is {@code ;} */
    private char separator = ';';
    /** Enable to print a CSV header, the default value is {@code true} */
    private boolean printHeader = true;
    /** Skip empty lines on reading, the default value is {@code true}. */
    private boolean skipEmptyLines = true;
    /** Skip the last CSV column values.
     * A default value of this attribute is {@code false}. */
    private boolean skipLastColumns = false;
    /** Print or validate the CSV Header content (nonnull value) */
    private CharSequence[] headerContent = new CharSequence[0];

    /**
     * Creates a new instance of UjoManagerCSV
     * @param keys Exported keys of class, if value is null than all keys are used.
     */
    public UjoManagerCSV(KeyList<U> keys) {
        super(keys.getType(), keys);
    }
    /**
     * Creates a new instance of UjoManagerCSV
     * @param ujoClass Exported Ujo Class
     * @param keys Exported keys of class, if value is null than all keys are used.
     */
    public UjoManagerCSV(Class<U> ujoClass, KeyList<U> keys) {
        super(ujoClass, keys);
    }

    /**
     * Creates a new instance of UjoManagerCSV
     * @param ujoClass Exported ujoClass Class
     */
    public UjoManagerCSV(Class<U> ujoClass) {
        super(ujoClass, (KeyList<U>) null);
    }

    /**
     * Creates a new instance of UjoManagerCSV
     * @param ujoClass Exported Ujo Class
     * @param keys Exported keys of class, if value is null than all keys are used.
     */
    public UjoManagerCSV(Class<U> ujoClass, Key... keys) {
        super(ujoClass, keys);
    }

    /** Save Ujo into CSV format by code page UTF-8. */
    public void saveCSV(File file, List<U> ujoList, Object context) throws IllegalStateException {
        OutputStream os = null;
        try {
            os = getOutputStream(file);
            saveCSV(os, UTF_8, ujoList, context);
        } catch (Exception e) {
            throwsCsvFailed(e, context);
        } finally {
            close(os, context);
        }
    }

    /**
     * Save Ujo into CSV format.
     * @param out Output Stream.
     * @param cs Character set
     * @param ujoList List of Ujo objects
     * @param context
     */
    public void saveCSV(OutputStream out, Charset cs, List<U> ujoList, Object context)
            throws IllegalStateException {
        final Writer writer = new OutputStreamWriter(out, cs != null ? cs : UTF_8);
        try {
            saveCSV(writer, ujoList, context);
        } catch (Exception e) {
            throwsCsvFailed(e, context);
        } finally {
            close(writer, context);
        }
    }

    /** Save Ujo into CSV format */
    @SuppressWarnings("unchecked")
    public void saveCSV(Writer out, List<U> ujoList, Object context)
            throws IllegalStateException {
        try {
            if (printHeader) {
                if (isHeaderFilled()) {
                    printHeaders(out);
                } else {
                    final U ujo = ujoList.size() > 0
                            ? ujoList.get(0)
                            : getUjoClass().newInstance();
                    boolean printSepar = false;
                    for (Key p : getKeys()) {
                        if (!getUjoManager().isTransient(p)
                                && (ujo == null
                                || ujo.readAuthorization(new UjoActionImpl(UjoAction.ACTION_CSV_EXPORT, context), p, null))
                        ){
                            if (printSepar) {
                                out.write(separator);
                            } else {
                                printSepar = true;
                            }
                            printValue(out, getHeaderTitle(p));
                        }
                    }
                }
                out.write(newLine);
            }

            for (U ujo : ujoList) {
                boolean printSepar = false;
                for (Key p : getKeys()) {
                    UjoAction action = new UjoActionImpl(UjoAction.ACTION_CSV_EXPORT, context);
                    final String value = getText(ujo, p, UNDEFINED, action);
                    if (ujo.readAuthorization(action, p, value)
                            && !getUjoManager().isTransient(p)
                    ){
                        if (printSepar) {
                            out.write(separator);
                        } else {
                            printSepar = true;
                        }
                        printValue(out, value);
                    }
                }
                out.write(newLine);
            }
        } catch (Exception e) {
            throwsCsvFailed(e, context);
        }
    }

    /**
     * Header label for a required Key. You can overwrite the method in case you want to localize header titles.
     * @param p Property
     * @return Default implementation returns <code>Key.getName()</code> .
     */
    protected String getHeaderTitle(final Key p) {
        return p.getName();
    }

    /** Load an Ujo from CSV format by UTF-8 code-page.
     * @param file An input data
     * @param context Context of loading will be passed to the method
     * {@link Ujo#readAuthorization(org.ujorm.UjoAction, org.ujorm.Key, java.lang.Object)}
     * inside an UjoAction
     * @return List of Ujo
     * @throws IllegalStateException can be throwed in case the header check failed
     */
    public List<U> loadCSV(File file, Object context) throws IllegalStateException {
        Reader reader = null;
        try {
            reader = RingBuffer.createReader(file);
            return loadCSV(new Scanner(reader), context);
        } catch (Exception e) {
            throwsCsvFailed(e, context);
        } finally {
            close(reader, context);
        }
        return null;
    }

    /** Load an Ujo from CSV format by UTF-8 code-page.
     * @param file An input data
     * @param context Context of loading will be passed to the method
     * {@link Ujo#readAuthorization(org.ujorm.UjoAction, org.ujorm.Key, java.lang.Object)}
     * inside an UjoAction
     * @return List of U
     * @throws IllegalStateException can be throwed in case the header check failed
     */
    public List<U> loadCSV(Scanner inp, Object context) throws IllegalStateException {
        final List<U> result = new ArrayList<U>(128);
        final StringBuilder value = new StringBuilder(32);
        final UjoAction action = new UjoActionImpl(context);
        boolean readHeader = printHeader;
        boolean inside = false;
        int lineCounter = 0;

        try {
            while (inp.hasNextLine()) {
                String line = inp.nextLine();
                ++lineCounter;

                if (skipEmptyLines && line.isEmpty()) {
                    continue;
                }

                if (readHeader) {
                    if (isHeaderFilled()
                    && !line.startsWith(getHeaderContent())) {
                        throw new IllegalStateException("The import header must start with the: " + getHeaders());
                    }
                    readHeader = false;
                    continue;
                }
                final U ujo = (U) getUjoClass().newInstance();
                result.add(ujo);
                int keyPointer = 0;  // Key pointer

                for (int i = 0, max = line.length(); i < max; i++) {
                    final char c = line.charAt(i);
                    final int next = i + 1;

                    if (inside) { // Inside a cell value:
                        if (c == QUOTATION) {
                            if (next < max && line.charAt(next) == QUOTATION) {
                                i++;
                            } else {
                                inside = false;
                                continue;
                            }
                        }
                        value.append(c);

                    } else { // Outside a quotation
                        if (c == separator) {
                            writeValue(ujo, value, keyPointer++, lineCounter, action);
                            value.setLength(0);
                        } else if (c == QUOTATION) {
                            inside = true;
                        } else {
                            value.append(c);
                        }
                    }

                    // Check the breaking cell:
                    if (inside
                    && next == max
                    && inp.hasNextLine()) {
                        line = line + NEW_LINE_CHAR + inp.nextLine();
                        max = line.length();
                        ++lineCounter;
                    }
                }
                writeValue(ujo, value, keyPointer++, lineCounter, action);
                value.setLength(0);
            }
        } catch (Exception e) {
            throwsCsvFailed(e, context);
        }
        return result;
    }

    /** Write value to U. */
    protected void writeValue
    ( final U ujo
    , final StringBuilder value
    , final int keyPointer
    , final int lineCounter
    , final UjoAction action
    ) throws IllegalArgumentException {
        final KeyList<U> keys = getKeys();
        if (keyPointer >= keys.size()) {
            if (skipLastColumns || value.length() == 0) {
                return;
            }
            String msg = String.format("Too many columns on the row %s with value '%s'."
                    + " Try to modify the attribute: %s."
                    , lineCounter
                    , value
                    , "skipLastColumns");
            throw new IllegalArgumentException(msg);
        }
        setText(ujo, keys.get(keyPointer), null, value.toString(), action);
    }

    /** Print Text */
    protected void printValue(Writer out, String value) throws IOException {
        if (UjoManager.isFilled(value)
        && (value.indexOf(separator) >= 0
        ||  value.indexOf(QUOTATION) >= 0
        ||  value.indexOf(NEW_LINE_CHAR) >= 0)
        ){
            out.write(QUOTATION);

            for (int i = 0, max = value.length(); i < max; i++) {
                final char c = value.charAt(i);
                out.write(c);
                if (c == QUOTATION) {
                    out.write(c); // Print second quotation
                }
            }
            out.write(QUOTATION);
        } else {
            out.write(value);
        }
    }

    // ----------- ATTRIBUTES -----------------

    /** Get CSV separator */
    public char getSeparator() {
        return separator;
    }

    /** CSV Separator, the default value is {@code ;} */
    public UjoManagerCSV setSeparator(char separator) {
        this.separator = separator;
        return this;
    }

    /** New Line */
    public String getNewLine() {
        return newLine;
    }

    /** New Line character sequence. It is allowed a combination of characters (\\r or \\n). */
    public void setNewLine(String newLine) {
        this.newLine = newLine;
    }

    /** Print or skip the CSV Header */
    public boolean isPrintHeader() {
        return printHeader;
    }

    /** Print CSV Header, the default value is {@code true} */
    public UjoManagerCSV setPrintHeader(boolean printHeader) {
        this.printHeader = printHeader;
        return this;
    }

    /**
     * Skip empty lines on reading, the default value is {@code true}.
     * @return the skipEmptyLines
     */
    public boolean isSkipEmptyLines() {
        return skipEmptyLines;
    }

    /**
     * Skip empty lines on reading, the default value is {@code true}.
     * @param skip the skipEmptyLines to set
     */
    public void setSkipEmptyLines(boolean skip) {
        this.skipEmptyLines = skip;
    }

    /** Skip the last CSV column values.
     * A default value of this attribute is {@code false}. */
    public boolean isSkipLastColumns() {
        return skipLastColumns;
    }

    /** Skip the last CSV column values.
     * A default value of this attribute is {@code false}. */
    public void setSkipLastColumns(boolean skipLastColumns) {
        this.skipLastColumns = skipLastColumns;
    }

    /** PrintHeaders text with separators */
    protected String getHeaders() {
        try {
            final CharArrayWriter result = new CharArrayWriter(256);
            printHeaders(result);
            return result.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** PrintHeaders text with separators */
    protected void printHeaders(Writer out) throws IOException {
        for (int i = 0; i < headerContent.length; i++) {
            if (i > 0) {
                out.append(this.separator);
            }
            out.append(headerContent[i]);
        }
    }

    /** Is a header is filled */
    protected boolean isHeaderFilled() {
        return getHeaderContent().length() > 0;
    }

    /** CSV header content:
     * <ul>
     * <li>empty string - use a default header to print or skip the check in parsing time</li>
     * <li>other string - use the value to print a header or check the a start header line in a reading time</li>
     * </ul>
     */
    public String getHeaderContent() {
        try {
            final CharArrayWriter out = new CharArrayWriter(256);
            printHeaders(out);
            return out.toString();
        } catch (IOException e) {
            throw new IllegalStateException("getHeaderContent failed", e);
        }
    }

    /** CSV header content items will be separated by the required character.
     * No character of the content will be escaped.
     * The empty text or {@code null} value means the undefined header.
     * @param headerContent a String or Key arguments
     */
    public void setHeaderContent(CharSequence... headerContent) {
        this.headerContent = headerContent != null
                ? headerContent
                : new CharSequence[0];
    }

    /** Assign the entire content of the CSV where no character is escaped.
     * The empty text or {@code null} value means the undefined header.
     * @param headerContent a String or Key arguments
     */
    public void setHeaderContent(String headerContent) {
        if (headerContent == null) {
            headerContent = "";
        }
        final CharSequence[] params = {headerContent};
        setHeaderContent(params);
    }

    /** Close an {@link Closeable} object */
    private void close(final Closeable closeable, Object context) throws IllegalStateException {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            throwsCsvFailed(e, context);
        }
    }

    /** Throws an CSV exception. */
    private void throwsCsvFailed(Throwable e, Object context) throws IllegalStateException {
        throw new IllegalStateException("CSV failed for a context: " + context, e);
    }

    // -------------- STATIC ----------------

    /** Create new instance */
    public static <U extends Ujo> UjoManagerCSV<U> of(Class<U> ujoClass) {
        return new UjoManagerCSV<U>(ujoClass);
    }

    /** Create new instance */
    public static <U extends Ujo> UjoManagerCSV<U> of(Class<U> ujoClass, Key... keys) {
        return new UjoManagerCSV<U>(ujoClass, keys);
    }

    /** Create new instance where the domain class is get from an array of the keys. */
    public static <U extends Ujo> UjoManagerCSV<U> of(Key... keys) {
        return new UjoManagerCSV<U>(KeyRing.getBaseType(keys), keys);
    }

    /** Create new instance by a KeyRing */
    public static <U extends Ujo> UjoManagerCSV<U> of(KeyList<U> keys) {
        return of(keys.toArray());
    }

    /** Create new instance
     * @deprecated Use the method {@code of(...)}
     */
    public static <U extends Ujo> UjoManagerCSV<U> getInstance(Class<U> ujoClass) {
        return new UjoManagerCSV<U>(ujoClass, (KeyList<U>) null);
    }

    /** Create new instance
     * @deprecated Use the method {@code of(...)}
     */
    public static <U extends Ujo> UjoManagerCSV<U> getInstance(Class<U> ujoClass, Key... keys) {
        return new UjoManagerCSV<U>(ujoClass, keys);
    }

    /** Create new instance where the domain class is get from an array of the keys.
     * @deprecated Use the method {@code of(...)}
     */
    public static <U extends Ujo> UjoManagerCSV<U> getInstance(Key... keys) {
        return new UjoManagerCSV<U>(KeyRing.getBaseType(keys), keys);
    }
}
