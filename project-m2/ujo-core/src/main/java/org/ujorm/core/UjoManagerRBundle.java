/*
 *  Copyright 2007-2013 Pavel Ponec
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.UjoAction;

/**
 * A Manager for Java Property Resources Bundle.
 * If you need take a control of a non UJO property object serialization then use a UjoTextable object.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 *  Person person = <span class="java-keywords">new</span> <span class="java-layer-method">Person</span>(); <span class="java-block-comment">// Set attributes ...</span>
 *
 *  UjoManagerRBundle&lt;Person&gt; manager = UjoManagerRBundle.<span class="java-layer-method">getInstance</span>(Person.<span class="java-keywords">class</span>);
 *
 *  <span class="java-block-comment">// Save CSV:</span>
 *  String header = <span class="java-string-literal">"Header Description"</span>;
 *  manager.<span class="java-layer-method">saveResourceBundle</span>(<span class="java-keywords">new</span> <span class="java-layer-method">File</span>(<span class="java-string-literal">"file.properties"</span>), person, header, <span class="java-string-literal">"Save</span><span class="java-string-literal">Context"</span>);
 *
 *  <span class="java-block-comment">// Load CSV:</span>
 *  person = manager.<span class="java-layer-method">loadResourceBundle</span>(<span class="java-keywords">new</span> <span class="java-layer-method">File</span>(<span class="java-string-literal">"file.properties"</span>), <span class="java-keywords">true</span>, <span class="java-string-literal">"LoadContext"</span>);
 * </pre>
 * @author Pavel Ponec
 * @see org.ujorm.extensions.UjoTextable
 */
public class UjoManagerRBundle<UJO extends Ujo> extends UjoService<UJO> {
    
    /**
     * Creates a new instance of UjoManagerRBundle
     */
    public UjoManagerRBundle(KeyList<UJO> keys) {
        super(keys.getType(), keys);
    }

    /**
     * Creates a new instance of UjoManagerRBundle
     */
    public UjoManagerRBundle(Class<UJO> ujoClass, Key ... keys) {
        super(ujoClass, keys);
    }
    
    /**
     * Save Ujo into Java resource bundle
     */
    public void saveResourceBundle(File outputFile, UJO ujo, String header, Object context) throws IOException {
        final OutputStream out = getOutputStream(outputFile);
        try {
            saveResourceBundle(out, ujo, header, context);
        } finally {
            out.close();
        }
    }
    
    /**
     * Save Ujo into Java resource bundle
     */
    @SuppressWarnings("unchecked")
    public void saveResourceBundle(OutputStream out, UJO ujo, String header, Object context) throws IOException {
        java.util.Properties props = new java.util.Properties();
        UjoAction action = new UjoActionImpl(UjoAction.ACTION_RESBUNDLE_EXPORT, context);
        for (Key prop : getKeys()) {

            final Object valueObj = prop.of(ujo);
            final String valueStr = getText(ujo, prop, valueObj, action);
            
            final boolean authorized 
            =  valueStr!=null
            && ujo.readAuthorization(action, prop, valueObj)
            && !getUjoManager().isTransientProperty(prop)
            ;
            
            if (authorized) {
                props.setProperty(prop.getName(), valueStr);
            }
        }
        props.store(out, header);
    }
    
    /**
     * Load an Ujo from Java resource bundle
     */
    public UJO loadResourceBundle(File inputFile, boolean validate, Object context)
    throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException {
        final InputStream inp = getInputStream(inputFile);
        try {
            return loadResourceBundle(inp, validate, context);
        } finally {
            inp.close();
        }
    }
    
    
    /**
     * Load an Ujo from Java resource bundle
     */
    public UJO loadResourceBundle(InputStream inp, boolean validate, Object context)
    throws IOException, InstantiationException, IllegalAccessException {
        UJO ujo = getUjoClass().newInstance();
        PropertyResourceBundle bundle = new PropertyResourceBundle(inp);
        UjoAction action = new UjoActionImpl(UjoAction.ACTION_RESBUNDLE_IMPORT, context);

        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            final String key = keys.nextElement();
            final String value = bundle.getString(key);
            final Key prop = ujo.readKeys().findDirectKey(key, false);
            if (prop!=null) {
                setText(ujo, prop, null, value, action);
            } else if (validate) {
                throw new IllegalArgumentException("An attribute \""+key+"\" was not found in " + ujo.getClass());
            }
        }
        return ujo;
    }
    
    /** Create new instance */
    public static <UJO extends Ujo> UjoManagerRBundle<UJO> getInstance(Class<UJO> ujoClass) {
        return getInstance(ujoClass, (Key[]) null);
    }
    
    /** Create new instance */
    public static <UJO extends Ujo> UjoManagerRBundle<UJO> getInstance(Class<UJO> ujoClass, Key ... keys) {
        return new UjoManagerRBundle<UJO>(ujoClass, keys);
    }
    
}

