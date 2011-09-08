/*
 *  Copyright 2007-2010 Pavel Ponec
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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.ujorm.Ujo;
import org.ujorm.UjoProperty;
import org.ujorm.UjoAction;

/**
 * A Manager for CSV import / export.
 * If you need take a control of a non UJO property object serialization then use a UjoTextable object list.
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
public class UjoManagerCSV<UJO extends Ujo> extends UjoService<UJO> {
    
    /** Quotation */
    final private char quotation = '"' ;
    /** CSV Separator */
    private char separator = ';' ;
    /** New Line */
    private String newLine = System.getProperty("line.separator");
    /** Print CSV Header */
    private boolean printHeader = true;
    
    /**
     * Creates a new instance of UjoManagerCSV
     * @param ujoClass   Exported Ujo Class
     * @param properties Exported properties of class, if value is null than all properties are used.
     */
    public UjoManagerCSV(Class<UJO> ujoClass, UjoProperty ... properties) {
        super(ujoClass, properties);
    }
    
    /** Save Ujo into CSV format by codepage UTF-8. */
    public void saveCSV(File file, List<UJO> ujoList, Object context) throws IOException, InstantiationException, IllegalAccessException {
        final OutputStream os = getOutputStream(file);
        try {
            saveCSV(os, UTF_8, ujoList, context);
        } finally {
            os.close();
        }
    }
    
    /**
     * Save Ujo into CSV format.
     * @param out Output Stream.
     * @param cs Character set
     * @param ujoList List of UJO objects
     * @param context
     * @throws java.io.IOException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public void saveCSV(OutputStream out, Charset cs, List<UJO> ujoList, Object context) throws IOException, InstantiationException, IllegalAccessException {
        final Writer writer = new OutputStreamWriter(out, cs!=null ? cs : UTF_8);
        try {
            saveCSV(writer, ujoList, context);
        } finally {
            writer.close();
        }
    }
    
    /** Save Ujo into CSV format */
    @SuppressWarnings("unchecked")
    public void saveCSV(Writer out, List<UJO> ujoList, Object context) throws IOException, InstantiationException, IllegalAccessException {
        if (printHeader) {
            UJO ujo = ujoList.size()>0 ? ujoList.get(0) : getUjoClass().newInstance();
            boolean printSepar = false;
            for (UjoProperty p : getProperties()) {
                
                if (!getUjoManager().isTransientProperty(p)
                && (ujo==null
                ||  ujo.readAuthorization(new UjoActionImpl(UjoAction.ACTION_CSV_EXPORT, context), p, null))
                ){
                    if (printSepar) {
                        out.write(separator);
                    } else {
                        printSepar = true;
                    }
                    printValue(out, getHeaderTitle(p));
                }
            }
            out.write(newLine);
        }
        
        for (UJO ujo : ujoList) {
            boolean printSepar = false;
            for (UjoProperty p : getProperties()) {
                UjoAction action = new UjoActionImpl(UjoAction.ACTION_CSV_EXPORT, context);
                final String value  = getText(ujo, p, UNDEFINED, action);
                if (ujo.readAuthorization(action, p, value)
                && !getUjoManager().isTransientProperty(p)
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
    }
    
    /**
     * Header label for a required Property. You can overwrite the method in case you want to localize header titles.
     * @param p Property
     * @return Default implementation returns <code>UjoProperty.getName()</code> .
     */
    protected String getHeaderTitle(final UjoProperty p) {
        return p.getName();
    }
    
    /** Load an Ujo from CSV format by UTF-8 code Page. */
    public List<UJO> loadCSV(File file, Object context) throws IOException, InstantiationException, IllegalAccessException {
        final Reader reader = new InputStreamReader(getInputStream(file), UTF_8);
        try {
            return loadCSV(new Scanner(reader), context);
        } finally {
            reader.close();
        }
    }
    
    /** Load an Ujo from Java resource bundle */
    public List<UJO> loadCSV(Scanner inp, Object context) throws InstantiationException, IllegalAccessException  {
        List<UJO> result = new ArrayList<UJO>(128);
        StringBuilder value = new StringBuilder(32);
        boolean readHeader = printHeader;
        boolean inside   = false;
        int lineCounter  = 0;
        UjoAction action = new UjoActionImpl(context);
        
        while (inp.hasNextLine()) {
            String line = inp.nextLine();
            ++lineCounter;
            
            if (readHeader) {
                readHeader = false;
                continue;
            }
            UJO ujo = (UJO) getUjoClass().newInstance();
            result.add(ujo);
            int propPointer = 0;  // Property Pointer
            
            for (int i=0; i<line.length(); i++) {
                char c = line.charAt(i);
                
                if (inside) { // Inside a quotation
                    if (c==quotation) {
                        int next = i+1;
                        if (next<line.length() && line.charAt(next)=='"' ) {
                            i++;
                        } else {
                            inside = false;
                            continue;
                        }
                    }
                    value.append(c);
                    
                } else { // Outside a quotation
                    if (c==separator) {
                        writeValue(ujo, value, propPointer++, lineCounter, action);
                    } else if (c==quotation) {
                        inside = true;
                    } else {
                        value.append(c);
                    }
                }
            }
            writeValue(ujo, value, propPointer++, lineCounter, action);
        }
        return result;
    }
    
    /** Write value to UJO */
    protected void writeValue
    ( final UJO ujo
    , final StringBuilder value
    , final int propPointer
    , final int lineCounter
    , final UjoAction action    
    ) throws IllegalArgumentException {
        if (propPointer>=getProperties().length) {
            throw new IllegalArgumentException("Too many columns on the row: " + lineCounter + " value: " + value.toString());
        }
        setText(ujo, getProperties()[propPointer], null, value.toString(), action);
        value.setLength(0);
    }
    
    /** Print Text */
    protected void printValue(Writer out, String value) throws IOException {
        if (UjoManager.isFilled(value)
        && (value.indexOf(separator)>=0
        ||  value.indexOf(quotation)>=0)
        ){
            out.write(quotation);
            
            for (int i=0; i<value.length(); i++) {
                char c = value.charAt(i);
                if (c==quotation) {
                    out.write(quotation);
                }
                out.write(c);
            }
            out.write(quotation);
        } else {
            out.write(value);
        }
    }
    
    // ----------- ATTRIBUTES -----------------
    
    public char getSeparator() {
        return separator;
    }
    
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
    
    /** Print CSV Header */
    public boolean isPrintHeader() {
        return printHeader;
    }
    
    /** Print CSV Header */
    public UjoManagerCSV setPrintHeader(boolean printHeader) {
        this.printHeader = printHeader;
        return this;
    }
    
    // -------------- STATIC ----------------
    
    /** Create new instance */
    public static <UJO extends Ujo> UjoManagerCSV<UJO> getInstance(Class<UJO> ujoClass) {
        return getInstance(ujoClass, (UjoProperty[]) null);
    }
    
    /** Create new instance */
    public static <UJO extends Ujo> UjoManagerCSV<UJO> getInstance(Class<UJO> ujoClass, UjoProperty ... properties) {
        return new UjoManagerCSV<UJO>(ujoClass, properties);
    }
    
    
}

