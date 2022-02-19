/*
 *  Copyright 2007-2022 Pavel Ponec
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

package org.ujorm.implementation.xml.t002_tech;

import java.math.*;

/**
 * Zero Constant provider.
 * @author Pavel Ponec
 */
public interface ZeroProvider {
    
    public static final Boolean    ZERO_BOOL     = false;
    public static final Byte       ZERO_BYTE     = (byte) 0;
    public static final Character  ZERO_CHAR     = (char) 0;
    public static final Short      ZERO_SHORT    = (short)0;
    public static final Integer    ZERO_INT      = 0;
    public static final Long       ZERO_LONG     = 0L;
    public static final Float      ZERO_FLOAT    = 0F;
    public static final Double     ZERO_DOUBLE   = 0D;
    public static final BigInteger ZERO_BIG_INT  = BigInteger.ZERO;
    public static final BigDecimal ZERO_BIG_DECI = BigDecimal.valueOf(0);
    
    // Some Arrays
    public static final byte[]     ZERO_BYTES    = new byte[0];
    public static final char[]     ZERO_CHARS    = new char[0];
    public static final String     ZERO_STRING   = "";

    /** Returns a zero equivalent for any class. If class is not supported, methods returns a null value.
     * <br>Supported clases depends on an implementation. */
    public Object getZeroValue(Class type);
    
    
}