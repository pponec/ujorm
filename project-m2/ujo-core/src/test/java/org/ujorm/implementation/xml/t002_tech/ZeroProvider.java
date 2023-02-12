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

    Boolean    ZERO_BOOL     = false;
    Byte       ZERO_BYTE     = (byte) 0;
    Character  ZERO_CHAR     = (char) 0;
    Short      ZERO_SHORT    = (short)0;
    Integer    ZERO_INT      = 0;
    Long       ZERO_LONG     = 0L;
    Float      ZERO_FLOAT    = 0F;
    Double     ZERO_DOUBLE   = 0D;
    BigInteger ZERO_BIG_INT  = BigInteger.ZERO;
    BigDecimal ZERO_BIG_DECI = BigDecimal.valueOf(0);

    // Some Arrays
    byte[]     ZERO_BYTES    = new byte[0];
    char[]     ZERO_CHARS    = new char[0];
    String     ZERO_STRING   = "";

    /** Returns a zero equivalent for any class. If class is not supported, methods returns a null value.
     * <br>Supported clases depends on an implementation. */
    Object getZeroValue(Class type);


}